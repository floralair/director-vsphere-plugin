/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.cloudera.director.vsphere.resources.ClusterResource;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.resources.NetworkResource;
import com.cloudera.director.vsphere.resources.PoolResource;
import com.cloudera.director.vsphere.service.intf.IHostResourceManager;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;

/**
 * @author chiq
 *
 */
public class HostResourceManager implements IHostResourceManager {
   private final Folder rootFolder;

   public HostResourceManager(Folder rootFolder) {
      this.rootFolder = rootFolder;
   }

   /**
    * @return the rootFolder
    */
   public Folder getRootFolder() {
      return rootFolder;
   }

   @Override
   public List<HostResource> filterHostsByNetwork(String networkName) throws InvalidProperty, RuntimeFault, RemoteException {
      List<HostResource> filteredHosts = new ArrayList<HostResource>();

      if (networkName == null || networkName.isEmpty()) {
         return filteredHosts;
      }

      ManagedEntity[] managedEntities = new InventoryNavigator(rootFolder).searchManagedEntities(new String[][] { {"HostSystem", "name" }, }, true);
      for(ManagedEntity managedEntity : managedEntities) {
         HostSystem hostSystem = new HostSystem(rootFolder.getServerConnection(), managedEntity.getMOR());

         HostResource hostResource = new HostResource();
         hostResource.setName(hostSystem.getName());
         hostSystem.getMOR();
         if (ManagedEntityStatus.red.equals(hostSystem.getConfigStatus())) {
            continue;
         }

         List<NetworkResource> networkResources = new ArrayList<NetworkResource>();
         Network[] networks = hostSystem.getNetworks();
         for (Network network : networks) {
            if (networkName.equals(network.getName())) {
               NetworkResource networkResource = new NetworkResource();
               networkResource.setName(network.getName());
               networkResources.add(networkResource);
            }
         }
         hostResource.setNetworks(networkResources);

         List<DatastoreResource> datastoreResources = new ArrayList<DatastoreResource>();
         Datastore[] datastores = hostSystem.getDatastores();
         for (Datastore datastore : datastores) {
            DatastoreResource datastoreResource = new DatastoreResource();
            datastoreResource.setName(datastore.getName());
            datastoreResource.setFreeSpace(datastore.getSummary().getFreeSpace() / 1024 / 1024 / 1024);
            datastoreResource.setMor(datastore.getMOR());
            datastoreResources.add(datastoreResource);
         }
         hostResource.setDatastores(datastoreResources);
         hostResource.setMor(hostSystem.getMOR());

         ClusterComputeResource clusterComputeResource = new ClusterComputeResource(rootFolder.getServerConnection(), hostSystem.getParent().getMOR());
         ClusterResource clusterResource = new ClusterResource();
         clusterResource.setName(clusterComputeResource.getName());
         clusterResource.setMor(clusterComputeResource.getMOR());

         PoolResource poolResource = new PoolResource();
         poolResource.setName(clusterComputeResource.getResourcePool().getName());
         poolResource.setMor(clusterComputeResource.getResourcePool().getMOR());
         clusterResource.setPool(poolResource);

         hostResource.setCluster(clusterResource);

         filteredHosts.add(hostResource);
      }

      return filteredHosts;
   }

}
