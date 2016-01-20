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

public class Group {

   private String name;
   private int minCount;
   private String networkName;
   private long templateStorageUsage;
   private List<Node> nodes = new ArrayList<Node>();
   private Map<String, String> hostTemplateMap;

   public Group(Collection<String> instanceIds, VSphereComputeInstanceTemplate template, String prefix, int minCount, long templateStorageUsage) {
      this.name = "group-" + (int) (new Date().getTime()/1000);
      this.minCount = minCount;
      this.networkName = template.getNetwork();
      this.templateStorageUsage = templateStorageUsage;
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
    * @return the templateStorageUsage
    */
   public long getTemplateStorageUsage() {
      return templateStorageUsage;
   }

   /**
    * @param templateStorageUsage the templateStorageUsage to set
    */
   public void setTemplateStorageUsage(long templateStorageUsage) {
      this.templateStorageUsage = templateStorageUsage;
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
}
