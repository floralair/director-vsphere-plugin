/**
 *
 */
package com.cloudera.director.vsphere.utils;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * @author chiq
 *
 */
public class NetworkUtil {

   public static Network getNetwork(ServiceInstance serviceInstance, String networkName) throws Exception {
      Network network = (Network) new InventoryNavigator(serviceInstance.getRootFolder()).searchManagedEntity("Network", networkName);
      if (network == null) {
         throw new VsphereDirectorException("Can not find the network " + networkName + " in vCenter server.");
      }
      return network;
   }

}