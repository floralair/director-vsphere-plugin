/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "networkSchema")
public class NetworkSchema extends Schema {

   public static class Network extends Schema {
      @XmlAttribute()
      public String nicLabel;

      @XmlAttribute()
      public String vcNetwork;

      @Override
      protected Object fieldValueOf(Field field)
            throws IllegalArgumentException, IllegalAccessException {
         return field.get(this);
      }

   };

   @XmlAttribute()
   public String name;

   @XmlAttribute()
   public String parent;

   @XmlElementWrapper(name = "networks")
   @XmlElement(name = "network")
   public ArrayList<Network> networks;

   @Override
   protected Object fieldValueOf(Field field) throws IllegalArgumentException,
         IllegalAccessException {
      return field.get(this);
   }

}
