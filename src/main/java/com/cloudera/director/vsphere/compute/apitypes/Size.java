/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.io.Serializable;

/**
 * @author chiq
 *
 */
@SuppressWarnings("serial")
public class Size implements Serializable {
   protected long sizeInBytes;

   protected Size(long sizeInBytes) {
      this.sizeInBytes = sizeInBytes;
   }

   public long getSize() {
      return sizeInBytes;
   }

   public float getFloat() {
      return sizeInBytes;
   }

   public long getKiB() {
      return sizeInBytes >> 10;
   }

   public float getKiBFloat() {
      return (float)sizeInBytes / (1 << 10);
   }

   public long getMiB() {
      return sizeInBytes >> 20;
   }

   public float getMiBFloat() {
      return (float)sizeInBytes / (1 << 20);
   }

   public long getGiB() {
      return sizeInBytes >> 30;
   }

   public float getGiBFloat() {
      return (float)sizeInBytes / (1 << 30);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof Size) {
         return sizeInBytes == ((Size)obj).sizeInBytes;
      }
      return false;
   }
   @Override
   public int hashCode() {
      return new Long(sizeInBytes).hashCode();
   }

   protected long addOp(long size) {
      return sizeInBytes + size;
   }

   protected long addOp(long ... sizes) {
      long size = sizeInBytes;
      for (int i = 0; i < sizes.length; i++) {
         size += sizes[i];
      }
      return size;
   }

   protected long addOp(Size ... sizes) {
      long size = sizeInBytes;
      for (int i = 0; i < sizes.length; i++) {
         size += sizes[i].sizeInBytes;
      }
      return size;
   }

   protected long subOp(long size) {
      return sizeInBytes - size;
   }

   protected long subOp(long ... sizes) {
      long size = sizeInBytes;
      for (int i = 0; i < sizes.length; i++) {
         size -= sizes[i];
      }
      return size;
   }

   protected long subOp(Size ... sizes) {
      long size = sizeInBytes;
      for (int i = 0; i < sizes.length; i++) {
         size -= sizes[i].sizeInBytes;
      }
      return size;
   }

   protected long negOp() {
      return -sizeInBytes;
   }
}