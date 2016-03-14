/**
 *
 */
package com.cloudera.director.vsphere.vm.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.intf.IWaitVmReadyService;
import com.vmware.vim25.OptionValue;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class WaitVmReadyService implements IWaitVmReadyService {
   private static final Logger logger = LoggerFactory.getLogger(WaitVmReadyService.class);

   public static final String VM_DISK_FORMAT_STATUS_KEY = "guestinfo.DiskFormatCode";
   public static final String VM_DISK_FORMAT_ERROR_KEY = "guestinfo.disk.format.status";
   private static final int QUERY_GUEST_VARIABLE_INTERVAL = 5000;

   private static final String DISK_FORMAT_SUCCESS = "0";
   private static final String DISK_FORMAT_INPROGRESS = "1";
   private static final int MAX_WAITING_SECONDS = 7200;

   private ServiceInstance serviceInstance;
   private VirtualMachine vm;

   public WaitVmReadyService(ServiceInstance serviceInstance, VirtualMachine vm) {
      this.serviceInstance = serviceInstance;
      this.vm = vm;
   }

   /**
    * @return the serviceInstance
    */
   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

   /**
    * @param serviceInstance the serviceInstance to set
    */
   public void setServiceInstance(ServiceInstance serviceInstance) {
      this.serviceInstance = serviceInstance;
   }

   /**
    * @return the vm
    */
   public VirtualMachine getVm() {
      return vm;
   }

   /**
    * @param vm the vm to set
    */
   public void setVm(VirtualMachine vm) {
      this.vm = vm;
   }

   @Override
   public void run() throws Exception {
      logger.info("Waiting for the node " + vm.getName() + " to ready.");
      waitForIpAddress();
      waitForDiskFormat();
      logger.info("The node " + vm.getName() + " is ready.");
   }

   public void waitForIpAddress() throws Exception {

      String ipAddress = null;

      long start = System.currentTimeMillis();
      while ((ipAddress == null || ipAddress.contains(":")) && isNotTimeout(start)) {
         updateVm();
         Thread.sleep(QUERY_GUEST_VARIABLE_INTERVAL);
         ipAddress = vm.getGuest().getIpAddress();
      }

      if (ipAddress == null) {
         throw new VsphereDirectorException("The node " + vm.getName() + " can not get IP address within 120 minutes, please check the networking environment.");
      } else {
         logger.info("The node " + vm.getName() + " IP address is ready.");
      }
   }

   public void waitForDiskFormat() throws Exception {

      String status = getStatus(vm, VM_DISK_FORMAT_STATUS_KEY, DISK_FORMAT_INPROGRESS, "Disk preparing");

      if (isInprogress(status, DISK_FORMAT_INPROGRESS)) {
         logger.error("Didn't get disk preparing finished signal for vm " + vm.getName() + ".");
         throw new VsphereDirectorException("Failed to get disk format status for node " + vm.getName() + ".");
      }

      if (isFailed(status, DISK_FORMAT_SUCCESS)) {
         Map<String, String> variables = getGuestVariables();
         String error = variables.get(VM_DISK_FORMAT_ERROR_KEY);
         logger.error("Failed to prepare disk for vm " + vm.getName() + ", for " + error);
         throw new VsphereDirectorException("Failed to format disk for node " + vm.getName() + ".");
      }

      logger.info("Disk preparing finished for node " + vm.getName());
   }

   private String getStatus(VirtualMachine vm, String statusKey, String inprogress, String action) throws Exception {
      Map<String, String> variables = getGuestVariables();
      String status = variables.get(statusKey);
      try {
         long start = System.currentTimeMillis();
         while (isInprogress(status, inprogress) && isNotTimeout(start)) {
            Thread.sleep(QUERY_GUEST_VARIABLE_INTERVAL);
            variables = getGuestVariables();
            status = variables.get(statusKey);
         }
      } catch (InterruptedException e) {
         logger.info("Waiting for " + action + " thread is interrupted.", e);
      }

      return status;
   }

   public Map<String, String> getGuestVariables() throws Exception {
      updateVm();
      Map<String, String> guestVariables = new HashMap<String, String>();
      for (OptionValue val : vm.getConfig().getExtraConfig()) {
         if (val.getKey().contains("guestinfo")) {
            if (val.getValue() != null) {
               guestVariables.put(val.getKey(), val.getValue().toString());
            } else {
               logger.info("got null val on " + val.getKey());
            }
         }
      }
      return guestVariables;
   }

   private boolean isInprogress(String status, String inprogress) {
      if (status == null || status.equalsIgnoreCase(inprogress)) {
         return true;
      } else {
         return false;
      }
   }

   private boolean isFailed(String status, String success) {
      return !status.equalsIgnoreCase(success);
   }

   private boolean isNotTimeout(long start) {
      long timeout = MAX_WAITING_SECONDS * 1000;
      return System.currentTimeMillis() - start < timeout;
   }

   private void updateVm() throws Exception {
      vm = VmUtil.getVirtualMachine(serviceInstance, vm.getName());
   }
}
