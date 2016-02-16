/***************************************************************************
 * Copyright (c) 2012-2013 VMware, Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package com.cloudera.director.vsphere.resources;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;


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


   /* (non-Javadoc)
    * @see com.vmware.aurora.vc.VcNetwork#getName()
    */

   public String getName() {
      return summary.getName();
   }

   public boolean isDvPortGroup() {
      return portgroupKey != null;
   }

   /* (non-Javadoc)
    * @see com.vmware.aurora.vc.VcNetwork#isUplink()
    */

   public boolean isUplink() {
      return isUplink;
   }

   /* (non-Javadoc)
    * @see com.vmware.aurora.vc.VcNetwork#getBackingInfo()
    */

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

   public String toString() {
      if (isDvPortGroup()) {
         return String.format("NETPG[%s,id=%s,up=%b]",
               summary.getName(), portgroupKey, isUplink);
      } else {
         return String.format("NET[%s]", summary.getName());
      }
   }
}