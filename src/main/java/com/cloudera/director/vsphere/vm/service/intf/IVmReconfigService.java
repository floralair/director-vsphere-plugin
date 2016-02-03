/**
 *
 */
package com.cloudera.director.vsphere.vm.service.intf;

import java.util.Map;

/**
 * @author chiq
 *
 */
public interface IVmReconfigService {

   /**
    * @param guestVariables
    * @throws Exception
    */
   void setMachineIdVariables(Map<String, Object> guestVariables) throws Exception;

   /**
    * @param optionKey
    * @param value
    * @throws Exception
    */
   void setExtraConfig(String optionKey, Object value) throws Exception;

}
