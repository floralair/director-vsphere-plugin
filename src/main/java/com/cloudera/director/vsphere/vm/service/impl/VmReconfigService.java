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
import com.cloudera.director.vsphere.vm.service.intf.IVmReconfigService;
import com.google.gson.Gson;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmReconfigService implements IVmReconfigService {

   private final VirtualMachine vm;

   private final String MACHINE_ID = "machine.id";

   /**
    * @param vm
    */
   public VmReconfigService(VirtualMachine vm) {
      this.vm = vm;
   }

   /**
    * @return the vm
    */
   public VirtualMachine getVm() {
      return vm;
   }

   public void changeDisks(Node node) throws Exception {
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
   public void setMachineIdVariables(Map<String, Object> guestVariables) throws Exception {
      setExtraConfig(MACHINE_ID, guestVariables);
   }

   @Override
   public void setExtraConfig(String optionKey, Object value) throws Exception {

      VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
      String jsonString = (new Gson()).toJson(value);

      OptionValue[] extraConfig = new OptionValue[1];
      extraConfig[0] = new OptionValue();
      extraConfig[0].setKey(optionKey);
      extraConfig[0].setValue(jsonString);

      vmSpec.setExtraConfig(extraConfig);

      vm.reconfigVM_Task(vmSpec);
   }

}
