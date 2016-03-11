package com.cloudera.director.vsphere.compute;

import static com.cloudera.director.spi.v1.model.InstanceTemplate.InstanceTemplateConfigurationPropertyToken.INSTANCE_NAME_PREFIX;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.spi.v1.compute.util.AbstractComputeInstance;
import com.cloudera.director.spi.v1.compute.util.AbstractComputeProvider;
import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.ConfigurationValidator;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.model.InstanceState;
import com.cloudera.director.spi.v1.model.InstanceStatus;
import com.cloudera.director.spi.v1.model.LocalizationContext;
import com.cloudera.director.spi.v1.model.Resource;
import com.cloudera.director.spi.v1.model.util.CompositeConfigurationValidator;
import com.cloudera.director.spi.v1.model.util.SimpleInstanceState;
import com.cloudera.director.spi.v1.model.util.SimpleResourceTemplate;
import com.cloudera.director.spi.v1.provider.ResourceProviderMetadata;
import com.cloudera.director.spi.v1.provider.util.SimpleResourceProviderMetadata;
import com.cloudera.director.spi.v1.util.ConfigurationPropertiesUtil;
import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.service.impl.GroupProvisionService;
import com.cloudera.director.vsphere.service.impl.GroupTerminateService;
import com.cloudera.director.vsphere.service.intf.IGroupProvisionService;
import com.cloudera.director.vsphere.vm.service.impl.VmPowerOperationService;
import com.cloudera.director.vsphere.vm.service.impl.VmService;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;


/**
 * @author chiq
 *
 */

/**
 * A provider for compute resources that works with a predefined list of hosts.
 */
