/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

public interface IVmDiskOperationService {

   /**
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    */
   void addDataDisk(String targetDatastoreName, long diskSize, String diskMode);

   /**
    * @param diskName
    */
   void removeDisk(String diskName);

   /**
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    */
   void addSwapDisk(String targetDatastoreName, long diskSize, String diskMode);

}
