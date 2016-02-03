/**
 *
 */
package com.cloudera.director.vsphere.exception;



/**
 * @author chiq
 *
 */
public class VsphereDirectorException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   public VsphereDirectorException() {
      super();
   }

   public VsphereDirectorException (String msg) {
      super(msg);
   }

   public VsphereDirectorException(String msg, Throwable cause) {
      super(msg, cause);
   }

   public VsphereDirectorException(Throwable cause) {
      super(cause);
   }

   protected VsphereDirectorException(Throwable cause, String section, String errorId, Object... args) {
      super(formatErrorMessage(section + "." + errorId, args), cause);
   }

   private static String formatErrorMessage(final String err, Object... args) {
      return String.format(err, args);
   }

   public static VsphereDirectorException INTERNAL(Throwable ex) {
      return new VsphereDirectorException(ex, "VSPHERE_DIRECTOR", "INTERNAL");
   }
}
