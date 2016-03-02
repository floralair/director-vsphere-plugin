/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.cloudera.director.vsphere.compute.apitypes.DeviceId;
import com.cloudera.director.vsphere.compute.apitypes.DiskCreateSpec;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema;
import com.cloudera.director.vsphere.compute.apitypes.DiskSchema.Disk;
import com.cloudera.director.vsphere.compute.apitypes.DiskSize;
import com.cloudera.director.vsphere.compute.apitypes.DiskType;
import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.cloudera.director.vsphere.compute.apitypes.VmSchema;
import com.cloudera.director.vsphere.resources.DatastoreResource;

/**
 * @author chiq
 *
 */
public class DiskSchemaUtil {

   public static DiskSchema getSchema(String xmlSchema) throws JAXBException {
      return SchemaUtil.getSchema(xmlSchema, DiskSchema.class);
   }

   public static DiskSchema getSchema(File file) throws JAXBException {
      return SchemaUtil.getSchema(file, DiskSchema.class);
   }

   public static void getTemplateDiskMap(VmSchema vmSchema,
         HashMap<String, Disk.Operation> diskMap) {
      for (DiskSchema.Disk disk : vmSchema.diskSchema.getDisks()) {
         diskMap.put(disk.externalAddress, Disk.Operation.CLONE);
      }
   }

   public static List<DiskCreateSpec> getDisksToAdd(Node node, HashMap<String, Disk.Operation> diskMap) {
      List<DiskCreateSpec> result = new ArrayList<DiskCreateSpec>();

      for (DiskSchema.Disk disk : node.getVmSchema().diskSchema.getDisks()) {
         if (disk.vmdkPath != null && !disk.vmdkPath.isEmpty()) {
            // existed virtual disk, no need to create, need to attach.
            continue;
         }
         if (DiskType.SYSTEM_DISK.getType().equals(disk.type)) {
            // system disk is either be cloned or attached, it will never be added.
            continue;
         }

         if (disk.attributes != null
               && disk.attributes.contains(DiskSchema.DiskAttribute.PROMOTE)) {
            diskMap.put(disk.externalAddress, Disk.Operation.PROMOTE);
         } else {
            // Make sure we don't already have an existing disk of the same address
            VsphereDirectorAssert.check(diskMap.get(disk.externalAddress) == null);
            DatastoreResource diskDs = node.getTargetHost().getDatastore(disk.datastore);
            DiskCreateSpec createSpec = new DiskCreateSpec(new DeviceId(disk.externalAddress), diskDs, disk.name, disk.mode, DiskSize.sizeFromMB(disk.initialSizeMB), disk.allocationType);
            result.add(createSpec);
         }
      }

      return result;
   }
}
