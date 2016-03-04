/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import com.cloudera.director.vsphere.resources.VcNetwork;
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
   private final VcNetwork vcNetwork;

   public VmNicOperationService(VirtualMachine vm, String operation, VcNetwork vcNet, String netName, String newNetname) {
      this.vm = vm;
      this.operation = operation;
      this.netName = netName;
      this.newNetname = newNetname;
      this.vcNetwork = vcNet;
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
         VirtualDeviceBackingInfo nicBacking;
         VirtualEthernetCardNetworkBackingInfo standardNicBacking;

         VirtualDeviceConnectInfo connectInfo = new VirtualDeviceConnectInfo();
         connectInfo.setConnected(true);
         connectInfo.setStartConnected(true);

         if(vcNetwork.isDvPortGroup()) {
            nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) vcNetwork.getBackingInfo();
            nic.setBacking(nicBacking);
         }else{
            standardNicBacking = new VirtualEthernetCardNetworkBackingInfo();
            standardNicBacking.setDeviceName(this.newNetname);
            nic.setBacking(standardNicBacking);
         }

         Description tmp = new Description();
         tmp.setSummary(this.newNetname);
         tmp.setLabel(this.newNetname);

         nic.setAddressType("generated");
         nic.setKey(4);
         nic.setConnectable(connectInfo);
         nic.setDeviceInfo(tmp);
         nicSpec.setDevice(nic);
         return nicSpec;
      } else if("remove".equalsIgnoreCase(this.operation)) {
         VirtualDevice[] vds = vmConfigInfo.getHardware().getDevice();
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
         for (int i = 0; i < vds.length; i++) {
            if ((vds[i] instanceof VirtualEthernetCard) && (vds[i].getKey() == 4000)) {
               nicSpec.setDevice(vds[i]);
               return nicSpec;
            }
         }
      }else if("edit".equalsIgnoreCase(this.operation)){
         VirtualDevice [] vds = vmConfigInfo.getHardware().getDevice();
         nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
         VirtualDeviceBackingInfo nicBacking;
         VirtualEthernetCardNetworkBackingInfo standardNicBacking;
         for(int i=0; i<vds.length; i++) {
            //if ((vds[i].getDeviceInfo().getLabel().equalsIgnoreCase(this.netName))) {
            if((vds[i].getKey() == 4000)){
               VirtualDeviceConnectInfo connectInfo = new VirtualDeviceConnectInfo();
               connectInfo.setConnected(true);
               connectInfo.setStartConnected(true);

               if(vcNetwork.isDvPortGroup()) {
                  nicBacking = (VirtualEthernetCardDistributedVirtualPortBackingInfo) vcNetwork.getBackingInfo();
                  vds[i].setBacking(nicBacking);
               }
               else {
                  standardNicBacking = (VirtualEthernetCardNetworkBackingInfo)vds[i].getBacking();
                  standardNicBacking.setDeviceName(this.newNetname);
                  vds[i].setBacking(standardNicBacking);
               }

               Description tmp = vds[i].getDeviceInfo();
               tmp.setSummary(this.newNetname);

               vds[i].setConnectable(connectInfo);
               vds[i].setDeviceInfo(tmp);

               nicSpec.setDevice(vds[i]);
               return nicSpec;
            }
         }
      }
      return nicSpec;
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
