/**
 *
 */
package com.cloudera.director.vsphere.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author chiq
 *
 */
public class CommonUtil {
   private static final Logger logger = Logger.getLogger(CommonUtil.class);

   public static String readJsonFile(final String fileName) {
      StringBuilder jsonBuff = new StringBuilder();
      if (fileName != null) {
         InputStream in = null;
         try {
            in = new FileInputStream(fileName);
            Reader rd = new InputStreamReader(in, "UTF-8");
            int c = 0;
            while ((c = rd.read()) != -1) {
               jsonBuff.append((char) c);
            }
         } catch (IOException e) {
            logger.error(e.getMessage() + "\n Can not find " + fileName + " or IO read error.");
         } finally {
            try {
               if (in != null) {
                  in.close();
               }
            } catch (IOException e) {
               logger.error(e.getMessage() + "\n Can not close " + fileName + ".");
            }
         }
      }
      return jsonBuff.toString();
   }

   /*
    * throws IO Exception
    */
   public static void prettyOutputStrings(Object obj, String fileName, String delimeter) throws Exception {
      StringBuffer buff = new StringBuffer();
      if (isBlank(delimeter)) {
         delimeter = System.lineSeparator();
      }

      if (obj != null) {
         String str = obj.toString();
         if (!isBlank(str)) {
            buff.append(str).append(delimeter);
         }
      }
      if (buff.length() > 0) {
         buff.delete(buff.length() - delimeter.length(), buff.length());
      }

      OutputStream out = null;
      BufferedWriter bw = null;
      try {
         if (!isBlank(fileName)) {
            out = new FileOutputStream(fileName);
         } else {
            out = System.out;
         }
         bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
         bw.write(buff.toString());
         if (!isBlank(fileName)) {
            // [Bug 1406542] always append a newline at the end of the file
            bw.newLine();
         }
         bw.flush();
      } finally {
         if (bw != null && out != null && !(out instanceof PrintStream)) {
            bw.close();
            out.close();
         }
      }
   }

   public static boolean isBlank(final String str) {
      return str == null || str.trim().isEmpty();
   }

   public static <T> T jsonToObject(Class<T> entityClass, String jsonString) {
      T entity = null;
      try {
         Constructor<T> meth = entityClass.getConstructor();
         entity = meth.newInstance();
         Gson gson = new Gson();
         entity = gson.fromJson(jsonString, entityClass);
      } catch (Exception e) {
         logger.error(e.getMessage());
      }
      return entity;
   }

   public static String objectToJson(Object object) {
      String jsonString = null;
      try {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         jsonString = gson.toJson(object);
         JsonParser jp = new JsonParser();
         JsonElement je = jp.parse(jsonString);
         jsonString = gson.toJson(je);
      } catch (Exception e) {
         logger.error(e.getMessage());
      }
      return jsonString;
   }
}
