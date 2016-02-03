/**
 *
 */
package com.cloudera.director.vsphere.service.intf;

/**
 * @author chiq
 *
 */
public interface IPlacementPlanner {

   /**
    * @throws Exception
    */
   void init() throws Exception;

   /**
    * @throws Exception
    */
   void initNodeDisks() throws Exception;

   /**
    *
    */
   void placeDisk();

}
