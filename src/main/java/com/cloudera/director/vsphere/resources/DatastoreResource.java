/**
 *
 */
package com.cloudera.director.vsphere.resources;

import java.util.Set;

import com.cloudera.director.vsphere.utils.VsphereDirectorAssert;
import com.vmware.vim25.ManagedObjectReference;

/**
 * @author chiq
 *
 */
public class DatastoreResource {

   private String name;
   private long freeSpace;
   private ManagedObjectReference mor;
   private String type;
   private Set<String> hostMounts;

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

}
