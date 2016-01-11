package com.cloudera.director.vsphere.compute;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import com.cloudera.director.spi.v1.compute.util.AbstractComputeInstance;

/**
  * @author chiq
  *
  */
public class VSphereComputeInstance
    extends AbstractComputeInstance<VSphereComputeInstanceTemplate, Void> {

  public static final Type TYPE = new ResourceType("VSphereComputeInstance");

  protected VSphereComputeInstance(VSphereComputeInstanceTemplate template,
      String identifier, InetAddress privateIpAddress) {
    super(template, identifier, privateIpAddress);
  }

  @Override
  public Type getType() {
    return TYPE;
  }

  @Override
  public Map<String, String> getProperties() {
    return new HashMap<String, String>();
  }
}
