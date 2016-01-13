/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.impl;

import com.cloudera.director.vsphere.vmware.vm.service.intf.IVmNicOperationService;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VmNicOperationService implements IVmNicOperationService{


   private final VirtualMachine vm;
   private final String operation;
   private final String netName;

   public VmNicOperationService(VirtualMachine vm, String operation, String netName) {
      this.vm = vm;
      this.operation = operation;
      this.netName = netName;
   }

   @Override
   public void run() throws Exception {

      VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

      VirtualDeviceConfigSpec nicSpec = getNICDeviceConfigSpec();

      String result = null;
      if(nicSpec!=null) {
         vmConfigSpec.setDeviceChange(new VirtualDeviceConfigSpec []{nicSpec});
         Task task = vm.reconfigVM_Task(vmConfigSpec);
         result = task.waitForMe();
      }

      if(result==Task.SUCCESS) {
         System.out.println("Done with NIC for VM:" + this.vm.getName());
      } else {
         System.out.println("Failed with NIC for VM:" + this.vm.getName());
      }
   }

   private VirtualDeviceConfigSpec getNICDeviceConfigSpec() throws Exception {
      VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
      VirtualMachineConfigInfo vmConfigInfo = vm.getConfig();

      if("add".equalsIgnoreCase(this.operation) && doesNetworkNameExist()) {
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
         VirtualEthernetCard nic =  new VirtualPCNet32();
         VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
         nicBacking.setDeviceName(this.netName);
         nic.setAddressType("generated");
         nic.setBacking(nicBacking);
         nic.setKey(4);
         nicSpec.setDevice(nic);
         return nicSpec;
      } else if("remove".equalsIgnoreCase(this.operation)) {
         VirtualDevice [] vds = vmConfigInfo.getHardware().getDevice();
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
         for(int i=0; i<vds.length; i++) {
            if((vds[i] instanceof VirtualEthernetCard) && (vds[i].getDeviceInfo().getLabel().equalsIgnoreCase(this.netName))) {
               nicSpec.setDevice(vds[i]);
               return nicSpec;
            }
         }
      }
      return null;
   }

   private boolean doesNetworkNameExist() throws Exception {
      VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
      ManagedObjectReference hmor = vmRuntimeInfo.getHost();
      HostSystem host = new HostSystem(vm.getServerConnection(), hmor);

      Network[] networks = host.getNetworks();
      for (Network network : networks) {
         if (network.getSummary().isAccessible() && network.getName().equalsIgnoreCase(this.netName)) {
            return true;
         }
      }

      return false;
   }

}
