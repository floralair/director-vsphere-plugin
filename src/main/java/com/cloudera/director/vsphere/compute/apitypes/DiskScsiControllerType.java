/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

public enum DiskScsiControllerType {
   LSI_CONTROLLER("VirtualLsiLogicController"), PARA_VIRTUAL_CONTROLLER("ParaVirtualSCSIController");

   private String displayName;

   private DiskScsiControllerType(String name) {
      this.displayName = name;
   }

   public String getDisplayName() {
      return displayName;
   }
}