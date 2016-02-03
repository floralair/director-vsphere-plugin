/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

public interface IVmDiskOperationService {

   /**
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    * @throws Exception
    */
   void addDataDisk(String targetDatastoreName, long diskSize, String diskMode) throws Exception;

   /**
    * @param diskName
    * @throws Exception
    */
   void removeDisk(String diskName) throws Exception;

   /**
    * @param targetDatastoreName
    * @param diskSize
    * @param diskMode
    * @throws Exception
    */
   void addSwapDisk(String targetDatastoreName, long diskSize, String diskMode) throws Exception;

}
