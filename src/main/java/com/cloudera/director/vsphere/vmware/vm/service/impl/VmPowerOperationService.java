/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.impl;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.vmware.vm.service.intf.IVmPowerOperationService;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.ToolsUnavailable;
import com.vmware.vim25.VmConfigFault;
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
   public void run() {
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

   private void reboot() {
      try {
         vm.rebootGuest();
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ToolsUnavailable e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      logger.info(vm.getName() + " guest OS rebooted");
   }

   private void poweron() {
      try {
         Task task = vm.powerOnVM_Task(null);
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " powered on");
         }
      } catch (VmConfigFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (FileFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InsufficientResourcesFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void poweroff() {
      try {
         Task task = vm.powerOffVM_Task();
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " powered off");
         }
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
   }

   private void reset() {
      try {
         Task task = vm.resetVM_Task();
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " reset");
         }
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void standby() {
      try {
         vm.standbyGuest();
         logger.info(vm.getName() + " guest OS stoodby");
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ToolsUnavailable e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void suspend() {
      try {
         Task task = vm.suspendVM_Task();
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " suspended");
         }
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   private void shutdown() {
      try {
         Task task = vm.suspendVM_Task();
         if(task.waitForMe()==Task.SUCCESS) {
            logger.info(vm.getName() + " suspended");
         }
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidState e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RuntimeFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (RemoteException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
}
