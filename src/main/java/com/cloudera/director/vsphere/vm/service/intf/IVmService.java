/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.mo.VirtualMachine;

public interface IVmService {

   /**
    * @param vmName
    * @param cloneName
    * @param numCPUs
    * @param memoryMB
    * @param targetDatastore
    * @param targetHost
    * @param targetPool
    * @return
    * @throws Exception
    */
   public boolean clone(String vmName, String cloneName, int numCPUs, long memoryMB, ManagedObjectReference targetDatastore, ManagedObjectReference targetHost, ManagedObjectReference targetPool) throws Exception;

   /**
    * @param vmName
    * @param operation
    * @throws Exception
    */
   public void powerOps(String vmName, String operation) throws Exception;

   /**
    * @param node
    * @throws Exception
    */
   public void configureVm(Node node) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   public boolean destroyVm(String vmName) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   public VirtualMachine getVm(String vmName) throws Exception;

   /**
    * @param vmName
    * @throws Exception
    */
   public void waitVmReady(String vmName) throws Exception;

   /**
    * @param vmName
    * @return
    * @throws Exception
    */
   public String getIpAddress(String vmName) throws Exception;

}
