/**
 *
 */
package com.cloudera.director.vsphere.vmware.vm.service.impl;

import java.rmi.RemoteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.vmware.vm.service.intf.IVmCloneService;
import com.vmware.vim25.CustomizationFault;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidState;
import com.vmware.vim25.MigrationFault;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInProgress;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VmConfigFault;
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
   private final String numCPUs;
   private final String memoryGB;

   public VmCloneService(VirtualMachine vm, String cloneName, String numCPUs, String memoryGB) {
      this.vm = vm;
      this.cloneName = cloneName;
      this.numCPUs = numCPUs;
      this.memoryGB = memoryGB;
   }

   @Override
   public void run() {

      if(vm == null) {
         logger.error("No VM " + vm.getName() + " found");
         return;
      }

      VirtualMachineCloneSpec cloneSpec = new VirtualMachineCloneSpec();
      cloneSpec.setLocation(new VirtualMachineRelocateSpec());
      cloneSpec.setPowerOn(false);
      cloneSpec.setTemplate(false);

      VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
      vmSpec.setName(cloneName);
      vmSpec.setMemoryMB(Long.parseLong(memoryGB) * 1024);
      vmSpec.setNumCPUs(Integer.parseInt(numCPUs));
      cloneSpec.setConfig(vmSpec);

      Task task = null;
      try {
         task = vm.cloneVM_Task((Folder) vm.getParent(), cloneName, cloneSpec);
         logger.info("Launching the VM " + cloneName + " clone task. " + "Please wait ...");

         String status = null;
         try {
            status = task.waitForTask();
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         if(status==Task.SUCCESS) {
            logger.info("VM " + cloneName + " got cloned successfully.");
         }
         else {
            logger.error("Failure -: VM " + cloneName + " cannot be cloned");
         }
      } catch (VmConfigFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (TaskInProgress e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (CustomizationFault e) {
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
      } catch (MigrationFault e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidDatastore e) {
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
