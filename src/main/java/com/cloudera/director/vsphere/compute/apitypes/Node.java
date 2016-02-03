/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.util.ArrayList;
import java.util.List;

import com.cloudera.director.vsphere.compute.VSphereComputeInstanceTemplate;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema.Disk;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.resources.PoolResource;
import com.cloudera.director.vsphere.utils.PlacementUtil;
import com.vmware.vim25.VirtualDiskMode;

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
   private String network;

   // target datastore for system disk
   private DatastoreResource targetDatastore;
   private HostResource targetHost;
   private PoolResource targetPool;
   private String datastoreType;
   private int key;

   private List<DiskSpec> disks;

   private VmSchema vmSchema;

   public Node(String vmName) {
      this.vmName = vmName;
   }

   public Node(String instanceId, VSphereComputeInstanceTemplate template, String prefix, String networkName) {
      this.templateVm = template.getTemplateVm();
      this.vmName = prefix + "-" + instanceId;
      this.instanceId = instanceId;
      this.numCPUs = template.getNumCPUs();
      this.memorySizeGB = template.getMemorySize();
      this.swapDiskSizeGB = template.getMemorySize();
      this.dataDiskSizeGB = template.getDataDiskSize();
      this.network = template.getNetwork();
      this.datastoreType = template.getStorageType();
      this.key = -1;

      this.vmSchema = new VmSchema();
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
    * @return the targetDatastore
    */
   public DatastoreResource getTargetDatastore() {
      return targetDatastore;
   }

   /**
    * @param targetDatastore the targetDatastore to set
    */
   public void setTargetDatastore(DatastoreResource targetDatastore) {
      this.targetDatastore = targetDatastore;
   }

   /**
    * @return the targetHost
    */
   public HostResource getTargetHost() {
      return targetHost;
   }

   /**
    * @param targetHost the targetHost to set
    */
   public void setTargetHost(HostResource targetHost) {
      this.targetHost = targetHost;
   }

   /**
    * @return the targetPool
    */
   public PoolResource getTargetPool() {
      return targetPool;
   }

   /**
    * @param targetPool the targetPool to set
    */
   public void setTargetPool(PoolResource targetPool) {
      this.targetPool = targetPool;
   }

   /**
    * @return the vmSchema
    */
   public VmSchema getVmSchema() {
      return vmSchema;
   }

   /**
    * @param vmSchema the vmSchema to set
    */
   public void setVmSchema(VmSchema vmSchema) {
      this.vmSchema = vmSchema;
   }

   /**
    * @return the disks
    */
   public List<DiskSpec> getDisks() {
      return disks;
   }

   /**
    * @param disks the disks to set
    */
   public void setDisks(List<DiskSpec> disks) {
      this.disks = disks;
   }

   /**
    * @return the datastoreType
    */
   public String getDatastoreType() {
      return datastoreType;
   }

   /**
    * @param datastoreType the datastoreType to set
    */
   public void setDatastoreType(String datastoreType) {
      this.datastoreType = datastoreType;
   }

   /**
    * @return the key
    */
   public int getKey() {
      return key;
   }

   /**
    * @param key the key to set
    */
   public void setKey(int key) {
      this.key = key;
   }

   public void toDiskSchema() {
      // generate disk schema
      ArrayList<Disk> tmDisks = new ArrayList<Disk>();

      // transform DiskSpec to TM.VmSchema.DiskSchema
      int lsiScsiIndex = 0;
      int paraVirtualScsiIndex = 0;

      for (DiskSpec disk : this.disks) {
         if (!disk.isSystemDisk()) {
            Disk tmDisk = new Disk();
            tmDisk.name = disk.getName();
            tmDisk.initialSizeMB = disk.getSize() * 1024;
            tmDisk.datastore = disk.getTargetDs();
            tmDisk.mode = VirtualDiskMode.independent_persistent;
            if ( disk.isSwapDisk() ) {
               tmDisk.externalAddress = PlacementUtil.getSwapAddress();
            } else {
               if (DiskScsiControllerType.LSI_CONTROLLER.equals(disk
                     .getController())) {
                  if (lsiScsiIndex == PlacementUtil.CONTROLLER_RESERVED_CHANNEL) {
                     // controller reserved channel, *:7, cannot be used by custom disk
                     lsiScsiIndex++;
                  }
                  tmDisk.externalAddress = PlacementUtil.LSI_CONTROLLER_EXTERNAL_ADDRESS_PREFIX + lsiScsiIndex;
                  lsiScsiIndex++;
               } else {
                  tmDisk.externalAddress = PlacementUtil.getParaVirtualAddress(paraVirtualScsiIndex);
                  paraVirtualScsiIndex = PlacementUtil.getNextValidParaVirtualScsiIndex(paraVirtualScsiIndex);
               }
            }
            tmDisk.allocationType = AllocationType.valueOf(disk.getAllocType());
            tmDisk.type = disk.getDiskType().getType();
            tmDisks.add(tmDisk);
         }
      }

      DiskSchema diskSchema = new DiskSchema();
      diskSchema.setName("Disk Schema");
      diskSchema.setDisks(tmDisks);
      this.vmSchema.diskSchema = diskSchema;
   }

   public String getNetwork() {
      return network;
   }

   public void setNetwork(String network) {
      this.network = network;
   }
}