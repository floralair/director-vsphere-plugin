/**
 *
 */
package com.cloudera.director.vsphere.service.intf;

import java.rmi.RemoteException;
import java.util.List;

import com.cloudera.director.vsphere.resources.HostResource;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;

/**
 * @author chiq
 *
 */
public interface IHostResourceManager {

   /**
    * @param networkName
    * @return
    * @throws InvalidProperty
    * @throws RuntimeFault
    * @throws RemoteException
    */
   List<HostResource> filterHostsByNetwork(String networkName) throws InvalidProperty, RuntimeFault, RemoteException;

   /**
    * @return
    * @throws RemoteException
    * @throws RuntimeFault
    * @throws InvalidProperty
    */
   List<String> getNetworkNames() throws InvalidProperty, RuntimeFault, RemoteException;

}
