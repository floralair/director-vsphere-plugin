/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

import java.util.Map;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.resources.VcNetwork;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.VirtualMachine;

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
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    * @throws Exception
    */
   void addSwapDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) throws Exception;

   /**
    * @param vmName
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    * @throws Exception
    */
   void addDataDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   VirtualMachine getVirtualMachine(String vmName) throws Exception;

   /**
    * @param vmName
    * @param operation
    * @param netName
    */
   void nicOps(String vmName, String operation, VcNetwork vcNet, String netName, String newNetwork) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   long getTemplateStorageUsage(String vmName) throws Exception;

   /**
    * @param node
    */

   public void configNetworks(Node node) throws Exception;

   /**
    * @param vmName
    * @param guestVariables
    */
   void setMachineIdVariables(String vmName, Map<String, Object> guestVariables) throws Exception;

   /**
    * @param node
    * @throws Exception
    */
   void configureVm(Node node) throws Exception;

}
