/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

import java.util.Map;

import com.cloudera.director.vsphere.compute.apitypes.Node;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author chiq
 *
 */
public interface IVmReconfigService {

   /**
    * @param node
    * @throws Exception
    */
   void setVolumesToMachineId(Node node) throws Exception;

   /**
    * @param vm
    * @param guestVariables
    * @throws Exception
    */
   void setMachineIdVariables(VirtualMachine vm, Map<String, Object> guestVariables) throws Exception;

   /**
    * @param vm
    * @param optionKey
    * @param value
    * @throws Exception
    */
   void setExtraConfig(VirtualMachine vm, String optionKey, Object value) throws Exception;

   /**
    * @param node
    * @throws Exception
    */
   void enableDiskUUID(Node node) throws Exception;

   /**
    * @param node
    * @throws Exception
    */
   void configNetworks(Node node) throws Exception;

}
