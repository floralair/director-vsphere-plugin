package com.cloudera.director.vsphere.compute;

import com.cloudera.director.spi.v1.compute.ComputeInstanceTemplate;
import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.Property;
import com.cloudera.director.spi.v1.model.util.SimpleConfigurationPropertyBuilder;

/**
 * @author jiahuili
 *
 */
public enum VSphereComputeInstanceTemplateConfigurationPropertyToken
    implements com.cloudera.director.spi.v1.model.ConfigurationPropertyToken {

  CPU_NUM(new SimpleConfigurationPropertyBuilder()
          .configKey("cpuNum")
          .name("CPU number")
          .defaultDescription(
                  "The CPU Number")
          .defaultValue("2")
          .required(true)
          .build()),

  MEM_SIZE_GB(new SimpleConfigurationPropertyBuilder()
          .configKey("memorySizeGB")
          .name("Memory (GB)")
          .defaultDescription("The memory size (in GB)")
          .defaultValue("2")
          .required(true)
          .widget(ConfigurationProperty.Widget.NUMBER)
          .type(Property.Type.INTEGER)
          .build()),

  TYPE(new SimpleConfigurationPropertyBuilder()
          .configKey("type")
          .name("Type")
          .required(true)
          .widget(ConfigurationProperty.Widget.OPENLIST)
          .defaultDescription("The instance type.")
          .defaultErrorMessage("Instance type is mandatory")
          .addValidValues("Custom")
          .defaultValue("Custom")
          .build()),

  DISK_TYPE(new SimpleConfigurationPropertyBuilder()
          .configKey("storageType")
          .name("Storage type")
          .required(true)
          .widget(ConfigurationProperty.Widget.OPENLIST)
          .defaultDescription(
                  "Select Storage type as \"SHARE\" or \"LOCAL\""
          ).defaultErrorMessage("Instance type is mandatory")
          .addValidValues(
                  "SHARE",
                  "LOCAL")
          .defaultValue("LOCAL")
          .build()),

  /**
   * Size of the root partition in GBs.
   */
  STORAGE_SIZE_GB(new SimpleConfigurationPropertyBuilder()
          .configKey("storageSizeGB")
          .name("Data disk size (GB)")
          .defaultValue("50")
          .defaultDescription(
                  "Specify a size for the vm data disk (in GB). "
          ).widget(ConfigurationProperty.Widget.NUMBER)
          .type(Property.Type.INTEGER)
          .build()),

  NETWORK_NAME(new SimpleConfigurationPropertyBuilder()
          .configKey("networkName")
          .name("Network name")
          .defaultDescription(
                  "The network identifier.<br />")
          .defaultValue("default")
          .required(true)
          .build()),

  TEMPLATE_VM(new SimpleConfigurationPropertyBuilder()
          .configKey(ComputeInstanceTemplate.ComputeInstanceTemplateConfigurationPropertyToken.IMAGE.unwrap().getConfigKey())
          .name("Original VM")
          .defaultDescription(
                  "The orginal vm template, you need to give the template vm full name(DATACENTER_NAME/vm/VM_NAME) in VC inventory")
          .defaultValue("default")
          .required(false)
          .build()),

  HA_TYPE(new SimpleConfigurationPropertyBuilder()
          .configKey("haType")
          .name("HA type")
          .required(false)
          .widget(ConfigurationProperty.Widget.OPENLIST)
          .defaultDescription(
                  "Select HA type as \"OFF\" , \"ON\" or \"FT\"")
          .addValidValues(
          "OFF",
          "ON",
          "FT")
          .defaultValue("OFF")
          .build());

  private final ConfigurationProperty configurationProperty;

  private VSphereComputeInstanceTemplateConfigurationPropertyToken(ConfigurationProperty configurationProperty) {
    this.configurationProperty = configurationProperty;
  }

  @Override
  public ConfigurationProperty unwrap() {
    return configurationProperty;
  }
}
