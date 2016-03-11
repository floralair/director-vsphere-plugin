package com.cloudera.director.vsphere;

import java.net.URL;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;
import com.vmware.vim25.mo.ServiceInstance;

public class VSphereCredentials {

   private final ServiceInstance serviceInstance;

   public VSphereCredentials(String vcServer, String port, String username, String password) {
      try {
         this.serviceInstance = new ServiceInstance(new URL("https://" + vcServer + ":" + port + "/sdk"), username, password, true);
      } catch (Exception e) {
         throw new VsphereDirectorException("Login to vCenter server " + vcServer + " failed. Please check the vCenter server address, port, username and password.", e);
      }
   }

   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

}
