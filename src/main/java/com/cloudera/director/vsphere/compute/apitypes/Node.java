/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.vmware.vim25.ManagedObjectReference;

/**
 * @author chiq
 *
 */
public class Node {

   private String templateVm;
   private String vmName;
   private String instanceId;
   private int numCPUs;
   private long memorySizeGB;
   private long swapDiskSizeGB;
   private long dataDiskSizeGB;
   private String targetDatastoreName;
   private ManagedObjectReference targetDatastore;
   private String targetHostName;
   private ManagedObjectReference targetHost;
   private String targetPoolName;
   private ManagedObjectReference targetPool;

   /**
    * @param instanceId
    * @param template
    * @param prefix
    */
   public Node(String instanceId, VSphereComputeInstanceTemplate template, String prefix) {
      this.templateVm = template.getTemplateVm();
      this.vmName = prefix + "-" + instanceId;
      this.instanceId = instanceId;
      this.numCPUs = template.getNumCPUs();
      this.memorySizeGB = template.getMemorySize();
      this.swapDiskSizeGB = template.getMemorySize();
      this.dataDiskSizeGB = template.getDataDiskSize();
   }

   /**
    * @return the templateVm
    */
   public String getTemplateVm() {
      return templateVm;
   }

   /**
    * @param templateVm the templateVm to set
    */
   public void setTemplateVm(String templateVm) {
      this.templateVm = templateVm;
   }

   /**
    * @return the vmName
    */
   public String getVmName() {
      return vmName;
   }

   /**
    * @param vmName the vmName to set
    */
   public void setVmName(String vmName) {
      this.vmName = vmName;
   }

   /**
    * @return the instanceId
    */
   public String getInstanceId() {
      return instanceId;
   }

   /**
    * @param instanceId the instanceId to set
    */
   public void setInstanceId(String instanceId) {
      this.instanceId = instanceId;
   }

   /**
    * @return the numCPUs
    */
   public int getNumCPUs() {
      return numCPUs;
   }

   /**
    * @param numCPUs the numCPUs to set
    */
   public void setNumCPUs(int numCPUs) {
      this.numCPUs = numCPUs;
   }

   /**
    * @return the memorySizeGB
    */
   public long getMemorySizeGB() {
      return memorySizeGB;
   }

   /**
    * @param memorySizeGB the memorySizeGB to set
    */
   public void setMemorySizeGB(long memorySizeGB) {
      this.memorySizeGB = memorySizeGB;
   }

   /**
    * @return the swapDiskSizeGB
    */
   public long getSwapDiskSizeGB() {
      return swapDiskSizeGB;
   }

   /**
    * @param swapDiskSizeGB the swapDiskSizeGB to set
    */
   public void setSwapDiskSizeGB(long swapDiskSizeGB) {
      this.swapDiskSizeGB = swapDiskSizeGB;
   }

   /**
    * @return the dataDiskSizeGB
    */
   public long getDataDiskSizeGB() {
      return dataDiskSizeGB;
   }

   /**
    * @param dataDiskSizeGB the dataDiskSizeGB to set
    */
   public void setDataDiskSizeGB(long dataDiskSizeGB) {
      this.dataDiskSizeGB = dataDiskSizeGB;
   }

   /**
    * @return the targetDatastoreName
    */
   public String getTargetDatastoreName() {
      return targetDatastoreName;
   }

   /**
    * @param targetDatastoreName the targetDatastoreName to set
    */
   public void setTargetDatastoreName(String targetDatastoreName) {
      this.targetDatastoreName = targetDatastoreName;
   }

   /**
    * @return the targetDatastore
    */
   public ManagedObjectReference getTargetDatastore() {
      return targetDatastore;
   }

   /**
    * @param targetDatastore the targetDatastore to set
    */
   public void setTargetDatastore(ManagedObjectReference targetDatastore) {
      this.targetDatastore = targetDatastore;
   }

   /**
    * @return the targetHostName
    */
   public String getTargetHostName() {
      return targetHostName;
   }

   /**
    * @param targetHostName the targetHostName to set
    */
   public void setTargetHostName(String targetHostName) {
      this.targetHostName = targetHostName;
   }

   /**
    * @return the targetHost
    */
   public ManagedObjectReference getTargetHost() {
      return targetHost;
   }

   /**
    * @param targetHost the targetHost to set
    */
   public void setTargetHost(ManagedObjectReference targetHost) {
      this.targetHost = targetHost;
   }

   /**
    * @return the targetPoolName
    */
   public String getTargetPoolName() {
      return targetPoolName;
   }

   /**
    * @param targetPoolName the targetPoolName to set
    */
   public void setTargetPoolName(String targetPoolName) {
      this.targetPoolName = targetPoolName;
   }

   /**
    * @return the targetPool
    */
   public ManagedObjectReference getTargetPool() {
      return targetPool;
   }

   /**
    * @param targetPool the targetPool to set
    */
   public void setTargetPool(ManagedObjectReference targetPool) {
      this.targetPool = targetPool;
   }
}