/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.intf.IVmService;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.Folder;
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

   /**
    * @return the serviceInstance
    */
   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

   /**
    * @return the rootFolder
    */
   public Folder getRootFolder() {
      return rootFolder;
   }

   @Override
   public boolean clone(String sourceVmName, String targetVmName, int numCPUs, long memoryGB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) throws Exception {

      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, sourceVmName);

      VmCloneService vMCloneService = new VmCloneService(vm, targetVmName, numCPUs, memoryGB, targetDatastore, targetHost, targetPool);
      return vMCloneService.run();
   }

   @Override
   public void powerOps(String vmName, String operation) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

      VmPowerOperationService vMPowerOperationsService = new VmPowerOperationService(vm, operation);
      vMPowerOperationsService.run();
   }

   @Override
   public VirtualMachine getVm(String vmName) throws Exception {
     return VmUtil.getVirtualMachine(serviceInstance, vmName);
   }

   @Override
   public String getIpAddress(String vmName) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

      String ipAddress = vm.getGuest().getIpAddress();

      if (ipAddress == null) {
         throw new VsphereDirectorException("The node " + vmName + " have no IP address.");
      }

      return ipAddress;
   }

   /**
    * @param node
    */
   @Override
   public void configureVm(Node node) throws Exception {

      VmReconfigService vmReconfigService = new VmReconfigService(serviceInstance);
      vmReconfigService.changeDisks(node);

      vmReconfigService.setVolumesToMachineId(node);

      vmReconfigService.configNetworks(node);

      // enable disk UUID
      vmReconfigService.enableDiskUUID(node);
   }

   @Override
   public boolean destroyVm(String vmName) throws Exception {
      try {
         VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

         VmDestroyService vmDestroyService = new VmDestroyService(vm);
         return vmDestroyService.run();
      } catch(Exception e) {
         logger.error("The VM " + vmName + " already destroyed or can not be destroyed.");
         return false;
      }
   }

   @Override
   public void waitVmReady(String vmName) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

      WaitVmReadyService waitVmReadyService = new WaitVmReadyService(serviceInstance, vm);
      waitVmReadyService.run();
   }

}
