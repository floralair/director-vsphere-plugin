/**
 *
 */
package com.cloudera.director.vsphere.resources;

import com.vmware.vim25.ManagedObjectReference;

/**
 * @author chiq
 *
 */
public class ClusterResource {

   private String name;
   private ManagedObjectReference mor;
   private PoolResource pool;

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
    * @return the pool
    */
   public PoolResource getPool() {
      return pool;
   }

   /**
    * @param pool the pool to set
    */
   public void setPool(PoolResource pool) {
      this.pool = pool;
   }

}
