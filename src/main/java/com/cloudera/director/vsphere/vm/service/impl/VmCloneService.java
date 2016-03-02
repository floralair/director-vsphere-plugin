/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.vm.service.intf.IVmCloneService;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmCloneService implements IVmCloneService{

   private static final Logger logger = LoggerFactory.getLogger(VmCloneService.class);

   private final VirtualMachine vm;
   private final String cloneName;
   private final int numCPUs;
   private final long memoryGB;
   private final ManagedObjectReference targetDatastore;
   private final ManagedObjectReference targetHost;
   private final ManagedObjectReference targetPool;

   public VmCloneService(VirtualMachine vm, String cloneName, int numCPUs, long memoryGB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) {
      this.vm = vm;
      this.cloneName = cloneName;
      this.numCPUs = numCPUs;
      this.memoryGB = memoryGB;
      this.targetDatastore = targetDatastore;
      this.targetHost = targetHost;
      this.targetPool = targetPool;
   }

   @Override
   public boolean run() throws Exception {

      if(vm == null) {
         return false;
      }

      VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();

      VirtualMachineRelocateSpec relocateSpec = new VirtualMachineRelocateSpec();
      if (targetDatastore != null) {
         relocateSpec.setDatastore(targetDatastore);
      }

      if (targetHost != null) {
         relocateSpec.setHost(targetHost);
      }

      if (targetPool != null) {
         relocateSpec.setPool(targetPool);
      }

      cloneSpec.setLocation(relocateSpec);
      cloneSpec.setPowerOn(false);
      cloneSpec.setTemplate(false);

      VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
      vmSpec.setName(cloneName);
      vmSpec.setMemoryMB(memoryGB * 1024);
      vmSpec.setNumCPUs(numCPUs);
      cloneSpec.setConfig(vmSpec);

      Task task = null;
      task = vm.cloneVM_Task((Folder) vm.getParent(), cloneName, cloneSpec);
      logger.info("Launching the VM " + cloneName + " clone task. " + "Please wait ...");

      String status = null;
      status = task.waitForTask();
      if(status==Task.SUCCESS) {
         logger.info("VM " + cloneName + " got cloned successfully.");
         return true;
      }
      else {
         logger.error("Failure -: VM " + cloneName + " cannot be cloned");
         return false;
      }
   }
}
