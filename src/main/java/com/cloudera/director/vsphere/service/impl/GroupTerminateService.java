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
import com.cloudera.director.vsphere.resourcesplacement.ResourcesPlacement;
import com.cloudera.director.vsphere.service.intf.IGroupTerminateService;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.impl.VmService;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public class GroupTerminateService implements IGroupTerminateService {
   private static final Logger logger = LoggerFactory.getLogger(GroupTerminateService.class);

   private final VSphereCredentials credentials;
   private final Group group;
   private final VmService vmService;
   private ResourcesPlacement resourcesPlacement;

   public GroupTerminateService(VSphereCredentials credentials, VSphereComputeInstanceTemplate template, String prefix, Collection<String> instanceIds, ResourcesPlacement resourcesPlacement) throws Exception {
      this.credentials = credentials;
      this.group = new Group(instanceIds, template, prefix, 0, VmUtil.getVirtualMachine(credentials.getServiceInstance(), template.getTemplateVm(), false));
      this.vmService = new VmService(credentials);
      this.resourcesPlacement = resourcesPlacement;
   }

   /**
    * @return the credentials
    */
   public VSphereCredentials getCredentials() {
      return credentials;
   }

   /**
    * @return the group
    */
   public Group getGroup() {
      return group;
   }

   /**
    * @return the vmService
    */
   public VmService getVmService() {
      return vmService;
   }

   /**
    * @return the resourcesPlacement
    */
   public ResourcesPlacement getResourcesPlacement() {
      return resourcesPlacement;
   }

   /**
    * @param resourcesPlacement the resourcesPlacement to set
    */
   public void setResourcesPlacement(ResourcesPlacement resourcesPlacement) {
      this.resourcesPlacement = resourcesPlacement;
   }

   @Override
   public void terminate() throws Exception {
      for (Node node : group.getNodes()) {
         terminateNode(node);
         logger.info(String.format("Deleted allocation: %s -> %s", node.getVmName(), node.getInstanceId()));
      }
      this.resourcesPlacement.update(this.credentials.getVcServer());
   }

   private void terminateNode(Node node) throws Exception {
      try {

         VirtualMachine vm = VmUtil.getVirtualMachine(this.credentials.getServiceInstance(), node.getVmName());
         HostSystem hostSystem = new HostSystem(this.credentials.getServiceInstance().getServerConnection(), vm.getRuntime().getHost());
         String datastore = vm.getDatastores()[0].getName();

         vmService.powerOps(node.getVmName(), "poweroff");
         vmService.destroyVm(node.getVmName());

         this.resourcesPlacement.updateNodesCount(this.credentials.getVcServer(), hostSystem.getName(), datastore);
      } catch (Exception e) {
         logger.error("The VM " + node.getVmName() + " already destroyed or can not be destroyed.");
      }
   }
}
