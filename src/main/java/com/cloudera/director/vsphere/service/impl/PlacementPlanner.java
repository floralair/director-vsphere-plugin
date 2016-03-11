/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cloudera.director.vsphere.compute.apitypes.AllocationType;
import com.cloudera.director.vsphere.compute.apitypes.DiskScsiControllerType;
import com.cloudera.director.vsphere.compute.apitypes.DiskSpec;
import com.cloudera.director.vsphere.compute.apitypes.DiskType;
import com.cloudera.director.vsphere.compute.apitypes.Group;
import com.cloudera.director.vsphere.compute.apitypes.LatencyPriority;
import com.cloudera.director.vsphere.compute.apitypes.NetworkSchema;
import com.cloudera.director.vsphere.compute.apitypes.NetworkSchema.Network;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.compute.apitypes.Priority;
import com.cloudera.director.vsphere.compute.apitypes.ResourceSchema;
import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.resources.DatastoreResource;
import com.cloudera.director.vsphere.resources.HostResource;
import com.cloudera.director.vsphere.service.intf.IPlacementPlanner;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Folder;

/**
 * @author chiq
 *
 */
public class PlacementPlanner implements IPlacementPlanner {
   static final Logger logger = Logger.getLogger(PlacementPlanner.class);

   private Group group;
   private final Folder rootFolder;
   private List<HostResource> hostResources;

   public PlacementPlanner(Folder rootFolder, Group group) throws InvalidProperty, RuntimeFault, RemoteException {
      this.group = group;
      this.rootFolder = rootFolder;
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
    * @return the hostResources
    */
   public List<HostResource> getHostResources() {
      return hostResources;
   }

   /**
    * @param hostResources the hostResources to set
    */
   public void setHostResources(List<HostResource> hostResources) {
      this.hostResources = hostResources;
   }

   @Override
   public void init() throws Exception {
      HostResourceManager hostResourceManager = new HostResourceManager(this.rootFolder);
      this.hostResources = hostResourceManager.filterHostsByNetwork(group.getNetworkName());
   }

   @Override
   public void initNodeDisks() throws Exception {
      for (Node node : group.getNodes()) {

         // initialize disks
         List<DiskSpec> disks = new ArrayList<DiskSpec>();

         DiskSpec systemDisk = new DiskSpec(group.getTemplateNode().getDisks().get(0));
         systemDisk.setDiskType(DiskType.SYSTEM_DISK);
         systemDisk.setSeparable(false);
         disks.add(systemDisk);

         // THICK as by default
         AllocationType diskAllocType = AllocationType.THICK;

         // swap disk
         long swapDisk = node.getSwapDiskSizeGB();
         disks.add(new DiskSpec(DiskType.SWAP_DISK.getDiskName(), swapDisk, node.getVmName(), false, DiskType.SWAP_DISK,
               DiskScsiControllerType.LSI_CONTROLLER, null, diskAllocType.toString(), null, null, null));

         // data disks
         disks.add(new DiskSpec(DiskType.DATA_DISK.getDiskName(), node.getDataDiskSizeGB(), node.getVmName(), false,
               DiskType.DATA_DISK, DiskScsiControllerType.PARA_VIRTUAL_CONTROLLER, null, diskAllocType.toString(), null, null, null));

         node.setDisks(disks);

         // target network, hard coded as the only one NIC
         NetworkSchema netSchema = new NetworkSchema();

         ArrayList<Network> networks = new ArrayList<Network>();
         netSchema.networks = networks;

         // TODO: enhance this logic to support nodegroup level networks
         Network network = new Network();
         network.vcNetwork = node.getNetwork();
         networks.add(network);

         node.getVmSchema().networkSchema = netSchema;

         // resource schema
         ResourceSchema resourceSchema = new ResourceSchema();
         resourceSchema.numCPUs = node.getNumCPUs();
         // we don't reserve cpu resource
         resourceSchema.cpuReservationMHz = 0;
         resourceSchema.memSize = node.getMemorySizeGB();
         resourceSchema.memReservationSize = 0;
         resourceSchema.name = "Resource Schema";
         resourceSchema.priority = Priority.Normal;
         resourceSchema.latencySensitivity = LatencyPriority.NORMAL;
         node.getVmSchema().resourceSchema = resourceSchema;
      }
   }

   @Override
   public void placeDisk() {
      for (Node node : group.getNodes()) {
         List<DiskSpec> disks;

         disks = placeUnSeparableDisks(node);
         if (disks == null) {
            logger.info("There is no enough free space to place " + node.getTotalDiskSize() + " GB disk.");
         }

         node.toDiskSchema();
      }
   }

   private List<DiskSpec> placeUnSeparableDisks(Node node) {
      List<DiskSpec> result = new ArrayList<DiskSpec>();

      HostResource targetHost = getTargetHost(node);
      DatastoreResource targetDatastore  = targetHost.getNodeTargetDatastore();

      for (DiskSpec disk : node.getDisks()) {
         disk.setTargetDs(targetDatastore.getName());
         targetDatastore.allocate(disk.getSize());
         result.add(disk);
         if (disk.isSystemDisk()) {
            node.setTargetDatastore(targetDatastore);
         }
         syncUpHostMounts(targetDatastore);
         node.setTargetHost(targetHost);
         node.setTargetPool(targetHost.getCluster().getPool());
      }
      targetDatastore.setNodesCount(targetDatastore.getNodesCount() + 1);
      targetHost.setNodeTargetDatastore(null);
      targetHost.setNodesCount(targetHost.getNodesCount() + 1);

      logger.info("The node " + node.getVmName() + " is placed to host " + node.getTargetHost().getName() + " datastore " + node.getTargetDatastore().getName());

      return result;
   }

   private HostResource getTargetHost(Node node) {
      HostResource targetHost = null;
      for (HostResource host : hostResources) {
         if (host.getTargetDatastore(node) == null) {
            continue;
         }

         if (targetHost == null) {
            targetHost = host;
         } else {
            if (targetHost.getNodesCount() > host.getNodesCount()) {
               targetHost = host;
            }
         }
      }

      if (targetHost == null) {
         logger.error("placeUnSeparableDisks: not sufficient " + node.getStorageType() + " storage space to place node " + node.getVmName());
         throw new VsphereDirectorException("Not sufficient " + node.getStorageType() + " storage space to place node " + node.getVmName());
      }

      return targetHost;
   }

   private void syncUpHostMounts(DatastoreResource datastore) {
      for (String hostMount : datastore.getHostMounts()) {
         for (HostResource host : hostResources) {
            if (host.getName().equals(hostMount)) {
               for (DatastoreResource oldDatastore : host.getDatastores()) {
                  if (datastore.getName().equals(oldDatastore.getName())) {
                     oldDatastore.setFreeSpace(datastore.getFreeSpace());
                  }
               }
            }
         }
      }
   }

}
