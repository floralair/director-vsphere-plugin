/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.resources.VcNetwork;
import com.vmware.vim25.ManagedObjectReference;

public interface IVmService {

   /**
    * @param vmName
    * @param cloneName
    * @param numCPUs
    * @param memoryMB
    * @param targetDatastore
    * @param targetHost
    * @param targetPool
    * @return
    * @throws Exception
    */
   boolean clone(String vmName, String cloneName, int numCPUs, long memoryMB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) throws Exception;

   /**
    * @param vmName
    * @param operation
    * @throws Exception
    */
   void powerOps(String vmName, String operation) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   String getIpAddress(String vmName) throws Exception;

   /**
    * @param vmName
    * @param operation
    * @param netName
    */
   void nicOps(String vmName, String operation, VcNetwork vcNet, String netName, String newNetwork) throws Exception;

   /**
    * @param node
    */

   public void configNetworks(Node node) throws Exception;

   /**
    * @param node
    * @throws Exception
    */
   void configureVm(Node node) throws Exception;

}
