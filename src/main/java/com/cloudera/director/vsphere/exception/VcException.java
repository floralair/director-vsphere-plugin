/**
 *
 */
package com.cloudera.director.vsphere.exception;




/**
 * @author chiq
 *
 */
public class VcException extends VsphereDirectorException {

   public VcException() {}

   private VcException(Throwable t, String errorId, Object ... args) {
      super(t, "VC", errorId, args);
   }

   public static VcException UNSUPPORTED_CONTROLLER_TYPE(String controllerType) {
      return new VcException(null, "UNSUPPORTED_CONTROLLER_TYPE", controllerType);
   }

   public static VcException INTERNAL() {
      return INTERNAL(null);
   }

   public static VcException INTERNAL(Throwable ex) {
      return new VcException(ex, "VSPHERE_DIRECTOR", "INTERNAL");
   }

   /**
    * @param string
    * @return
    */
   public static Exception CONTROLLER_NOT_FOUND(String string) {
      // TODO Auto-generated method stub
      return null;
   }
}
