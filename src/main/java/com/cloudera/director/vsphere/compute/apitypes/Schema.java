/**
 *
 */
package com.cloudera.director.vsphere.compute.apitypes;

import java.lang.reflect.Field;

/**
 * @author chiq
 *
 */
abstract public class Schema {

   @Override
   public String toString() {
      StringBuilder result = new StringBuilder();
      String newLine = System.getProperty("line.separator");

      result.append(this.getClass().getName());
      result.append(" Object {");
      result.append(newLine);

      //determine fields declared in this class only (no fields of superclass)
      Field[] fields = this.getClass().getDeclaredFields();

      //print field names paired with their values
      for (Field field : fields) {
         result.append("  ");
         try {
            result.append(field.getName());
            result.append(": ");
            // requires access to private field:
            result.append(fieldValueOf(field));
         } catch (IllegalAccessException ex) {
            System.out.println(ex);
         }
         result.append(newLine);
      }
      result.append("}");

      return result.toString();
   }

   abstract protected Object fieldValueOf(Field field)
         throws IllegalArgumentException, IllegalAccessException;

}
