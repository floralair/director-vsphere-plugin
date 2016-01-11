/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.intf;

public interface IVmDiskOperationService {

   /**
    * @param diskSize
    * @param diskMode
    */
   void addDisk(int diskSize, String diskMode);

   /**
    * @param diskName
    */
   void removeDisk(String diskName);

}
