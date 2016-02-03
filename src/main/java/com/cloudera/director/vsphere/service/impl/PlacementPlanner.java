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
      for (Node node : group.getNodes()) {
         List<DiskSpec> disks;

         // system and swap disk
         List<DiskSpec> systemDisks = new ArrayList<DiskSpec>();

         // un-separable disks
         List<DiskSpec> unseparable = new ArrayList<DiskSpec>();

         // separable disks
         List<DiskSpec> separable = new ArrayList<DiskSpec>();

         for (DiskSpec disk : node.getDisks()) {
            if (DiskType.DATA_DISK == disk.getDiskType()) {
               if (disk.isSeparable()) {
                  // TODO Add separable disks in future
               } else {
                  unseparable.add(disk);
               }
            } else {
               systemDisks.add(disk);
            }
         }

         // place system disks first
         disks = placeUnSeparableDisks(node, systemDisks, host2DatastoreMap);
         if (disks == null) {
            logger.info("There is no enough free space to place " + getDiskSize(systemDisks) + " GB system disk.");
         }

         // place un-separable disks
         List<DiskSpec> subDisks = null;
         if (unseparable != null && unseparable.size() != 0) {
            subDisks = placeUnSeparableDisks(node, unseparable, host2DatastoreMap);
            if (subDisks == null) {
               logger.info("There is no enough free space to place " + getDiskSize(unseparable) + " GB unseparable disk.");
            } else {
               disks.addAll(subDisks);
            }
         }
         node.toDiskSchema();
      }
   }

   private List<DiskSpec> placeUnSeparableDisks(Node node, List<DiskSpec> disks, Map<HostResource, List<DatastoreResource>> host2DatastoreMap) {
      List<DiskSpec> result = new ArrayList<DiskSpec>();

      Collections.sort(disks, Collections.reverseOrder());
      for (HostResource host : host2DatastoreMap.keySet()) {
         if (node.getTargetHost() != null && node.getTargetHost().equals(host)) {
            continue;
         }

         List<DatastoreResource> datastores = host2DatastoreMap.get(host);

         // balance the datastore usage among multiple calls
         Collections.shuffle(datastores);

         for (DiskSpec disk : disks) {
            int i = 0;
            for (; i < datastores.size(); i++) {
               DatastoreResource ds = datastores.get(i);
               if (disk.getSize() <= ds.getFreeSpace()) {
                  disk.setTargetDs(ds.getName());
                  ds.allocate(disk.getSize());
                  result.add(disk);
                  Collections.rotate(datastores, 1);
                  if (disk.isSystemDisk()) {
                     node.setTargetDatastore(ds);
                  }
                  node.setTargetHost(host);
                  node.setTargetPool(host.getCluster().getPool());
                  break;
               }
            }
            // cannot find a datastore to hold this disk
            if (i >= datastores.size()) {
               logger.error("placeUnSeparableDisks: not sufficient storage space to place disk " + disk.toString());
               return null;
            }
         }
      }
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

}
