/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.intf;

import com.vmware.vim25.mo.VirtualMachine;

public interface IVmService {

   /**
    * @param vmName
    * @param cloneName
    * @param numCPUs
    * @param memoryMB
    */
   void clone(String vmName, String cloneName, String numCPUs, String memoryMB);

   /**
    * @param vmName
    * @param operation
    */
   void powerOps(String vmName, String operation);

   /**
    * @param vmName
    * @return
    */
   String getIpaddress(String vmName);

   /**
    * @param vmName
    * @param diskSize
    * @param diskMode
    */
   void addSwapDisk(String vmName, int diskSize, String diskMode);

   /**
    * @param vmName
    * @param diskSize
    * @param diskMode
    */
   void addDataDisk(String vmName, int diskSize, String diskMode);

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
   void nicOps(String vmName, String operation, String netName);

}
