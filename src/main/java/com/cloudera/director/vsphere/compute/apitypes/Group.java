/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.cloudera.director.vsphere.utils.VmConfigUtil;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.mo.VirtualMachine;

public class Group {

   private String name;
   private int minCount;
   private String networkName;
   private Node templateNode;
   private List<Node> nodes = new ArrayList<Node>();
   private Map<String, String> hostTemplateMap;

   public Group() {}

   public Group(Collection<String> instanceIds) {
      this.name = "group-" + (int) (new Date().getTime()/1000);
      for (String instanceId : instanceIds) {
         Node node = new Node(instanceId);
         this.nodes.add(node);
      }
   }

   public Group(Collection<String> instanceIds, VSphereComputeInstanceTemplate template, String prefix, int minCount, VirtualMachine templateVm) {
      this.name = "group-" + (int) (new Date().getTime()/1000);
      this.minCount = minCount;
      this.templateNode = createNodeFromTemplateVm(templateVm);
      this.networkName = template.getNetwork();
      this.hostTemplateMap = new HashMap<String, String> ();

      for (String instanceId : instanceIds) {
         Node node = new Node(instanceId, template, prefix);
         this.nodes.add(node);
      }
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the minCount
    */
   public int getMinCount() {
      return minCount;
   }

   /**
    * @param minCount the minCount to set
    */
   public void setMinCount(int minCount) {
      this.minCount = minCount;
   }

   /**
    * @return the networkName
    */
   public String getNetworkName() {
      return networkName;
   }

   /**
    * @param networkName the networkName to set
    */
   public void setNetworkName(String networkName) {
      this.networkName = networkName;
   }

   /**
    * @return the nodes
    */
   public List<Node> getNodes() {
      return nodes;
   }

   /**
    * @param nodes the nodes to set
    */
   public void setNodes(List<Node> nodes) {
      this.nodes = nodes;
   }

   /**
    * @return the hostTemplateMap
    */
   public Map<String, String> getHostTemplateMap() {
      return hostTemplateMap;
   }

   /**
    * @param hostTemplateMap the hostTemplateMap to set
    */
   public void setHostTemplateMap(Map<String, String> hostTemplateMap) {
      this.hostTemplateMap = hostTemplateMap;
   }

   /**
    * @return the templateNode
    */
   public Node getTemplateNode() {
      return templateNode;
   }

   /**
    * @param templateNode the templateNode to set
    */
   public void setTemplateNode(Node templateNode) {
      this.templateNode = templateNode;
   }

   public Node createNodeFromTemplateVm(final VirtualMachine templateVm) {
      if (templateVm == null) {
         return null;
      }

      Node templateNode = new Node(templateVm.getName());
      List<DiskSpec> diskSpecs = new ArrayList<DiskSpec>();
      VirtualDevice[] devices = VmConfigUtil.getDevice(templateVm);
      List<DeviceId> deviceIds = VmConfigUtil.getVirtualDiskIds(devices);
      for (DeviceId slot : deviceIds) {
         VirtualDisk vmdk = (VirtualDisk) VmConfigUtil.getVirtualDevice(devices, slot);
         DiskSpec spec = new DiskSpec();
         spec.setName(DiskType.SYSTEM_DISK.getDiskName());
         spec.setSize(vmdk.getCapacityInKB() / (1024 * 1024));
         spec.setDiskType(DiskType.SYSTEM_DISK);
         spec.setController(DiskScsiControllerType.LSI_CONTROLLER);
         diskSpecs.add(spec);
      }
      templateNode.setDisks(diskSpecs);
      return templateNode;
   }

}
