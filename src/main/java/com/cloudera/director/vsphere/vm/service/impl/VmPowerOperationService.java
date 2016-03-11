/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.vm.service.intf.IVmPowerOperationService;
import com.vmware.vim25.FaultToleranceConfigInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VmPowerOperationService implements IVmPowerOperationService {
   private static final Logger logger = LoggerFactory.getLogger(VmPowerOperationService.class);

   private final VirtualMachine vm;
   private final String operation;

   public VmPowerOperationService(VirtualMachine vm, String operation) {
      this.vm = vm;
      this.operation = operation;
   }

   @Override
   public void run() throws Exception {
      if ("reboot".equalsIgnoreCase(operation)) {
         reboot();
      } else if("poweron".equalsIgnoreCase(operation)) {
         poweron();
      } else if("poweroff".equalsIgnoreCase(operation)) {
         poweroff();
      } else if("reset".equalsIgnoreCase(operation)) {
         reset();
      } else if("standby".equalsIgnoreCase(operation)) {
         standby();
      } else if("suspend".equalsIgnoreCase(operation)) {
         suspend();
      } else if("shutdown".equalsIgnoreCase(operation)) {
         shutdown();
      } else {
         logger.error("Invalid operation. Exiting...");
      }
   }

   private void reboot() throws Exception {
      vm.rebootGuest();
      logger.info(vm.getName() + " guest OS rebooted");
   }

   private void poweron() throws Exception {
      Task task = vm.powerOnVM_Task(null);
      if(task.waitForMe()==Task.SUCCESS) {
         logger.info(vm.getName() + " powered on");
      }
   }

   private void poweroff() throws Exception {
      FaultToleranceConfigInfo info = vm.getConfig().getFtInfo();
      if (info != null && info.getRole() == 1) {
         logger.info("VM " + vm.getName() + " is FT primary VM, disable FT before delete it.");
         Task turnOffFTtask = vm.turnOffFaultToleranceForVM_Task();
         if (turnOffFTtask.waitForMe() == Task.SUCCESS) {
            logger.info("The VM " + vm.getName() + " FT is disabled.");
         }
      }
      if (VirtualMachinePowerState.poweredOn.equals(vm.getRuntime().getPowerState()) || VirtualMachinePowerState.suspended.equals(vm.getRuntime().getPowerState())) {
         Task task = vm.powerOffVM_Task();
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " powered off");
         }
      }
   }

   private void reset() throws Exception {
      Task task = vm.resetVM_Task();
      if(task.waitForMe()==Task.SUCCESS) {
         logger.info(vm.getName() + " reset");
      }
   }

   private void standby() throws Exception {
      vm.standbyGuest();
      logger.info(vm.getName() + " guest OS stoodby");
   }

   private void suspend() throws Exception {
      Task task = vm.suspendVM_Task();
      if(task.waitForMe()==Task.SUCCESS) {
         logger.info(vm.getName() + " suspended");
      }
   }

   private void shutdown() throws Exception {
      Task task = vm.suspendVM_Task();
      if(task.waitForMe()==Task.SUCCESS) {
         logger.info(vm.getName() + " suspended");
      }
   }
}
