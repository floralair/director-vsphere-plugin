package com.cloudera.director.vsphere.compute;

import java.util.List;
import java.util.Map;

import com.cloudera.director.spi.v1.compute.ComputeInstanceTemplate;
import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.model.LocalizationContext;
import com.cloudera.director.spi.v1.util.ConfigurationPropertiesUtil;
import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.utils.NetworkUtil;
import com.cloudera.director.vsphere.utils.VmUtil;

/**
  * @author chiq
  *
  */
public class VSphereComputeInstanceTemplate extends ComputeInstanceTemplate {

  /**
   * The list of configuration properties (including inherited properties).
   */
  private static final List<ConfigurationProperty> CONFIGURATION_PROPERTIES =
      ConfigurationPropertiesUtil.merge(
          ComputeInstanceTemplate.getConfigurationProperties(),
          ConfigurationPropertiesUtil.asConfigurationPropertyList(
              VSphereComputeInstanceTemplateConfigurationPropertyToken.values())
      );

  /**
   * The instance type.
   */
  private final String type;

  private final int numCPUs;
  private final long memorySize;
  private final String storageType;
  private final long dataDiskSize;
  private final String templateVm;
  private final String network;

public String getType() {
   return type;
}

public int getNumCPUs() {
   return numCPUs;
}

public long getMemorySize() {
   return memorySize;
}

public String getStorageType() {
   return storageType;
}

public long getDataDiskSize() {
   return dataDiskSize;
}

public String getTemplateVm() {
   return templateVm;
}

public String getNetwork() {
   return network;
}

public static List<ConfigurationProperty> getConfigurationProperties() {
    return CONFIGURATION_PROPERTIES;
  }

  public VSphereComputeInstanceTemplate(String name,Configured configuration, Map<String, String> tags, LocalizationContext providerLocalizationContext) {
    super(name, configuration, tags, providerLocalizationContext);
    LocalizationContext localizationContext = getLocalizationContext();

    type = getConfigurationValue(VSphereComputeInstanceTemplateConfigurationPropertyToken.TYPE, localizationContext);

    numCPUs = Integer.parseInt(configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.CPU_NUM,localizationContext));
    memorySize = Long.parseLong(configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.MEM_SIZE_GB,localizationContext));
    storageType = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.STORAGE_TYPE,localizationContext);
    dataDiskSize = Long.parseLong(configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.STORAGE_SIZE_GB,localizationContext));
    templateVm = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.TEMPLATE_VM,localizationContext);
    network = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.NETWORK_NAME,localizationContext);

  }

  public void validate(VSphereCredentials credentials) {
     try {
        VmUtil.getVirtualMachine(credentials.getServiceInstance(), templateVm, true);
     } catch (Exception e) {
        throw new VsphereDirectorException("There is no template VM named " + templateVm + " in the vCenter server.", e);
     }

     try {
        NetworkUtil.getNetwork(credentials.getServiceInstance(), network);
     } catch (Exception e) {
        throw new VsphereDirectorException("There is no network named " + network + " in the vCenter server.", e);
     }
  }
}