public class VSphereComputeProvider
extends AbstractComputeProvider<VSphereComputeInstance, VSphereComputeInstanceTemplate> {

   private static final Logger LOG = LoggerFactory.getLogger(VmPowerOperationService.class);

   protected static final List<ConfigurationProperty> CONFIGURATION_PROPERTIES =
         ConfigurationPropertiesUtil.asConfigurationPropertyList(VSphereComputeProviderConfigurationPropertyToken.values());

   public static final String ID = "compute";

   public static final ResourceProviderMetadata METADATA = SimpleResourceProviderMetadata.builder()
         .id(ID)
         .name("vCenter Server")
         .description("Allocates instances from a predefined list")
         .providerClass(VSphereComputeProvider.class)
         .providerConfigurationProperties(CONFIGURATION_PROPERTIES)
         .resourceTemplateConfigurationProperties(
               VSphereComputeInstanceTemplate.getConfigurationProperties())
               .build();

   private final VSphereCredentials credentials;
   private final Configured configuration;

   private final ConfigurationValidator resourceTemplateConfigurationValidator;

   public VSphereComputeProvider(Configured configuration, LocalizationContext cloudLocalizationContext) {
      super(configuration, METADATA, cloudLocalizationContext);

      String vcServer = configuration.getConfigurationValue(VSphereComputeProviderConfigurationPropertyToken.VC_SERVER, cloudLocalizationContext);
      String vcPort = configuration.getConfigurationValue(VSphereComputeProviderConfigurationPropertyToken.VC_SERVER_PORT, cloudLocalizationContext);
      String vcUsername = configuration.getConfigurationValue(VSphereComputeProviderConfigurationPropertyToken.VC_SERVER_USERNAME, cloudLocalizationContext);
      String vcPassword = configuration.getConfigurationValue(VSphereComputeProviderConfigurationPropertyToken.VC_SERVER_PASSWORD, cloudLocalizationContext);
      this.credentials = new VSphereCredentials(vcServer, vcPort, vcUsername, vcPassword);

      this.configuration = configuration;

      this.resourceTemplateConfigurationValidator =
            new CompositeConfigurationValidator(METADATA.getResourceTemplateConfigurationValidator(),
                  new VSphereComputeInstanceTemplateConfigurationValidator(this));
   }

   @Override
   public ConfigurationValidator getResourceTemplateConfigurationValidator() {
      return resourceTemplateConfigurationValidator;
   }

   @Override
   public Resource.Type getResourceType() {
      return AbstractComputeInstance.TYPE;
   }

   @Override
   public VSphereComputeInstanceTemplate createResourceTemplate(
         String name, Configured configuration, Map<String, String> tags) {
      return new VSphereComputeInstanceTemplate(name, configuration, tags, getLocalizationContext());
   }

   @Override
   public synchronized void allocate(VSphereComputeInstanceTemplate template,
         Collection<String> instanceIds, int minCount) throws InterruptedException {

      LocalizationContext providerLocalizationContext = getLocalizationContext();
      LocalizationContext templateLocalizationContext = SimpleResourceTemplate.getTemplateLocalizationContext(providerLocalizationContext);

      try {
         template.validate(this.credentials);
         IGroupProvisionService groupProvisionService = new GroupProvisionService(this.credentials, template, template.getConfigurationValue(INSTANCE_NAME_PREFIX, templateLocalizationContext), instanceIds, minCount);
         groupProvisionService.provision();
      } catch (Exception e) {
         throw new VsphereDirectorException(e);
      }
   }

   @Override
   public synchronized Collection<VSphereComputeInstance> find(VSphereComputeInstanceTemplate template, Collection<String> instanceIds) throws InterruptedException {

      LocalizationContext providerLocalizationContext = getLocalizationContext();
      LocalizationContext templateLocalizationContext = SimpleResourceTemplate.getTemplateLocalizationContext(providerLocalizationContext);

      VmService vmService = new VmService(credentials);

      List<VSphereComputeInstance> result = new ArrayList<VSphereComputeInstance>();

      for (String currentId : instanceIds) {
         try {
            String decoratedInstanceName = decorateInstanceName(template, currentId, templateLocalizationContext);
            String currentIpAddress = vmService.getIpAddress(decoratedInstanceName);
            result.add(new VSphereComputeInstance(template, currentId, InetAddress.getByName(currentIpAddress)));
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }

      return result;
   }

   @Override
   public synchronized Map<String, InstanceState> getInstanceState(VSphereComputeInstanceTemplate template, Collection<String> instanceIds) {

      LocalizationContext providerLocalizationContext = getLocalizationContext();
      LocalizationContext templateLocalizationContext = SimpleResourceTemplate.getTemplateLocalizationContext(providerLocalizationContext);

      VmService vmService = new VmService(credentials);

      Map<String, InstanceState> result = new HashMap<String, InstanceState>();
      for (String currentId : instanceIds) {
         String decoratedInstanceName = null;
         VirtualMachine vm = null;
         try {
            decoratedInstanceName = decorateInstanceName(template, currentId, templateLocalizationContext);
            vm = vmService.getVm(decoratedInstanceName);
         } catch (Exception e) {
            if (vm == null) {
               result.put(currentId, new SimpleInstanceState(InstanceStatus.DELETED));
               LOG.info("Node '{}' not found.", decoratedInstanceName);
            } else {
               result.put(currentId, new SimpleInstanceState(InstanceStatus.UNKNOWN));
               LOG.info("Node '{}' found but the status is unnormal.", decoratedInstanceName);
            }
         }

         if (vm != null) {
            if (VirtualMachinePowerState.poweredOn.equals(vm.getRuntime().getPowerState())) {
               if (vm.getGuest().getIpAddress() != null) {
                  result.put(currentId, new SimpleInstanceState(InstanceStatus.RUNNING));
               } else {
                  result.put(currentId, new SimpleInstanceState(InstanceStatus.UNKNOWN));
               }
            } else if (VirtualMachinePowerState.poweredOff.equals(vm.getRuntime().getPowerState()) || VirtualMachinePowerState.suspended.equals(vm.getRuntime().getPowerState())) {
               result.put(currentId, new SimpleInstanceState(InstanceStatus.STOPPED));
            }
         }

      }

      return result;
   }

   @Override
   public synchronized void delete(VSphereComputeInstanceTemplate template, Collection<String> instanceIds) throws InterruptedException {

      if (instanceIds.isEmpty()) {
         return;
      }

      LocalizationContext providerLocalizationContext = getLocalizationContext();
      LocalizationContext templateLocalizationContext = SimpleResourceTemplate.getTemplateLocalizationContext(providerLocalizationContext);

      try {
         GroupTerminateService groupTerminateService = new GroupTerminateService(this.credentials, template, template.getConfigurationValue(INSTANCE_NAME_PREFIX, templateLocalizationContext), instanceIds);
         groupTerminateService.terminate();
      } catch (Exception e) {
         throw new VsphereDirectorException(e);
      }
   }

   private static String decorateInstanceName(VSphereComputeInstanceTemplate template, String currentId, LocalizationContext templateLocalizationContext) {
      return template.getConfigurationValue(INSTANCE_NAME_PREFIX, templateLocalizationContext) + "-" + currentId;
   }

}
