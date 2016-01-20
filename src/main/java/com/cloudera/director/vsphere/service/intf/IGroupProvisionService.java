/**
 *
 */
package com.cloudera.director.vsphere.service.intf;

import java.util.Map;

/**
 * @author chiq
 *
 */
public interface IGroupProvisionService {

   /**
    * @throws Exception
    */
   void provision() throws Exception;

   /**
    * @return
    */
   Map<String, String> getAllocations();

}
