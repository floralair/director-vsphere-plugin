package com.cloudera.director.vsphere;

import java.net.URL;

import com.vmware.vim25.mo.ServiceInstance;

public class VSphereCredentials {

   private final ServiceInstance serviceInstance;

   public VSphereCredentials(String vcServer, String port, String username, String password) {
      try {
         this.serviceInstance = new ServiceInstance(new URL("https://" + vcServer + ":" + port + "/sdk"), username, password, true);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public ServiceInstance getServiceInstance() {
      return serviceInstance;
   }

}
