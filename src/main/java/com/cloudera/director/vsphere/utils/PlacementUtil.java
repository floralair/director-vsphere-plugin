/**
 *
 */
package com.cloudera.director.vsphere.utils;


public class PlacementUtil {
   public static final String LOCAL_DATASTORE_TYPE = "LOCAL";
   public static final String SHARED_DATASTORE_TYPE = "SHARED";

   public static final String NIC_LABLE = "Network Adapter 1";
   public static final String OS_DISK = "OS.vmdk";
   public static final String SWAP_DISK = "SWAP.vmdk";
   public static final String DATA_DISK = "DATA.vmdk";
   public static final String DATA_DISK_1 = "DATA1.vmdk";
   public static final String DATA_DISK_2 = "DATA2.vmdk";

   public static final String LSI_CONTROLLER_EXTERNAL_ADDRESS_SWAP = "VirtualLsiLogicController:0:1";

   public static final String LSI_CONTROLLER_EXTERNAL_ADDRESS_PREFIX = "VirtualLsiLogicController:1:";

   public static final int CONTROLLER_RESERVED_CHANNEL = 7;
   public static final String[] PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES = {
      "ParaVirtualSCSIController:1:",
      "ParaVirtualSCSIController:2:",
      "ParaVirtualSCSIController:3:"
   };

   public static int getNextValidParaVirtualScsiIndex(int paraVirtualScsiIndex) {
      paraVirtualScsiIndex ++;
      int diskIndex = paraVirtualScsiIndex /
            PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES.length;
      // controller reserved channel, *:7, cannot be used by custom disk
      if (diskIndex == CONTROLLER_RESERVED_CHANNEL) {
         paraVirtualScsiIndex +=
               PlacementUtil.PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES.length;
      }
      return paraVirtualScsiIndex;
   }

   public static String getParaVirtualAddress(int paraVirtualScsiIndex) {
      int arrayIndex = paraVirtualScsiIndex %
            PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES.length;
      int diskIndex = paraVirtualScsiIndex /
            PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES.length;
      return PARA_VIRTUAL_SCSI_EXTERNAL_ADDRESS_PREFIXES[arrayIndex] + diskIndex;
   }

   public static String getSwapAddress() {
      return LSI_CONTROLLER_EXTERNAL_ADDRESS_SWAP;
   }
}
