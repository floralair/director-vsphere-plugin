/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.resources.VcNetwork;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.intf.IVmService;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.Network;
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
   public String getIpAddress(String vmName) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

      if(vm == null) {
         logger.error("No VM " + vmName + " found");
         return null;
      }

      String ipAddress = null;
      int retryTimes = 600; // Raise an error if the VM can not get IP address within 30 minutes

      while ((ipAddress == null || ipAddress.contains(":")) && retryTimes > 0) {
         ipAddress = vm.getGuest().getIpAddress();
         Thread.sleep(3000);
         retryTimes --;
      }

      if (ipAddress == null) {
         throw new VsphereDirectorException("The node " + vmName + " can not get IP address within 30 minutes, please check the networking environment.");
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

      configNetworks(node);

      // enable disk UUID
      vmReconfigService.enableDiskUUID(node);
   }

   /**
    * @param node
    */
   @Override
   public void configNetworks(Node node) throws Exception {
      ManagedObjectReference mob = node.getTargetHost().getMor();
      HostSystem hostSystem = new HostSystem(rootFolder.getServerConnection(), mob);

      Network[] networks = hostSystem.getNetworks();
      logger.info("The host  is " + hostSystem.getName());
      boolean tag = false;
      for (Network network : networks) {
         //the setting network for Cloudera director exists in Esx hosts netowrk which the node vm is located in
         logger.info("The ESX host network is " + network.getName());
         if (node.getNetwork().equals(network.getName())) {
            tag = true;
            VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, node.getVmName());
            if(vm.getNetworks().length == 0){
               VcNetwork vcNet = new VcNetwork();
               vcNet.update(rootFolder.getServerConnection(), network);
               nicOps(node.getVmName(), "add", vcNet, node.getNetwork(), null);
               break;
            }else {
               //edit existing network
               VcNetwork vcNet = new VcNetwork();
               vcNet.update(rootFolder.getServerConnection(), network);
               nicOps(vm.getName(), "edit", vcNet, vm.getNetworks()[0].getName(), node.getNetwork());
               break;
            }
         }
      }

      if(tag == false)
         throw new Exception("Network " + node.getNetwork() + " is not defined on ESX hosts");
   }

   @Override
   public void nicOps(String vmName, String operation, VcNetwork vcNet, String netName, String newNetwork) throws Exception {
      VirtualMachine vm = VmUtil.getVirtualMachine(serviceInstance, vmName);

      VmNicOperationService vmNicOperationService = new VmNicOperationService(vm, operation, vcNet, netName, newNetwork);
      vmNicOperationService.run();
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
}
