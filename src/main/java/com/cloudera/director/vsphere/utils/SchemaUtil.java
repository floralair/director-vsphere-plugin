/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author chiq
 *
 */
public class SchemaUtil {

   @SuppressWarnings("unchecked")
   public static <T> T getSchema(String xmlSchema, Class<T> clazz)
         throws JAXBException {
      JAXBContext context = JAXBContext.newInstance(clazz);
      Unmarshaller um = context.createUnmarshaller();
      StringReader input = new StringReader(xmlSchema);
      return (T) um.unmarshal(input);
   };

   @SuppressWarnings("unchecked")
   public static <T> T getSchema(File file, Class<T> clazz)
         throws JAXBException {
      JAXBContext context = JAXBContext.newInstance(clazz);
      Unmarshaller um = context.createUnmarshaller();
      return (T) um.unmarshal(file);
   }

   public static <T> String getXmlString(T jaxbObject) throws JAXBException {
      StringWriter sw = new StringWriter();
      JAXBContext context = JAXBContext.newInstance(jaxbObject.getClass());
      Marshaller m = context.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.marshal(jaxbObject, sw);
      return sw.toString();
   }

   public static <T> void putXml(T jaxbObject, File f) {
      JAXB.marshal(jaxbObject, f);
   }

}
