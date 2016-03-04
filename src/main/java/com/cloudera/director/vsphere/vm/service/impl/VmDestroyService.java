/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.vm.service.intf.IVmDestroyService;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class VmDestroyService implements IVmDestroyService {
   private static final Logger logger = LoggerFactory.getLogger(VmDestroyService.class);

   private final VirtualMachine vm;

   public VmDestroyService(VirtualMachine vm) {
      this.vm = vm;
   }

   @Override
   public boolean run() throws Exception {
      String vmName = vm.getName();
      Task task = vm.destroy_Task();
      logger.info("Destroying the VM " + vmName + " from vCenter. Please wait...");

      String status = null;
      status = task.waitForTask();
      if(status==Task.SUCCESS) {
         logger.info("Destroy the VM " + vmName + " successfully.");
         return true;
      }
      else {
         logger.error("Failure: VM " + vmName + " cannot be destroyed.");
         return false;
      }
   }

}
