/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.resources.ClusterResource;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.resources.NetworkResource;
import com.cloudera.director.vsphere.resources.PoolResource;
import com.cloudera.director.vsphere.service.intf.IHostResourceManager;
import com.vmware.vim25.DatastoreHostMount;
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
         throw new VsphereDirectorException("The network name is empty, please give the network name.");
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
               networkResource.setMor(network.getMOR());
               networkResources.add(networkResource);
            }
         }

         if (networkResources.isEmpty()) {
            throw new VsphereDirectorException("There is no network named " + networkName +" in the vCenter environment.");
         }

         hostResource.setNetworks(networkResources);

         List<DatastoreResource> datastoreResources = new ArrayList<DatastoreResource>();
         Datastore[] datastores = hostSystem.getDatastores();
         for (Datastore datastore : datastores) {
            DatastoreResource datastoreResource = new DatastoreResource();
            datastoreResource.setName(datastore.getName());
            datastoreResource.setFreeSpace(datastore.getSummary().getFreeSpace() / 1024 / 1024 / 1024);
            datastoreResource.setMor(datastore.getMOR());
            datastoreResource.setType(datastore.getSummary().getType());
            Set<String> hostMounts = new HashSet<String>();
            for (DatastoreHostMount datastoreHostMount : datastore.getHost()) {
               HostSystem hostMount = new HostSystem(rootFolder.getServerConnection(), datastoreHostMount.getKey());
               hostMounts.add(hostMount.getName());
            }
            datastoreResource.setHostMounts(hostMounts);
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

      if (filteredHosts.isEmpty()) {
         throw new VsphereDirectorException("There is no host which using the network named " + networkName + ".");
      }

      return filteredHosts;
   }

}
