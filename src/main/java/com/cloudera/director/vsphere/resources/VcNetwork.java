package com.cloudera.director.vsphere.resources;

import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.NetworkSummary;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.mo.DistributedVirtualPortgroup;
import com.vmware.vim25.mo.DistributedVirtualSwitch;
import com.vmware.vim25.mo.ManagedObject;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServerConnection;


public class VcNetwork {

   private NetworkSummary summary;
   // The following fields are not null iff this is a DV portgroup.
   private String portgroupKey = null;
   private String switchUuid = null;
   private boolean isUplink = false;

   public void update(ServerConnection sc, ManagedObject mo) throws Exception {
      Network network = (Network) mo;
      summary = network.getSummary();
      portgroupKey = null;
      switchUuid = null;
      isUplink = false;
      if (network instanceof DistributedVirtualPortgroup){
         DistributedVirtualPortgroup pg = (DistributedVirtualPortgroup) network;
         DistributedVirtualSwitch dvs =
               new DistributedVirtualSwitch(sc, pg.getConfig().getDistributedVirtualSwitch());
         for (ManagedObjectReference ref : dvs.getConfig().getUplinkPortgroup()) {
            if (ref.equals(network.getMOR())){
               isUplink = true;
               break;
            }
         }
         portgroupKey = pg.getKey();
         switchUuid = dvs.getUuid();
      }
   }

   public String getName() {
      return summary.getName();
   }

   public boolean isDvPortGroup() {
      return portgroupKey != null;
   }

   public boolean isUplink() {
      return isUplink;
   }

   public VirtualDeviceBackingInfo getBackingInfo() {
      if (isDvPortGroup()) {
         DistributedVirtualSwitchPortConnection conn = new DistributedVirtualSwitchPortConnection();
         conn.setPortgroupKey(portgroupKey);
         conn.setSwitchUuid(switchUuid);
         VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
         nicBacking.setPort(conn);
         return nicBacking;
      } else {
         Boolean autoConnect = true;
         //TODO: need to handle standard portgroup
         return new VirtualEthernetCardNetworkBackingInfo();
      }
   }

   @Override
   public String toString() {
      if (isDvPortGroup()) {
         return String.format("NETPG[%s,id=%s,up=%b]",
               summary.getName(), portgroupKey, isUplink);
      } else {
         return String.format("NET[%s]", summary.getName());
      }
   }
}