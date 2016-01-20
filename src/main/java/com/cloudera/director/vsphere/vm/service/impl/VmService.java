/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.vm.service.intf.IVmService;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualHardware;
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
   public boolean clone(String sourceVmName, String targetVmName, int numCPUs, long memoryGB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) throws Exception {

      VirtualMachine vm = getVirtualMachine(sourceVmName);

      VmCloneService vMCloneService = new VmCloneService(vm, targetVmName, numCPUs, memoryGB, targetDatastore, targetHost, targetPool);
      return vMCloneService.run();
   }

   @Override
   public void powerOps(String vmName, String operation) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmPowerOperationService vMPowerOperationsService = new VmPowerOperationService(vm, operation);
      vMPowerOperationsService.run();
   }

   @Override
   public String getIpAddress(String vmName) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      if(vm == null) {
         logger.error("No VM " + vmName + " found");
         return null;
      }

      String ipAddress = null;

      while (ipAddress == null || ipAddress.contains(":")) {
         ipAddress = vm.getGuest().getIpAddress();
         Thread.sleep(3000);
      }

      return ipAddress;
   }

   @Override
   public void addDataDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmDiskOperationService vmDiskOperationService = new VmDiskOperationService(vm);
      vmDiskOperationService.addDataDisk(targetDatastoreName, diskSize, diskMode);

   }

   @Override
   public void addSwapDisk(String vmName, String targetDatastoreName, long diskSize, String diskMode) {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmDiskOperationService vmDiskOperationService = new VmDiskOperationService(vm);
      vmDiskOperationService.addSwapDisk(targetDatastoreName, diskSize, diskMode);
   }

   @Override
   public void nicOps(String vmName, String operation, String netName) throws Exception {
      VirtualMachine vm = getVirtualMachine(vmName);

      VmNicOperationService vmNicOperationService = new VmNicOperationService(vm, operation, netName);
      vmNicOperationService.run();
   }

   @Override
   public long getTemplateStorageUsage(String vmName) {
      long templateStorageUsage = 0;

      VirtualMachine vm =  getVirtualMachine(vmName);
      VirtualHardware virtualHardware = vm.getConfig().getHardware();
      VirtualDevice[] virtualDevices = virtualHardware.getDevice();
      for (VirtualDevice virtualDevice : virtualDevices) {
         if (virtualDevice.getDeviceInfo().getLabel().split("Hard disk").length > 1) {
            VirtualDisk virtualDisk = (VirtualDisk) virtualDevice;
            templateStorageUsage += virtualDisk.getCapacityInKB();
         }
      }
      templateStorageUsage = templateStorageUsage / 1024 / 1024;  // Convert to GB

      return templateStorageUsage;
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
