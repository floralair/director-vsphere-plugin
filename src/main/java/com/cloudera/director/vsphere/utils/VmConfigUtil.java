/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloudera.director.vsphere.compute.apitypes.DeviceId;
import com.cloudera.director.vsphere.compute.apitypes.DiskSize;
import com.cloudera.director.vsphere.compute.apitypes.VcFileManager;
import com.cloudera.director.vsphere.exception.VcException;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.vmware.vim25.ParaVirtualSCSIController;
import com.vmware.vim25.VirtualController;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDeviceConnectInfo;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualDiskMode;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualLsiLogicSASController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualSCSIController;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmConfigUtil {
   static final Logger logger = Logger.getLogger(VmConfigUtil.class);

   public enum ScsiControllerType {
      PVSCSI(ParaVirtualSCSIController.class),
      LSILOGIC(VirtualLsiLogicController.class),
      LSILOGICSAS(VirtualLsiLogicSASController.class),
      BUSLOGIC(VirtualLsiLogicSASController.class);

      public final Class<? extends VirtualSCSIController> implClass;

      ScsiControllerType(Class<? extends VirtualSCSIController> implClass) {
         this.implClass = implClass;
      }

      VirtualSCSIController createController() {
         try {
            return implClass.newInstance();
         } catch (Exception e) {
            VsphereDirectorAssert.INTERNAL(e);
            return null;
         }
      }

      public static ScsiControllerType findController(Class<? extends VirtualSCSIController> clazz) {
         for (ScsiControllerType type : ScsiControllerType.values()) {
            if (type.implClass == clazz) {
               return type;
            }
         }
         return null;
      }
   }

   public static VirtualDiskFlatVer2BackingInfo createVmdkBackingInfo(
         VirtualMachine vm, DatastoreResource ds, String diskName, VirtualDiskMode diskMode, Boolean thin, Boolean eagerlyScrub) {
      String vmdkPath = VcFileManager.getDsPath(vm, ds, diskName);
      return createVmdkBackingInfo(vmdkPath, diskMode, null, thin, eagerlyScrub);
   }

   public static VirtualDiskFlatVer2BackingInfo createVmdkBackingInfo(String vmdkPath, VirtualDiskMode diskMode,
         VirtualDiskFlatVer2BackingInfo parentBacking, Boolean thinDisk, Boolean eagerlyScrub) {
      VirtualDiskFlatVer2BackingInfo vmdkBacking = new VirtualDiskFlatVer2BackingInfo();
      vmdkBacking.setFileName(vmdkPath);
      vmdkBacking.setDiskMode(diskMode.toString());
      if (parentBacking != null) {
         vmdkBacking.setParent(parentBacking);
      }
      if (thinDisk != null) {
         vmdkBacking.setThinProvisioned(thinDisk);
      }
      if (eagerlyScrub != null) {
         vmdkBacking.setEagerlyScrub(eagerlyScrub);
      }
      return vmdkBacking;
   }

   public static VirtualDeviceConfigSpec attachVirtualDiskSpec(int key, VirtualMachine vm, DeviceId deviceId, VirtualDiskFlatVer2BackingInfo backing, boolean createDisk, DiskSize size) throws Exception {
      VirtualController controller = getVirtualController(vm, deviceId);
      if (controller == null) {
         // Add the controller to the VM if it does not exist
         controller = attachVirtualController(vm, deviceId);
         if (controller == null) {
            throw VcException.CONTROLLER_NOT_FOUND(deviceId.toString());
         }
      }

      VirtualDisk vmdk = VmConfigUtil.createVirtualDisk(controller, deviceId.unitNum,
                                                        backing, size);
      // key is used in ConfigSpec when updating multiple aspects, such as devices
      // The key we specify does not matter, and will get reassigned, so we start with -1
      // and go lower (the system assigned keys are positive). Without specifying the keys,
      // multiple updates in a single call will not work.
      vmdk.setKey(key);
      key--;
      VirtualDeviceConfigSpec spec = new VirtualDeviceConfigSpec();
      spec.setOperation(VirtualDeviceConfigSpecOperation.add);
      if (createDisk) {
         spec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);
      }
      spec.setDevice(vmdk);
      return spec;
   }

   public static VirtualController attachVirtualController(VirtualMachine vm, DeviceId deviceId) throws Exception {
      VmConfigUtil.ScsiControllerType scsiType = VmConfigUtil.ScsiControllerType.findController(deviceId.getTypeClass());
      if (scsiType != null) {
         logger.info("Adding " + deviceId.controllerType + " SCSI controller to VM " + vm.getName() + " at bus " + deviceId.busNum);
      } else {
         logger.error("Unsupported SCSI type creation: " + deviceId.controllerType);
         throw VcException.INTERNAL();
      }

      if (reconfigure(vm, createConfigSpec(VmConfigUtil.createControllerDevice(scsiType, deviceId.busNum)))) {
         return getVirtualController(vm, deviceId);
      } else {
         return null;
      }
   }

   public static boolean reconfigure(VirtualMachine vm, final VirtualMachineConfigSpec spec) throws Exception {
      Task task = vm.reconfigVM_Task(spec);
      return task.SUCCESS.equals(task.waitForMe());
   }

   public static VirtualMachineConfigSpec createConfigSpec(VirtualDeviceConfigSpec... deviceChanges)
         throws Exception {
      VirtualMachineConfigSpec config = new VirtualMachineConfigSpec();
      config.setDeviceChange(deviceChanges);
      return config;
   }

   public static VirtualDeviceConfigSpec createControllerDevice(ScsiControllerType type, int busNum) {
      VirtualSCSIController controller = type.createController();
      controller.setBusNumber(busNum);
      controller.setHotAddRemove(true);
      controller.setKey(-1);
      controller.setSharedBus(VirtualSCSISharing.noSharing);
      return addDeviceSpec(controller);
   }

   public static VirtualDeviceConfigSpec addDeviceSpec(VirtualDevice dev) {
      VirtualDeviceConfigSpec devSpec = new VirtualDeviceConfigSpec();
      devSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
      devSpec.setDevice(dev);
      return devSpec;
   }

   public static VirtualDisk createVirtualDisk(VirtualDevice controller, int unitNum, VirtualDiskFlatVer2BackingInfo backing, DiskSize size) {
      VsphereDirectorAssert.check(controller instanceof VirtualController);

      VirtualDisk vmdk = new VirtualDisk();
      vmdk.setUnitNumber(unitNum);
      vmdk.setControllerKey(controller.getKey());
      if (size != null) {
         vmdk.setCapacityInKB(size.getKiB());
      }
      setVirtualDeviceBacking(vmdk, backing);
      return vmdk;
   }

   public static void setVirtualDeviceBacking(VirtualDevice device, VirtualDiskFlatVer2BackingInfo backing) {
      VirtualDeviceConnectInfo connectInfo = new VirtualDeviceConnectInfo();
      connectInfo.setConnected(true);
      connectInfo.setStartConnected(true);
      device.setBacking(backing);
      device.setConnectable(connectInfo);
   }

   public static VirtualController getVirtualController(VirtualMachine vm, DeviceId deviceId) {
      return findVirtualController(getDevice(vm), deviceId);
   }

   public static List<DeviceId> getVirtualDiskIds(VirtualDevice[] devices) {
      HashMap<Integer, VirtualController> controllers =
         new HashMap<Integer, VirtualController>();
      List<DeviceId> diskIds = new ArrayList<DeviceId>();
      // Find all valid controllers.
      for (VirtualDevice device : devices) {
         if (DeviceId.isSupportedController(device)) {
            controllers.put(device.getKey(), (VirtualController)device);
         }
      }
      // Find all valid disk devices.
      for (VirtualDevice device : devices) {
         if (device instanceof VirtualDisk) {
             VirtualController controller = controllers.get(device.getControllerKey());
             if (controller != null) {
                diskIds.add(new DeviceId(controller, device));
             }
         }
      }
      return diskIds;
   }

   public static VirtualDevice[] getDevice(final VirtualMachine vm) {
      return vm.getConfig().getHardware().getDevice();
   }

   public static VirtualDevice getVirtualDevice(final VirtualMachine vm, DeviceId deviceId) {
      return findVirtualDevice(getDevice(vm), deviceId);
   }

   public static VirtualDevice getVirtualDevice(VirtualDevice[] devices, DeviceId deviceId) {
      return findVirtualDevice(devices, deviceId);
   }

   public static VirtualDevice findVirtualDevice(VirtualDevice[] devices, DeviceId id) {
      VsphereDirectorAssert.check(id.unitNum != null);
      VirtualDevice controller = findVirtualController(devices, id);
      if (controller == null) {
         return null;
      }
      Integer key = controller.getKey();
      for (VirtualDevice device : devices) {
         if (key.equals(device.getControllerKey()) &&
             id.unitNum.equals(device.getUnitNumber())) {
            return device;
         }
      }
      return null;
   }

   protected static VirtualController findVirtualController(VirtualDevice[] devices, DeviceId id) {
      Class<?> controllerType = id.getTypeClass();
      for (VirtualDevice device : devices) {
         if (controllerType.isInstance(device) &&
               ((VirtualController)device).getBusNumber() == id.busNum) {
            return (VirtualController)device;
         }
      }
      return null;
   }


}
