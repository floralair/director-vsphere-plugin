/**
 *
 */
package com.cloudera.director.vsphere.resourcesplacement;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * @author chiq
 *
 */
public class VcServer {

   @Expose
   private String name;

   @Expose
   private List<VcHost> vcHosts = new ArrayList<VcHost>();

   /**
    * @param name
    */
   public VcServer(String name) {
      this.name = name;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the vcHosts
    */
   public List<VcHost> getVcHosts() {
      return vcHosts;
   }

   /**
    * @param vcHosts the vcHosts to set
    */
   public void setVcHosts(List<VcHost> vcHosts) {
      this.vcHosts = vcHosts;
   }

   public VcHost getVcHostByName(String vcHostName) {
      for (VcHost vcHost : this.vcHosts) {
         if (vcHostName.equals(vcHost.getName())) {
            return vcHost;
         }
      }
      return null;
   }
}
