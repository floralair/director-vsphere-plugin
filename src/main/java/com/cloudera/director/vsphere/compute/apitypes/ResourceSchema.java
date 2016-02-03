/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author chiq
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "resourceSchema")
public class ResourceSchema extends Schema {

   @XmlAttribute
   public String name;

   @XmlAttribute(required = true)
   public int numCPUs;

   @XmlAttribute(required = true)
   public long cpuReservationMHz;

   @XmlAttribute(required = true)
   public long memSize;

   @XmlAttribute(required = true)
   public long memReservationSize;

   @XmlAttribute(required = true)
   public Priority priority;

   @XmlAttribute()
   public LatencyPriority latencySensitivity;

   @Override
   protected Object fieldValueOf(Field field) throws IllegalArgumentException,
         IllegalAccessException {
      return field.get(this);
   }
}
