/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

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
    */
   void addSwapDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode);

   /**
    * @param vmName
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    */
   void addDataDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode);

   /**
    * @param vmName
    * @return
    */
   VirtualMachine getVirtualMachine(String vmName);

   /**
    * @param vmName
    * @param operation
    * @param netName
    */
   void nicOps(String vmName, String operation, String netName, String newNetwork) throws Exception;

   /**
    * @param vmName
    * @return
    */
   long getTemplateStorageUsage(String vmName);

}
