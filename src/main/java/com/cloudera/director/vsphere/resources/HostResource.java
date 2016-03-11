/**
 *
 */
package com.cloudera.director.vsphere.resources;

import java.util.List;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.vmware.vim25.ManagedObjectReference;

/**
 * @author chiq
 *
 */
public class HostResource {

   private String name;
   private ManagedObjectReference mor;
   private List<DatastoreResource> datastores;
   private List<NetworkResource> networks;
   private ClusterResource cluster;
   private int nodesCount = 0;
   private DatastoreResource nodeTargetDatastore;

   /**
    * @return the cluster
    */
   public ClusterResource getCluster() {
      return cluster;
   }

   /**
    * @param cluster the cluster to set
    */
   public void setCluster(ClusterResource cluster) {
      this.cluster = cluster;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the mor
    */
   public ManagedObjectReference getMor() {
      return mor;
   }

   /**
    * @param mor the mor to set
    */
   public void setMor(ManagedObjectReference mor) {
      this.mor = mor;
   }

   /**
    * @return the datastores
    */
   public List<DatastoreResource> getDatastores() {
      return datastores;
   }

   /**
    * @param datastores the datastores to set
    */
   public void setDatastores(List<DatastoreResource> datastores) {
      this.datastores = datastores;
   }

   /**
    * @return the networks
    */
   public List<NetworkResource> getNetworks() {
      return networks;
   }

   /**
    * @param networks the networks to set
    */
   public void setNetworks(List<NetworkResource> networks) {
      this.networks = networks;
   }

   /**
    * @return the nodesCount
    */
   public int getNodesCount() {
      return nodesCount;
   }

   /**
    * @param nodesCount the nodesCount to set
    */
   public void setNodesCount(int nodesCount) {
      this.nodesCount = nodesCount;
   }

   /**
    * @return the nodeTargetDatastore
    */
   public DatastoreResource getNodeTargetDatastore() {
      return nodeTargetDatastore;
   }

   /**
    * @param nodeTargetDatastore the nodeTargetDatastore to set
    */
   public void setNodeTargetDatastore(DatastoreResource nodeTargetDatastore) {
      this.nodeTargetDatastore = nodeTargetDatastore;
   }

   public DatastoreResource getDatastore(String name) {
      for (DatastoreResource datastore : this.datastores) {
         if (name.equals(datastore.getName())) {
            return datastore;
         }
      }
      return null;
   }

   public DatastoreResource getTargetDatastore(Node node) {
      DatastoreResource targetDatastore = null;
      for (DatastoreResource datastore : this.datastores) {
         if (!datastore.canBeAllocated(node) || datastore.getFreeSpace() < node.getTotalDiskSize()) {
            continue;
         }

         if (targetDatastore == null) {
            targetDatastore = datastore;
         } else {
             if (targetDatastore.getNodesCount() > datastore.getNodesCount()) {
                targetDatastore = datastore;
             }
         }
      }
      this.nodeTargetDatastore = targetDatastore;
      return targetDatastore;
   }

}
