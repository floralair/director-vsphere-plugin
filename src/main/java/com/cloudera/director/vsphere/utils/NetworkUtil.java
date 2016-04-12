/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.util.List;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.cloudera.director.vsphere.service.impl.HostResourceManager;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * @author chiq
 *
 */
public class NetworkUtil {

   public static void validateNetwork(ServiceInstance serviceInstance, String networkName) throws Exception {
      HostResourceManager hostResourceManager = new HostResourceManager(serviceInstance.getRootFolder());
      List<String> networkNames = hostResourceManager.getNetworkNames();
      if (!networkNames.contains(networkName)) {
         throw new VsphereDirectorException("Can not find the network " + networkName + " in vCenter server.");
      }
   }

}