/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

public enum DiskSplitPolicy {
   // separate this disk on datastores as much as possible
   EVEN_SPLIT,
   // separate this disk on datastore as less as possible
   AGGREGATE,
   // separate this disk onto at most two datastores, first try with two if possible
   BI_SECTOR
}