/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudera.director.vsphere.compute.apitypes.DiskCreateSpec;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema.Disk;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.utils.DiskSchemaUtil;
import com.cloudera.director.vsphere.utils.VmConfigUtil;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.intf.IVmReconfigService;
import com.google.gson.Gson;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFlagInfo;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmReconfigService implements IVmReconfigService {

   private final ServiceInstance serviceInstance;

   private final String MACHINE_ID = "machine.id";
   private  final String GUEST_VARIABLE_VOLUMES = "volumes";

   public VmReconfigService(ServiceInstance serviceInstance) {
      this.serviceInstance = serviceInstance;
   }

   /**
    * @return the serviceInstance
    */
   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

   public void changeDisks(Node node) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, node.getVmName());
      final VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      final List<VirtualDeviceConfigSpec> devChanges = new ArrayList<VirtualDeviceConfigSpec>();

      HashMap<String, Disk.Operation> diskMap = new HashMap<String, Disk.Operation>();
      List<DiskCreateSpec> addDisks = DiskSchemaUtil.getDisksToAdd(node, diskMap);

      if (addDisks != null) {
         for (DiskCreateSpec spec : addDisks) {
            devChanges.add(spec.getVcSpec(node.getKey(), vm));
         }
      }

      configSpec.setDeviceChange(devChanges.toArray(new VirtualDeviceConfigSpec[devChanges.size()]));
      VmConfigUtil.reconfigure(vm, configSpec);
   }

   @Override
   public void setVolumesToMachineId(Node node) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, node.getVmName());
      String volumes = VmUtil.getVolumes(vm, node.getVmSchema().diskSchema.getDisks());
      Map<String, Object> volumesVariable = new HashMap<String, Object>();
      volumesVariable.put(GUEST_VARIABLE_VOLUMES, volumes);
      setMachineIdVariables(vm, volumesVariable);
   }

   @Override
   public void setMachineIdVariables(VirtualMachine vm, Map<String, Object> guestVariables) throws Exception {
      setExtraConfig(vm, MACHINE_ID, guestVariables);
   }

   @Override
   public void setExtraConfig(VirtualMachine vm, String optionKey, Object value) throws Exception {

      VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
      String jsonString = (new Gson()).toJson(value);

      OptionValue[] extraConfig = new OptionValue[1];
      extraConfig[0] = new OptionValue();
      extraConfig[0].setKey(optionKey);
      extraConfig[0].setValue(jsonString);

      vmSpec.setExtraConfig(extraConfig);

      vm.reconfigVM_Task(vmSpec);
   }

   @Override
   public void enableDiskUUID(Node node) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, node.getVmName());

      VirtualMachineFlagInfo flagInfo = new VirtualMachineFlagInfo();
      flagInfo.setDiskUuidEnabled(true);
      VirtualMachineConfigSpec configSpec = new VirtualMachineConfigSpec();
      configSpec.setFlags(flagInfo);
      vm.reconfigVM_Task(configSpec);
   }

}
