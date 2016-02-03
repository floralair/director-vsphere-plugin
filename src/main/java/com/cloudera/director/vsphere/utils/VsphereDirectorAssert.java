/**
 *
 */
package com.cloudera.director.vsphere.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.cloudera.director.vsphere.exception.VsphereDirectorException;

/**
 * @author chiq
 *
 */
@SuppressWarnings("serial")
public class VsphereDirectorAssert extends VsphereDirectorException {
   private static final Logger logger = Logger.getLogger(VsphereDirectorAssert.class);

   // Default public constructor. This should only be used by AMF client.
   public VsphereDirectorAssert() {}

   private VsphereDirectorAssert(String errorId) {
      super(null, "AUASSERT", errorId);
   }

   private static VsphereDirectorAssert FAILURE() {
      return new VsphereDirectorAssert("FAILURE");
   }

   private static void log(VsphereDirectorAssert exc, long[] bugIds, String msg, Level level) {
      StringBuffer banner = new StringBuffer("Assertion failure");
      if (bugIds != null) {
         banner.append("(");
         for (int i = 0; i < bugIds.length; i++) {
            banner.append("PR ").append(bugIds[i]);
            if (i < bugIds.length - 1) {
               banner.append(",");
            }
         }
         banner.append(")");
      }
      if (msg != null) {
         banner.append(": ").append(msg);
      } else {
         banner.append(".");
      }
      logger.log(level, banner, exc);
   }

   private static void check(boolean cond, long[] bugIds, String message) {
      if (!cond) {
         VsphereDirectorAssert exc = FAILURE();
         // Log this assertion, even though the top-level will likely log it too.
         // We have several threads that don't log exceptions at their top level,
         // which is worse than logging an assertion failure twice.
         log(exc, bugIds, message, Level.ERROR);
         throw exc;
      }
   }

   private static void warn(boolean cond, long[] bugIds, String message) {
      if (!cond) {
         VsphereDirectorAssert exc = FAILURE();
         log(exc, bugIds, message, Level.WARN);
      }
   }

   public static void check(boolean cond, String message) {
      check(cond, null, message);
   }

   public static void check(boolean cond) {
      check(cond, null, null);
   }

   public static void checkBugs(boolean cond, long ... bugIds) {
      check(cond, bugIds, null);
   }

   public static void warnBugs(boolean cond, long ...bugIds) {
      warn(cond, bugIds, null);
   }

   /**
    * Assert that the code path should never be reached.
    */
   public static void unreachable() {
      check(false, "should not be reached");
   }

   /**
    * Assert that the code has not been implemented.
    */
   public static void notImplemented() {
      check(false, "has not been implemented");
   }
}
