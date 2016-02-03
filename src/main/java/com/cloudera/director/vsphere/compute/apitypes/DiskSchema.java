/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.vmware.vim25.VirtualDiskMode;

/**
 * @author chiq
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "diskSchema")
public class DiskSchema extends Schema {
   public enum DiskAttribute {
      PROMOTE
   }

   public static class Disk extends Schema {
      public enum Operation {
         CLONE, ADD, REMOVE, PROMOTE
      }

      @XmlAttribute()
      public String name;

      @XmlAttribute()
      public String type;

      @XmlAttribute()
      public AllocationType allocationType;

      @XmlAttribute()
      public String externalAddress;

      @XmlAttribute()
      public long initialSizeMB;

      @XmlAttribute()
      public VirtualDiskMode mode;

      @XmlAttribute()
      public String datastore;

      @XmlAttribute()
      public String vmdkPath;

      @XmlElementWrapper(name = "diskAttributes")
      @XmlElement(name = "diskAttribute")
      public ArrayList<DiskAttribute> attributes;

      @Override
      protected Object fieldValueOf(Field field)
            throws IllegalArgumentException, IllegalAccessException {
         return field.get(this);
      }
   };

   @XmlAttribute()
   private String name;

   @XmlElementWrapper(name = "disks")
   @XmlElement(name = "disk")
   private List<Disk> disks;

   @XmlAttribute()
   private String parent;

   @XmlAttribute()
   private String parentSnap;

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public void setDisks(ArrayList<Disk> disks) {
      this.disks = disks;
   }

   public List<Disk> getDisks() {
      return this.disks;
   }

   public void setParent(String parent) {
      this.parent = parent;
   }

   public String getParent() {
      return this.parent;
   }

   public void setParentSnap(String parentSnap) {
      this.parentSnap = parentSnap;
   }

   public String getParentSnap() {
      return this.parentSnap;
   }

   @Override
   protected Object fieldValueOf(Field field) throws IllegalArgumentException,
         IllegalAccessException {
      return field.get(this);
   }
}
