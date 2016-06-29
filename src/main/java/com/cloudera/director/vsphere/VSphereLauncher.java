
package com.cloudera.director.vsphere;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.spi.v1.common.http.HttpProxyParameters;
import com.cloudera.director.spi.v1.model.Configured;
import com.cloudera.director.spi.v1.provider.CloudProvider;
import com.cloudera.director.spi.v1.provider.util.AbstractLauncher;
import com.cloudera.director.vsphere.resourcesplacement.ResourcesPlacement;
import com.cloudera.director.vsphere.resourcesplacement.VcServer;
import com.cloudera.director.vsphere.utils.CommonUtil;

@SuppressWarnings("PMD.UnusedFormalParameter")
public class VSphereLauncher extends AbstractLauncher {
   private static final Logger logger = LoggerFactory.getLogger(VSphereLauncher.class);

   private File configurationDirectory;

  public VSphereLauncher() {
    super(Collections.singletonList(VSphereProvider.METADATA), null);
  }

  @Override
  public void initialize(File configurationDirectory, HttpProxyParameters httpProxyParameters) {
     this.configurationDirectory = configurationDirectory;
  }

  @Override
  public CloudProvider createCloudProvider(String cloudProviderId, Configured ignored, Locale locale) {
    if (!VSphereProvider.ID.equals(cloudProviderId)) {
      throw new NoSuchElementException("Cloud provider not found: " + cloudProviderId);
    }

    ResourcesPlacement.setConfigurationDirectory(this.configurationDirectory);
    String jsonString = ResourcesPlacement.init();
    ResourcesPlacement resourcesPlacement = CommonUtil.jsonToObject(ResourcesPlacement.class, jsonString);
    if (resourcesPlacement == null) {
       resourcesPlacement = new ResourcesPlacement();
       List<VcServer> vcServers = new ArrayList<VcServer>();
       resourcesPlacement.setVcServers(vcServers);
    }

    return new VSphereProvider(resourcesPlacement, getLocalizationContext(locale));
  }
}
