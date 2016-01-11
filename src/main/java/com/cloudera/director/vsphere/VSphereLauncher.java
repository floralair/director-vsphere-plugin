
package com.cloudera.director.vsphere;

import java.util.Collections;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.provider.CloudProvider;
import com.cloudera.director.spi.v1.provider.util.AbstractLauncher;

@SuppressWarnings("PMD.UnusedFormalParameter")
public class VSphereLauncher extends AbstractLauncher {

  public VSphereLauncher() {
    super(Collections.singletonList(VSphereProvider.METADATA), null);
  }

  @Override
  public CloudProvider createCloudProvider(String cloudProviderId, Configured ignored, Locale locale) {
    if (!VSphereProvider.ID.equals(cloudProviderId)) {
      throw new NoSuchElementException("Cloud provider not found: " + cloudProviderId);
    }

    return new VSphereProvider(getLocalizationContext(locale));
  }
}
