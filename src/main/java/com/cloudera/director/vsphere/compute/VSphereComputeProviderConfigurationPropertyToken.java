package com.cloudera.director.vsphere.compute;

import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.util.SimpleConfigurationPropertyBuilder;

// Fully qualifying class name due to compiler bug
public enum VSphereComputeProviderConfigurationPropertyToken
        implements com.cloudera.director.spi.v1.model.ConfigurationPropertyToken {

    /**
     * @see com.cloudera.director.vsphere.util.HostGroups#expand(String)
     */
    VC_SERVER(new SimpleConfigurationPropertyBuilder()
            .configKey("vCenter Server")
            .name("Server")
            .required(true)
            .defaultDescription("A vCenter server hostname or ipaddress " +
                    "used for allocations. On termination allocated hosts are not returned to the pool.")
            .build()),

    /**
     * @see com.cloudera.director.vsphere.util.HostGroups#expand(String)
     */
    VC_SERVER_PORT(new SimpleConfigurationPropertyBuilder()
            .configKey("vCenter Server port")
            .name("Port")
            .defaultValue("443")
            .required(true)
            .defaultDescription("The port of  vCenter server " +
                    "used for allocations. On termination allocated hosts are not returned to the pool.")
            .build()),

    /**
     * @see com.cloudera.director.vsphere.util.HostGroups#expand(String)
     */
    VC_SERVER_USERNAME(new SimpleConfigurationPropertyBuilder()
            .configKey("vCenter Server Username")
            .name("Username")
            .required(true)
            .defaultDescription("The Username for vCenter server " +
                    "used for allocations.")
            .build()),

    /**
     * @see com.cloudera.director.vsphere.util.HostGroups#expand(String)
     */
    VC_SERVER_PASSWORD(new SimpleConfigurationPropertyBuilder()
            .configKey("vCenter Server Password")
            .name("Password")
            .widget(ConfigurationProperty.Widget.PASSWORD)
            .required(true)
            .defaultDescription("The Password for vCenter server " +
                    "used for allocations.")
            .build());

    /**
     * The configuration property.
     */
    private final ConfigurationProperty configurationProperty;

    private VSphereComputeProviderConfigurationPropertyToken(ConfigurationProperty configurationProperty) {
        this.configurationProperty = configurationProperty;
    }

    @Override
    public ConfigurationProperty unwrap() {
        return configurationProperty;
    }
}
