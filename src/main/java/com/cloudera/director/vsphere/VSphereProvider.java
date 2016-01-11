
package com.cloudera.director.vsphere;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.cloudera.director.spi.v1.model.ConfigurationProperty;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.model.LocalizationContext;
import com.cloudera.director.spi.v1.provider.CloudProviderMetadata;
import com.cloudera.director.spi.v1.provider.ResourceProvider;
import com.cloudera.director.spi.v1.provider.ResourceProviderMetadata;
import com.cloudera.director.spi.v1.provider.util.AbstractCloudProvider;
import com.cloudera.director.spi.v1.provider.util.SimpleCloudProviderMetadataBuilder;
import com.cloudera.director.spi.v1.provider.util.SimpleCredentialsProviderMetadata;
import com.cloudera.director.vsphere.compute.VSphereComputeProvider;

public class VSphereProvider extends AbstractCloudProvider {

  public static final String ID = "vSphere";

  private static final List<ResourceProviderMetadata> RESOURCE_PROVIDER_METADATA =
      Collections.singletonList(VSphereComputeProvider.METADATA);

  protected static final CloudProviderMetadata METADATA = new SimpleCloudProviderMetadataBuilder()
      .id(ID)
      .name("vSphere Platform")
      .description("vSphere Platform provider implementation")
      .configurationProperties(Collections.<ConfigurationProperty>emptyList())
      .credentialsProviderMetadata(new SimpleCredentialsProviderMetadata(Collections.<ConfigurationProperty>emptyList()))
      .resourceProviderMetadata(RESOURCE_PROVIDER_METADATA)
      .build();


  public VSphereProvider(LocalizationContext rootLocalizationContext) {
    super(METADATA, rootLocalizationContext);
  }

  @Override
  public ResourceProvider createResourceProvider(
      String resourceProviderId, Configured configuration) {

    if (VSphereComputeProvider.METADATA.getId().equals(resourceProviderId)) {
      return new VSphereComputeProvider(configuration, getLocalizationContext());
    }

    throw new NoSuchElementException(
        String.format("Invalid resource provider ID: %s", resourceProviderId));
  }

}
