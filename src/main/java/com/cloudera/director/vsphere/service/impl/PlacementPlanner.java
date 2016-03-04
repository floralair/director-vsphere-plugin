/**
 *
 */
package com.cloudera.director.vsphere.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      Map<HostResource, List<DatastoreResource>> host2DatastoreMap = getHost2DatastoreMap();
      List<String> usedHosts = new ArrayList<String>();
      for (Node node : group.getNodes()) {
         List<DiskSpec> disks;

         int totalDiskSize = getDiskSize(node.getDisks());
         disks = placeUnSeparableDisks(node, node.getDisks(), totalDiskSize, host2DatastoreMap, usedHosts);
         if (disks == null) {
            logger.info("There is no enough free space to place " + totalDiskSize + " GB disk.");
         }

         node.toDiskSchema();
      }
   }

   private List<DiskSpec> placeUnSeparableDisks(Node node, List<DiskSpec> disks, int totalDiskSize, Map<HostResource, List<DatastoreResource>> host2DatastoreMap, List<String> usedHosts) {
      List<DiskSpec> result = new ArrayList<DiskSpec>();

      for (HostResource host : host2DatastoreMap.keySet()) {
         if (usedHosts.contains(host.getName())) {
            continue;
         }

         List<DatastoreResource> datastores = host2DatastoreMap.get(host);

         // balance the datastore usage among multiple calls
         Collections.shuffle(datastores);

         int i = 0;
         for (; i < datastores.size(); i++) {
            boolean placed = false;
            DatastoreResource ds = datastores.get(i);

            if (node.needLocalStorage() && ds.isSharedStorage()) {
               continue;
            }

            if (node.needSharedStorage() && ds.isLocalStorage()) {
               continue;
            }

            if (totalDiskSize <= ds.getFreeSpace()) {
               for (DiskSpec disk : disks) {
                  disk.setTargetDs(ds.getName());
                  ds.allocate(disk.getSize());
                  result.add(disk);
                  if (disk.isSystemDisk()) {
                     node.setTargetDatastore(ds);
                  }
                  syncUpHostMounts(ds, host2DatastoreMap);
                  node.setTargetHost(host);
                  node.setTargetPool(host.getCluster().getPool());
                  usedHosts.add(host.getName());
               }
               placed = true;
            }

            if (placed) {
               break;
            }
         }

         // cannot find a datastore to hold this node
         if (i >= datastores.size()) {
            logger.error("placeUnSeparableDisks: not sufficient " + node.getStorageType() + " storage space to place node " + node.getVmName());
            throw new VsphereDirectorException("Not sufficient " + node.getStorageType() + " storage space to place node " + node.getVmName());
         }

         if (node.getTargetHost() != null) {
            break;
         }
      }

      if (node.getTargetHost() == null) {
         logger.error("There is no enough host to place node " + node.getVmName());
         throw new VsphereDirectorException("There is no enough host to place node " + node.getVmName());
      }

      logger.info("The node " + node.getVmName() + " is placed to host " + node.getTargetHost().getName() + " datastore " + node.getTargetDatastore().getName());

      return result;
   }

   private int getDiskSize(List<DiskSpec> disks) {
      int size = 0;
      for (DiskSpec disk : disks) {
         size += disk.getSize();
      }
      return size;
   }

   private Map<HostResource, List<DatastoreResource>> getHost2DatastoreMap() {
      Map<HostResource, List<DatastoreResource>> host2DatastoreMap = new HashMap<HostResource, List<DatastoreResource>>();
      for (HostResource host : hostResources) {
         host2DatastoreMap.put(host, host.getDatastores());
      }
      return host2DatastoreMap;
   }

   private void syncUpHostMounts(DatastoreResource datastore, Map<HostResource, List<DatastoreResource>> host2DatastoreMap) {
      for (String hostMount : datastore.getHostMounts()) {
         for (HostResource host : host2DatastoreMap.keySet()) {
            if (host.getName().equals(hostMount)) {
               List<DatastoreResource> oldDatastores = host2DatastoreMap.get(host);
               for (DatastoreResource oldDatastore : oldDatastores) {
                  if (datastore.getName().equals(oldDatastore.getName())) {
                     oldDatastore.setFreeSpace(datastore.getFreeSpace());
                  }
               }
            }
         }
      }
   }

}
