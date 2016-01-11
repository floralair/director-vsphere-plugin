/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.impl;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.vmware.vm.service.intf.IVmService;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class VmService implements IVmService {
   private static final Logger logger = LoggerFactory.getLogger(VmService.class);

   private final ServiceInstance serviceInstance;
   private final Folder rootFolder;

   public VmService (VSphereCredentials credentials){
      this.serviceInstance = credentials.getServiceInstance();
      this.rootFolder = this.serviceInstance.getRootFolder();
   }

   @Override
   public void clone(String vmName, String cloneName, String numCPUs, String memoryMB) {

      VirtualMachine vm = getVirtualMachine(vmName);

      VmCloneService vMCloneService = new VmCloneService(vm, cloneName, numCPUs, memoryMB);
      vMCloneService.run();
   }

   @Override
   public void powerOps(String vmName, String operation) {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmPowerOperationService vMPowerOperationsService = new VmPowerOperationService(vm, operation);
      vMPowerOperationsService.run();
   }

   @Override
   public String getIpaddress(String vmName) {
      VirtualMachine vm = getVirtualMachine(vmName);

      if(vm == null) {
         logger.error("No VM " + vmName + " found");
         return null;
      }

      String ipAddress = null;

      while (ipAddress == null) {
         ipAddress = vm.getGuest().getIpAddress();

         try {
            Thread.sleep(3000);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      return ipAddress;
   }

   @Override
   public void addDisk(String vmName, int diskSize, String diskMode) {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmDiskOperationService vmDiskOperationService = new VmDiskOperationService(vm);
      vmDiskOperationService.addDisk(diskSize, diskMode);

   }

   @Override
   public VirtualMachine getVirtualMachine(String vmName) {
      VirtualMachine vm = null;
      try {
         vm = (VirtualMachine) new InventoryNavigator(this.rootFolder).searchManagedEntity("VirtualMachine", vmName);
      } catch (InvalidProperty e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return vm;
   }

}
