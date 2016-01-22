/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import com.cloudera.director.vsphere.vm.service.intf.IVmNicOperationService;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VmNicOperationService implements IVmNicOperationService{


   private final VirtualMachine vm;
   private final String operation;
   private final String netName;
   private final String newNetname;

   public VmNicOperationService(VirtualMachine vm, String operation, String netName, String newNetname) {
      this.vm = vm;
      this.operation = operation;
      this.netName = netName;
      this.newNetname = newNetname;
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
         VirtualEthernetCard nic =  new VirtualVmxnet3();
         VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
         nicBacking.setDeviceName(this.netName);
         nic.setAddressType("generated");
         nic.setBacking(nicBacking);
         nic.setKey(4);
         nicSpec.setDevice(nic);
         return nicSpec;
      } else if("remove".equalsIgnoreCase(this.operation)) {
         VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
         for (int i = 0; i < vds.length; i++) {
            if ((vds[i] instanceof VirtualEthernetCard) && (vds[i].getDeviceInfo().getLabel().equalsIgnoreCase(this.netName))) {
               nicSpec.setDevice(vds[i]);
               return nicSpec;
            }
         }
      }else if("edit".equalsIgnoreCase(this.operation)){
            VirtualDevice [] vds = vmConfigInfo.getHardware().getDevice();
            nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
            for(int i=0; i<vds.length; i++) {
               if ((vds[i].getDeviceInfo().getSummary().equalsIgnoreCase(this.netName))) {
                  VirtualDeviceConnectInfo connectInfo = new VirtualDeviceConnectInfo();
                  connectInfo.setConnected(true);
                  connectInfo.setStartConnected(true);

                  VirtualEthernetCardNetworkBackingInfo nicBacking = (VirtualEthernetCardNetworkBackingInfo)vds[i].getBacking();
                  nicBacking.setDeviceName(this.newNetname);

                  Description tmp = vds[i].getDeviceInfo();
                  tmp.setSummary(this.newNetname);

                  vds[i].setBacking(nicBacking);
                  vds[i].setConnectable(connectInfo);
                  vds[i].setDeviceInfo(tmp);

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
