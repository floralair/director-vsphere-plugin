/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.util.Collection;
import java.util.Map;

import com.cloudera.director.vsphere.VSphereCredentials;
import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.cloudera.director.vsphere.compute.apitypes.Group;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.resourcesplacement.ResourcesPlacement;
import com.cloudera.director.vsphere.service.intf.IGroupProvisionService;
import com.cloudera.director.vsphere.service.intf.IPlacementPlanner;
import com.cloudera.director.vsphere.utils.VmUtil;
import com.cloudera.director.vsphere.vm.service.impl.VmService;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * @author chiq
 *
 */
public class GroupProvisionService implements IGroupProvisionService {

   private VSphereCredentials credentials;
   private Group group;
   private VmService vmService;
   private ResourcesPlacement resourcesPlacement;

   public GroupProvisionService() {

   }

   public GroupProvisionService(VSphereCredentials credentials, VSphereComputeInstanceTemplate template, String prefix, Collection<String> instanceIds, int minCount, ResourcesPlacement resourcesPlacement) throws Exception {
      ServiceInstance serviceInstance = credentials.getServiceInstance();
      this.credentials = credentials;
      this.group = new Group(instanceIds, template, prefix, minCount, VmUtil.getVirtualMachine(serviceInstance, template.getTemplateVm()));
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
    * @param credentials the credentials to set
    */
   public void setCredentials(VSphereCredentials credentials) {
      this.credentials = credentials;
   }

   /**
    * @return the group
    */
   public Group getGroup() {
      return group;
   }


   /**
    * @param group the group to set
    */
   public void setGroup(Group group) {
      this.group = group;
   }

   /**
    * @return the vmService
    */
   public VmService getVmService() {
      return vmService;
   }

   /**
    * @param vmService the vmService to set
    */
   public void setVmService(VmService vmService) {
      this.vmService = vmService;
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
   public void provision() throws Exception {
      getPlacementPlan();
      cloneVms();
      reconfigVms();
      startVms();
      waitVmsReady();
   }

   public void getPlacementPlan() throws Exception {
      IPlacementPlanner placementPlanner = new PlacementPlanner(credentials, group, resourcesPlacement);
      placementPlanner.init();
      placementPlanner.initNodeDisks();
      placementPlanner.placeDisk();
   }

   private void cloneVms() throws Exception {
      for (Node node : group.getNodes()) {

         String templateVmName = node.getTemplateVm();

         // Use the first cloning vm in a host to source template vm to improve vms cloning performance.
         Map<String, String> hostTemplateMap = group.getHostTemplateMap();
         String targetHostName = node.getTargetHost().getName();
         if (hostTemplateMap.get(targetHostName) != null) {
            templateVmName = hostTemplateMap.get(targetHostName);
         }

         boolean isCloned = vmService.clone(templateVmName, node.getVmName(), node.getNumCPUs(), node.getMemorySizeGB(), node.getTargetDatastore().getMor(), node.getTargetHost().getMor(), node.getTargetPool().getMor());

         if (isCloned && hostTemplateMap.get(targetHostName) == null) {
            hostTemplateMap.put(targetHostName, node.getVmName());
            group.setHostTemplateMap(hostTemplateMap);
         }
      }
   }

   private void reconfigVms() throws Exception {
      for (Node node : group.getNodes()) {
         vmService.configureVm(node);
      }
   }

   private void startVms() throws Exception {
      for (Node node : group.getNodes()) {
         vmService.powerOps(node.getVmName(), "poweron");
      }
   }

   private void waitVmsReady() throws Exception {
      for (Node node : group.getNodes()) {
         vmService.waitVmReady(node.getVmName());
      }
   }

}