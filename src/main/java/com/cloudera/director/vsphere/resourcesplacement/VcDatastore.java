/**
 *
 */
package com.cloudera.director.vsphere.resourcesplacement;

import java.util.HashSet;
import java.util.Set;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.utils.VsphereDirectorAssert;
import com.google.gson.annotations.Expose;

/**
 * @author chiq
 *
 */
public class VcDatastore {

   @Expose
   private String name;

   @Expose
   private int nodesCount = 0;

   @Expose
   private long freeSpace;

   @Expose
   private String type;

   @Expose
   private Set<String> hostMounts = new HashSet<String>();

   public VcDatastore(String name, long freeSpace, String type, Set<String> hostMounts) {
      this.name = name;
      this.freeSpace = freeSpace;
      this.type = type;
      this.hostMounts = hostMounts;
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
    * @return the freeSpace
    */
   public long getFreeSpace() {
      return freeSpace;
   }

   /**
    * @param freeSpace the freeSpace to set
    */
   public void setFreeSpace(long freeSpace) {
      this.freeSpace = freeSpace;
   }

   /**
    * @return the type
    */
   public String getType() {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(String type) {
      this.type = type;
   }

   /**
    * @return the hostMounts
    */
   public Set<String> getHostMounts() {
      return hostMounts;
   }

   /**
    * @param hostMounts the hostMounts to set
    */
   public void setHostMounts(Set<String> hostMounts) {
      this.hostMounts = hostMounts;
   }

   // TODO Need to consider there is only one datastoreHostMount on shared storage
   public boolean isLocalStorage() {
      return ("VMFS").equalsIgnoreCase(this.type) && this.hostMounts.size() == 1;
   }

   public boolean isSharedStorage() {
      return !isLocalStorage();
   }

   public void allocate(long sizeGB) {
      VsphereDirectorAssert.check(this.freeSpace - sizeGB >= 0);
      this.freeSpace -= sizeGB;
   }

   public boolean canBeAllocated(Node node) {
      boolean canBeAllocated = false;
      if (node.needLocalStorage() && isLocalStorage() && this.freeSpace >= node.getTotalDiskSize()) {
         canBeAllocated = true;
      }
      if (node.needSharedStorage() && isSharedStorage() && this.freeSpace >= node.getTotalDiskSize()) {
         canBeAllocated = true;
      }
      return canBeAllocated;
   }

   public void update(DatastoreResource datastoreResource) {
      this.freeSpace = datastoreResource.getFreeSpace();
      this.type = datastoreResource.getType();
      this.hostMounts = datastoreResource.getHostMounts();
   }

   public void updateNodesCount() {
      if (this.nodesCount > 0) {
         this.nodesCount -= 1;
      }
   }
}
