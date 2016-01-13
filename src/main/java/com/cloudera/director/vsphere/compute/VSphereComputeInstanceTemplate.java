package com.cloudera.director.vsphere.compute;

import java.util.List;
import java.util.Map;

import com.cloudera.director.spi.v1.compute.ComputeInstanceTemplate;
import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.model.LocalizationContext;
import com.cloudera.director.spi.v1.util.ConfigurationPropertiesUtil;

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

  private final String numCPUs;
  private final String memorySize;
  private final String diskType;
  private final String dataDiskSize;
  private final String templateVm;
  private final String network;
  private final String haType;

public String getType() {
   return type;
}

public String getNumCPUs() {
   return numCPUs;
}

public String getMemorySize() {
   return memorySize;
}

public String getDiskType() {
   return diskType;
}

public String getDataDiskSize() {
   return dataDiskSize;
}

public String getTemplateVm() {
   return templateVm;
}

public String getNetwork() {
   return network;
}

public String getHaType() {
   return haType;
}

public static List<ConfigurationProperty> getConfigurationProperties() {
    return CONFIGURATION_PROPERTIES;
  }

  public VSphereComputeInstanceTemplate(String name,Configured configuration, Map<String, String> tags, LocalizationContext providerLocalizationContext) {
    super(name, configuration, tags, providerLocalizationContext);
    LocalizationContext localizationContext = getLocalizationContext();

    type = getConfigurationValue(VSphereComputeInstanceTemplateConfigurationPropertyToken.TYPE, localizationContext);

    numCPUs = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.CPU_NUM,localizationContext);
    memorySize = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.MEM_SIZE_GB,localizationContext);
    diskType = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.DISK_TYPE,localizationContext);
    dataDiskSize = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.STORAGE_SIZE_GB,localizationContext);
    templateVm = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.TEMPLATE_VM,localizationContext);
    network = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.NETWORK_NAME,localizationContext);
    haType = configuration.getConfigurationValue(
          VSphereComputeInstanceTemplateConfigurationPropertyToken.HA_TYPE,localizationContext);

  }
}
