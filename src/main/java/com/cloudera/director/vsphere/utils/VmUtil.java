/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.util.ArrayList;
import java.util.List;

import com.cloudera.director.vsphere.compute.apitypes.DeviceId;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema.Disk;
import com.cloudera.director.vsphere.compute.apitypes.DiskType;
import com.google.gson.Gson;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmUtil {

   public static VirtualMachine getVirtualMachine(ServiceInstance serviceInstance, String vmName) throws Exception {
      VirtualMachine vm = (VirtualMachine) new InventoryNavigator(serviceInstance.getRootFolder()).searchManagedEntity("VirtualMachine", vmName);
      return vm;
   }

   public static String getVolumes(VirtualMachine vm, List<Disk> disks) {
      final List<String> volumes = new ArrayList<String>();
      if (disks != null && !disks.isEmpty()) {
         for (DiskSchema.Disk disk : disks) {
            if (DiskType.DATA_DISK.getType().equals(disk.type) || DiskType.SWAP_DISK.getType().equals(disk.type)) {
               volumes.add(disk.type + ":" + VmUtil.fetchDiskUUID(vm, disk.externalAddress));
            }
         }
      }
      return (new Gson()).toJson(volumes);
   }

   public static String fetchDiskUUID(final VirtualMachine vm, final String diskExtAddress) {

      VirtualDisk vDisk = findVirtualDisk(vm, diskExtAddress);
      if (vDisk == null) return null;

      VirtualDiskFlatVer2BackingInfo backing = (VirtualDiskFlatVer2BackingInfo) vDisk.getBacking();
      return backing.getUuid();
   }

   public static VirtualDisk findVirtualDisk(VirtualMachine vm, String externalAddr) {
      if (vm == null) return null;

      DeviceId diskId = new DeviceId(externalAddr);
      VirtualDevice device = VmConfigUtil.getVirtualDevice(vm, diskId);
      if (device == null)
         return null;

      VsphereDirectorAssert.check(device instanceof VirtualDisk);
      return (VirtualDisk) device;
   }

}
