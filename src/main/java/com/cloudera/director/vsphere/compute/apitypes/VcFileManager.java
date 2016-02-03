/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.vmware.vim25.mo.VirtualMachine;


/**
 * @author chiq
 *
 */
public class VcFileManager {
   public static String getDsPath(VirtualMachine vm, DatastoreResource ds, String name) {
      if (ds == null) {
         return getDsPath(vm, name);
      } else {
         try {
            return String.format("[%s]", ds.getName());
         }
         catch (Exception ex) {
//            throw BaseVMException.INVALID_FILE_PATH(ex, vm.getPathName());
            return null;
         }
      }
   }

   /**
    * Get the datastore path for a file under the VM directory.
    * @param vm virtual machine object
    * @param name file name
    * @return
    */
   public static String getDsPath(VirtualMachine vm, String name) {
//      return String.format("%s/%s", vm.get, name);
      return null;
   }
}
