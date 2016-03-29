/**
 *
 */
package com.cloudera.director.vsphere.resourcesplacement;

import java.util.ArrayList;
import java.util.List;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.google.gson.annotations.Expose;

/**
 * @author chiq
 *
 */
public class VcHost {

   @Expose
   private String name;

   @Expose
   private int nodesCount = 0;

   @Expose
   private List<VcDatastore> vcDatastores = new ArrayList<VcDatastore>();

   private VcDatastore nodeTargetDatastore;

   /**
    * @param name
    */
   public VcHost(String name) {
      this.name = name;
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
    * @return the vcDatastores
    */
   public List<VcDatastore> getVcDatastores() {
      return vcDatastores;
   }

   /**
    * @param vcDatastores the vcDatastores to set
    */
   public void setVcDatastores(List<VcDatastore> vcDatastores) {
      this.vcDatastores = vcDatastores;
   }

   /**
    * @return the nodeTargetDatastore
    */
   public VcDatastore getNodeTargetDatastore() {
      return nodeTargetDatastore;
   }

   /**
    * @param nodeTargetDatastore the nodeTargetDatastore to set
    */
   public void setNodeTargetDatastore(VcDatastore nodeTargetDatastore) {
      this.nodeTargetDatastore = nodeTargetDatastore;
   }

   public VcDatastore getTargetDatastore(Node node) {
      VcDatastore targetDatastore = null;
      for (VcDatastore datastore : this.vcDatastores) {
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

   public List<String> getDataStoreNames() {
      List<String> datastoreNames = new ArrayList<String>();
      for (VcDatastore datastore : this.vcDatastores) {
         datastoreNames.add(datastore.getName());
      }
      return datastoreNames;
   }

   public void updateNodesCount() {
      if (this.nodesCount > 0) {
         this.nodesCount -= 1;
      }
   }

   public VcDatastore getVcDatastoreByName(String vcDatastoreName) {
      for (VcDatastore vcDatastore : this.vcDatastores) {
         if (vcDatastoreName.equals(vcDatastore.getName())) {
            return vcDatastore;
         }
      }
      return null;
   }
}
