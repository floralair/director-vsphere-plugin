/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.cloudera.director.vsphere.compute.apitypes.Group;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.service.intf.IGroupTerminateService;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.impl.VmService;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * @author chiq
 *
 */
public class GroupTerminateService implements IGroupTerminateService {
   private static final Logger logger = LoggerFactory.getLogger(GroupTerminateService.class);

   private final Group group;
   private final Folder rootFolder;
   private final VmService vmService;

   public GroupTerminateService(VSphereCredentials credentials, VSphereComputeInstanceTemplate template, String prefix, Collection<String> instanceIds) throws Exception {
      ServiceInstance serviceInstance = credentials.getServiceInstance();
      this.rootFolder = serviceInstance.getRootFolder();
      this.group = new Group(instanceIds, template, prefix, 0, VmUtil.getVirtualMachine(serviceInstance, template.getTemplateVm(), false));
      this.vmService = new VmService(credentials);
   }

   /**
    * @return the group
    */
   public Group getGroup() {
      return group;
   }

   /**
    * @return the rootFolder
    */
   public Folder getRootFolder() {
      return rootFolder;
   }

   /**
    * @return the vmService
    */
   public VmService getVmService() {
      return vmService;
   }

   @Override
   public void terminate() throws Exception {
      for (Node node : group.getNodes()) {
         terminateNode(node);
         logger.info(String.format("Deleted allocation: %s -> %s", node.getVmName(), node.getInstanceId()));
      }
   }

   private void terminateNode(Node node) throws Exception {
      try {
         vmService.powerOps(node.getVmName(), "poweroff");
         vmService.destroyVm(node.getVmName());
      } catch (Exception e) {
         logger.error("The VM " + node.getVmName() + " already destroyed or can not be destroyed.");
      }
   }
}
