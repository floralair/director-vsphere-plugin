/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.rmi.RemoteException;
import java.util.List;

import com.cloudera.director.vsphere.compute.apitypes.Group;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.service.intf.IPlacementPlanner;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;

/**
 * @author chiq
 *
 */
public class PlacementPlanner implements IPlacementPlanner {

   private Group group;
   private List<HostResource> hostResources;

   public PlacementPlanner(Folder rootFolder, Group group) throws InvalidProperty, RuntimeFault, RemoteException {
      this.group = group;
      HostResourceManager hostResourceManager = new HostResourceManager(rootFolder);
      this.hostResources = hostResourceManager.filterHostsByNetwork(group.getNetworkName());
   }

   /**
    * @return the group
    */
   public Group getGroup() {
      return group;
   }

   /**
    * @param group the group to set
    */
   public void setGroup(Group group) {
      this.group = group;
   }

   /**
    * @return the hostResources
    */
   public List<HostResource> getHostResources() {
      return hostResources;
   }

   /**
    * @param hostResources the hostResources to set
    */
   public void setHostResources(List<HostResource> hostResources) {
      this.hostResources = hostResources;
   }

   @Override
   public void init() throws Exception {
      for (Node node : group.getNodes()) {
         DatastoreResource targetDatastoreResource = null;
         HostResource targetHostResource = null;
         long nodeStorageUsage = group.getTemplateStorageUsage() + node.getDataDiskSizeGB() + node.getSwapDiskSizeGB();
         for (HostResource hostResource : hostResources) {
            targetDatastoreResource = getFreeDatastoreResource(hostResource, nodeStorageUsage);
            targetHostResource = hostResource;

            if (targetDatastoreResource != null) {
               break;
            }
         }

         if (targetDatastoreResource == null) {
            throw new VsphereDirectorException("There is no enough storage to provision this cluster.");
         }

         node.setTargetDatastore(targetDatastoreResource.getMor());
         node.setTargetDatastoreName(targetDatastoreResource.getName());
         targetDatastoreResource.setFreeSpace(targetDatastoreResource.getFreeSpace() - nodeStorageUsage);

         node.setTargetHost(targetHostResource.getMor());
         node.setTargetHostName(targetHostResource.getName());

         node.setTargetPool(targetHostResource.getCluster().getPool().getMor());
         node.setTargetPoolName(targetHostResource.getCluster().getPool().getName());
      }
   }

   private DatastoreResource getFreeDatastoreResource(HostResource hostResource, long size) throws Exception {
      for(DatastoreResource datastoreResource : hostResource.getDatastores()) {
         if(datastoreResource.getFreeSpace() > size) {
            return datastoreResource;
         }
      }
      return null;
   }
}
