/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

public enum DiskType {
   SYSTEM_DISK("root.vmdk", "OS"), SWAP_DISK("swap.vmdk", "SWAP"), DATA_DISK(
         "data.vmdk", "DATA");

   public String diskName;
   public String type;

   private DiskType(String diskName, String type) {
      this.diskName = diskName;
      this.type = type;
   }

   public String getDiskName() {
      return diskName;
   }

   public String getType() {
      return type;
   }

   public static DiskType getDiskType(String type) {
      if (SYSTEM_DISK.getType().equals(type)) {
         return SYSTEM_DISK;
      } else if (SWAP_DISK.getType().equals(type)) {
         return SWAP_DISK;
      } else if (DATA_DISK.getType().equals(type)) {
         return DATA_DISK;
      } else
         return null;
   }

}