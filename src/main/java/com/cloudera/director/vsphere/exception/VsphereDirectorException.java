/**
 *
 */
package com.cloudera.director.vsphere.exception;


/**
 * @author chiq
 *
 */
public class VsphereDirectorException extends RuntimeException {

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

}
