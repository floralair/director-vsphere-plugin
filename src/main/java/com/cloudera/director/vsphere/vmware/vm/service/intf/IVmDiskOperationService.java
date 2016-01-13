/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.intf;

public interface IVmDiskOperationService {

   /**
    * @param diskSize
    * @param diskMode
    */
   void addDataDisk(int diskSize, String diskMode);

   /**
    * @param diskName
    */
   void removeDisk(String diskName);

   /**
    * @param diskSize
    * @param diskMode
    */
   void addSwapDisk(int diskSize, String diskMode);

}
