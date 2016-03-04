package com.cloudera.director.vsphere.compute;

import static com.cloudera.director.spi.v1.model.InstanceTemplate.InstanceTemplateConfigurationPropertyToken.INSTANCE_NAME_PREFIX;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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


/**
 * @author chiq
 *
 */

/**
 * A provider for compute resources that works with a predefined list of hosts.
 */
public class VSphereComputeProvider
extends AbstractComputeProvider<VSphereComputeInstance, VSphereComputeInstanceTemplate> {

   private static final Logger LOG = Logger.getLogger(VSphereComputeProvider.class.getName());

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

   private final Deque<String> availableHosts = new ArrayDeque<String>();
   private final Map<String, String> allocations = new HashMap<String, String>();
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

   synchronized Deque<String> getAvailableHosts() {
      return availableHosts;
   }

   synchronized Map<String, String> getAllocations() {
      return allocations;
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
         IGroupProvisionService groupProvisionService = new GroupProvisionService(this.credentials, template, template.getConfigurationValue(INSTANCE_NAME_PREFIX, templateLocalizationContext), instanceIds, minCount);
         groupProvisionService.provision();
         this.allocations.putAll(groupProvisionService.getAllocations());
      } catch (Exception e) {
         throw new VsphereDirectorException(e);
      }
   }

   @Override
   public synchronized Collection<VSphereComputeInstance> find(
         VSphereComputeInstanceTemplate template, Collection<String> instanceIds)
               throws InterruptedException {

      List<VSphereComputeInstance> result = new ArrayList<VSphereComputeInstance>();
      for (String currentId : instanceIds) {
         String host = allocations.get(currentId);
         if (host != null) {
            try {
               result.add(new VSphereComputeInstance(template, currentId, InetAddress.getByName(host)));

            } catch (UnknownHostException e) {
               throw new RuntimeException(e);
            }
         }
      }

      return result;
   }

   @Override
   public synchronized Map<String, InstanceState> getInstanceState(
         VSphereComputeInstanceTemplate template, Collection<String> instanceIds) {

      Map<String, InstanceState> result = new HashMap<String, InstanceState>();
      for (String currentId : instanceIds) {
         if (allocations.containsKey(currentId)) {
            result.put(currentId, new SimpleInstanceState(InstanceStatus.RUNNING));
         } else {
            result.put(currentId, new SimpleInstanceState(InstanceStatus.DELETED));
         }
      }

      return result;
   }

   @Override
   public synchronized void delete(VSphereComputeInstanceTemplate template,
         Collection<String> instanceIds) throws InterruptedException {

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
}
