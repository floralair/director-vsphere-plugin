/**
 *
 */
package com.cloudera.director.vsphere.resourcesplacement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.utils.CommonUtil;
import com.google.gson.annotations.Expose;

/**
 * @author chiq
 *
 */
public class ResourcesPlacement {
   static final Logger logger = Logger.getLogger(ResourcesPlacement.class);

   private static final String PLACEMENT_FILE_NAME = "resources-placement";
   private static File configurationDirectory;
   private static File placementFile;

   @Expose
   private List<VcServer> vcServers = new ArrayList<VcServer>();

   /**
    * @param configurationDirectory
    */
   public ResourcesPlacement() {

   }

   /**
    * @return the pLACEMENT_FILE_NAME
    */
   public String getPLACEMENT_FILE_NAME() {
      return PLACEMENT_FILE_NAME;
   }

   /**
    * @return the configurationDirectory
    */
   public File getConfigurationDirectory() {
      return configurationDirectory;
   }

   /**
    * @param configurationDirectory the configurationDirectory to set
    */
   public static void setConfigurationDirectory(File configurationDirectory) {
      ResourcesPlacement.configurationDirectory = configurationDirectory;
   }

   /**
    * @return the vcServers
    */
   public List<VcServer> getVcServers() {
      return vcServers;
   }

   /**
    * @param vcServers the vcServers to set
    */
   public void setVcServers(List<VcServer> vcServers) {
      this.vcServers = vcServers;
   }

   public static String init() {
      placementFile = new File(configurationDirectory, PLACEMENT_FILE_NAME);
      if(!placementFile.exists()) {
         try {
            placementFile.createNewFile();
            CommonUtil.prettyOutputStrings(CommonUtil.objectToJson(new ResourcesPlacement()), placementFile.getPath(), null);
         } catch (Exception e) {
            throw new VsphereDirectorException("The placement file " + placementFile.getPath() + " can not be created");
         }
      }
      return CommonUtil.readJsonFile(placementFile.getPath());
   }

   public boolean containsVcServer(String vcServerName) {
      boolean containsVcServer = false;
      for (VcServer vcServer : vcServers) {
         if (vcServerName.equals(vcServer.getName())) {
            containsVcServer = true;
            break;
         }
      }
      return containsVcServer;
   }

   public void update(String vcServerName) {
      update(vcServerName, null);
   }

   public void update(String vcServerName, List<HostResource> hostResources) {
      if (hostResources != null) {
         if (!containsVcServer(vcServerName)) {
            createVcServer(vcServerName, hostResources);
         } else {
            updateVcServer(vcServerName, hostResources);
         }
      }

      try {
         CommonUtil.prettyOutputStrings(CommonUtil.objectToJson(this), placementFile.getPath(), null);
      } catch (Exception e) {
         throw new VsphereDirectorException("Update the placement file " + placementFile.getPath() + " content failed.");
      }
   }

   /**
    * @param vcServerName
    * @param hostResources
    */
   private void createVcServer(String vcServerName, List<HostResource> hostResources) {
      VcServer vcServer = getVcServerByName(vcServerName);

      List<VcHost> vcHosts = vcServer.getVcHosts();
      for (HostResource hostResource : hostResources) {
         vcHosts.add(createVcHost(hostResource));
      }
      vcServer.setVcHosts(vcHosts);

      this.vcServers.add(vcServer);
   }

   /**
    * @param vcServer
    * @param hostResources
    */
   private void updateVcServer(String vcServerName, List<HostResource> hostResources) {
      VcServer vcServer = getVcServerByName(vcServerName);

      List existedHosts = getHostNamesByVcServer(vcServerName);
      List<VcHost> vcHosts = vcServer.getVcHosts();
      for (HostResource hostResource : hostResources) {

         if (existedHosts.contains(hostResource.getName())) {
            for (VcHost vcHost : vcHosts) {
               if (vcHost.getName().equals(hostResource.getName())) {
                  updateVcHost(vcHost, hostResource);
               }
            }

         } else {
            vcHosts.add(createVcHost(hostResource));
         }
      }
      vcServer.setVcHosts(vcHosts);

   }

   /**
    * @param vcServerName
    * @return
    */
   public VcServer getVcServerByName(String vcServerName) {
      for (VcServer vcServer : vcServers) {
         if (vcServerName.equals(vcServer.getName())) {
            return vcServer;
         }
      }
      return new VcServer(vcServerName);
   }

   /**
    * @param vcServerName
    * @return
    */
   public List<VcHost> getHostsByVcServer(String vcServerName) {
      return getVcServerByName(vcServerName).getVcHosts();
   }

   /**
    * @param vcServerName
    * @return
    */
   public List<String> getHostNamesByVcServer(String vcServerName) {
      List<String> hostNames = new ArrayList<String>();
      for (VcHost vcHost : getHostsByVcServer(vcServerName)) {
         hostNames.add(vcHost.getName());
      }
      return hostNames;
   }

   private VcHost createVcHost(HostResource hostResource) {
      VcHost vcHost = new VcHost(hostResource.getName());

      List<VcDatastore> vcDatastores = vcHost.getVcDatastores();
      for (DatastoreResource datastoreResource : hostResource.getDatastores()) {
         VcDatastore vcDatastore = new VcDatastore(datastoreResource.getName(), datastoreResource.getFreeSpace(), datastoreResource.getType(), datastoreResource.getHostMounts());
         vcDatastores.add(vcDatastore);
         vcHost.setVcDatastores(vcDatastores);
      }
      return vcHost;
   }

   private void updateVcHost(VcHost vcHost, HostResource hostResource) {
      List<String> existedDatastores = vcHost.getDataStoreNames();
      List<VcDatastore> vcDatastores = vcHost.getVcDatastores();

      for (DatastoreResource datastoreResource : hostResource.getDatastores()) {
         if (existedDatastores.contains(datastoreResource.getName())) {
            for (VcDatastore vcDatastore : vcDatastores) {
               if (vcDatastore.getName().equals(datastoreResource.getName())) {
                  vcDatastore.update(datastoreResource);
               }
            }
         } else {
            VcDatastore vcDatastore = new VcDatastore(datastoreResource.getName(), datastoreResource.getFreeSpace(), datastoreResource.getType(), datastoreResource.getHostMounts());
            vcDatastores.add(vcDatastore);
         }
      }
   }

   public void updateNodesCount(String vcServerName, String vcHostName, String vcDatastoreName) {
      VcServer vcServer = getVcServerByName(vcServerName);
      VcHost vcHost = vcServer.getVcHostByName(vcHostName);
      if (vcHost != null) {
         vcHost.updateNodesCount();
         VcDatastore vcDatastore = vcHost.getVcDatastoreByName(vcDatastoreName);
         if (vcDatastore != null) {
            vcDatastore.updateNodesCount();
         }
      }
   }

}
