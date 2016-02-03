/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.utils.VmConfigUtil;
import com.cloudera.director.vsphere.utils.VsphereDirectorAssert;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskMode;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class DiskCreateSpec {
   DeviceId deviceId;
   DatastoreResource ds = null;
   String diskName;
   VirtualDiskMode diskMode;
   DiskSize size = null;
   AllocationType allocationType;

   public DiskCreateSpec(DeviceId deviceId, DatastoreResource ds, String diskName,
         VirtualDiskMode diskMode, DiskSize size, AllocationType allocationType) {
      this.deviceId = deviceId;
      this.ds = ds;
      this.diskName = diskName;
      this.diskMode = diskMode;
      this.size = size;
      this.allocationType = allocationType;
      VsphereDirectorAssert.check(deviceId != null);
   }

   public DiskCreateSpec(DeviceId deviceId, DatastoreResource ds, String diskName,
         VirtualDiskMode diskMode, DiskSize size) {
      this(deviceId, ds, diskName, diskMode, size, AllocationType.THIN);
   }

   public VirtualDeviceConfigSpec getVcSpec(int key, VirtualMachine vm) throws Exception {
      Boolean thinDisk = null;
      Boolean eagerlyScrub = null;
      if (allocationType != null) {
         switch (allocationType) {
         case THIN:
            thinDisk = Boolean.TRUE;
            break;
         case ZEROEDTHICK:
            eagerlyScrub = Boolean.TRUE;
         case THICK:
            thinDisk = Boolean.FALSE;
            break;
         }
      }
      VirtualDiskFlatVer2BackingInfo backing = VmConfigUtil.createVmdkBackingInfo(vm, ds, diskName, diskMode, thinDisk, eagerlyScrub);
      return VmConfigUtil.attachVirtualDiskSpec(key, vm, deviceId, backing, true, size);
   }

   @Override
   public String toString() {
      return new ToStringBuilder("DiskCreateSpec")
            .append("name",diskName)
            .append("id",deviceId)
            .append("size",size)
            .append("ds",ds.getName())
            .toString();
   }
}
