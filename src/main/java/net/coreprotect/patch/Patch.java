package net.coreprotect.patch;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.Database;
import net.coreprotect.model.Config;

public class Patch extends CoreProtect {
   private static boolean patching = false;

   public static boolean continuePatch() {
      return patching && Config.server_running;
   }

   protected static String getClassVersion(String version) {
      return version.split(".__")[1].replaceAll("_", ".");
   }

   public static Integer[] getLastVersion(Connection connection) {
      Integer[] last_version = new Integer[]{0, 0, 0};

      try {
         String query = "SELECT version FROM " + Config.prefix + "version ORDER BY rowid DESC LIMIT 0, 1";
         Statement statement = connection.createStatement();
         ResultSet rs = statement.executeQuery(query);

         while(rs.next()) {
            String version = rs.getString("version");
            if (!version.contains(".")) {
               int version_int = Integer.parseInt(version);
               version = String.format("%3.2f", (double)version_int / 100.0D);
            }

            version = version.replaceAll(",", ".");
            String[] old_version_split = version.split("\\.");
            if (old_version_split.length > 2) {
               last_version[0] = Integer.parseInt(old_version_split[0]);
               last_version[1] = Integer.parseInt(old_version_split[1]);
               last_version[2] = Integer.parseInt(old_version_split[2]);
            } else {
               int revision = 0;
               String parse = old_version_split[1];
               if (parse.length() > 1) {
                  revision = Integer.parseInt(parse.substring(1));
               }

               last_version[0] = Integer.parseInt(old_version_split[0]);
               last_version[1] = 0;
               last_version[2] = revision;
            }
         }

         rs.close();
         statement.close();
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      return last_version;
   }

   private static List<String> getPatches() {
      ArrayList patches = new ArrayList();

      try {
         File pluginFile = new File(CoreProtect.class.getProtectionDomain().getCodeSource().getLocation().toURI());
         if (pluginFile.getPath().endsWith(".jar")) {
            JarInputStream jarInputStream = new JarInputStream(new FileInputStream(pluginFile));

            while(true) {
               JarEntry jarEntry = jarInputStream.getNextJarEntry();
               if (jarEntry == null) {
                  jarInputStream.close();
                  break;
               }

               String className = jarEntry.getName();
               if (className.startsWith("net/coreprotect/patch/script/__") && className.endsWith(".class")) {
                  Class<?> patchClass = Class.forName(className.substring(0, className.length() - 6).replaceAll("/", "."));
                  String patchVersion = getClassVersion(patchClass.getName());
                  if (!Functions.newVersion(getPluginVersion(), patchVersion)) {
                     patches.add(patchVersion);
                  }
               }
            }
         }

         Collections.sort(patches, new Comparator<String>() {
            public int compare(String o1, String o2) {
               if (Functions.newVersion(o1, o2)) {
                  return -1;
               } else {
                  return Functions.newVersion(o2, o1) ? 1 : 0;
               }
            }
         });
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return patches;
   }

   private static Integer[] getPluginVersion() {
      String[] versionSplit = CoreProtect.getInstance().getDescription().getVersion().split("\\.");
      return new Integer[]{Integer.parseInt(versionSplit[0]), Integer.parseInt(versionSplit[1]), Integer.parseInt(versionSplit[2])};
   }

   public static void processConsumer() {
      try {
         Functions.messageOwner("Processing new data. Please wait...");
         Consumer.is_paused = false;
         Thread.sleep(1000L);

         while(Consumer.is_paused) {
            Thread.sleep(500L);
         }
      } catch (Exception var1) {
         var1.printStackTrace();
      }

   }

   private static int runPatcher(Integer[] last_version, Integer[] version) {
      int result = -1;
      patching = true;

      try {
         boolean patched = false;
         boolean allPatches = true;
         Connection connection = Database.getConnection(true);
         Statement statement = connection.createStatement();
         Integer[] newVersion = last_version;
         if (last_version[1] == 0 && last_version[2] > 0) {
            last_version[1] = last_version[2];
            last_version[2] = 0;
         }

         List<String> patches = getPatches();
         Iterator var9 = patches.iterator();

         while(var9.hasNext()) {
            String patchData = (String)var9.next();
            String[] thePatch = patchData.split("\\.");
            int patchMajor = Integer.parseInt(thePatch[0]);
            int patchMinor = Integer.parseInt(thePatch[1]);
            int patchRevision = Integer.parseInt(thePatch[2]);
            Integer[] patchVersion = new Integer[]{patchMajor, patchMinor, patchRevision};
            boolean performPatch = Functions.newVersion(newVersion, patchVersion);
            if (performPatch) {
               boolean success = false;

               try {
                  Functions.messageOwner("-----");
                  Functions.messageOwner("Performing v" + patchData + " upgrade. Please wait...");
                  Functions.messageOwner("-----");
                  if (continuePatch()) {
                     Class<?> patchClass = Class.forName("net.coreprotect.patch.script.__" + patchData.replaceAll("\\.", "_"));
                     Method patchMethod = patchClass.getDeclaredMethod("patch", Statement.class);
                     patchMethod.setAccessible(true);
                     success = (Boolean)patchMethod.invoke((Object)null, statement);
                  }
               } catch (Exception var20) {
                  var20.printStackTrace();
               }

               if (!success) {
                  allPatches = false;
                  break;
               }

               patched = true;
               newVersion = patchVersion;
            }
         }

         if (allPatches) {
            if (patched) {
               result = 1;
            } else {
               result = 0;
            }
         }

         int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
         if (result >= 0) {
            statement.executeUpdate("INSERT INTO " + Config.prefix + "version (time,version) VALUES ('" + unixtimestamp + "', '" + version[0] + "." + version[1] + "." + version[2] + "')");
         } else if (patched) {
            statement.executeUpdate("INSERT INTO " + Config.prefix + "version (time,version) VALUES ('" + unixtimestamp + "', '" + newVersion[0] + "." + newVersion[1] + "." + newVersion[2] + "')");
         }

         statement.close();
         connection.close();
      } catch (Exception var21) {
         var21.printStackTrace();
      }

      patching = false;
      return result;
   }

   public static boolean versionCheck(Statement statement) {
      try {
         final Integer[] current_version = getPluginVersion();
         final Integer[] last_version = getLastVersion(statement.getConnection());
         boolean newVersion = Functions.newVersion(last_version, current_version);
         if (newVersion && last_version[0] > 0 && !Config.converter_running) {
            Config.converter_running = true;
            Consumer.is_paused = true;

            class patcher implements Runnable {
               public void run() {
                  try {
                     int finished = Patch.runPatcher(last_version, current_version);
                     Config.converter_running = false;
                     if (finished == 1) {
                        Patch.processConsumer();
                        Functions.messageOwner("-----");
                        Functions.messageOwner("Successfully upgraded to v" + CoreProtect.getInstance().getDescription().getVersion() + ".");
                        Functions.messageOwner("-----");
                     } else if (finished == 0) {
                        Consumer.is_paused = false;
                     } else if (finished == -1) {
                        Functions.messageOwner("Upgrade interrupted. Will try again on restart.");
                     }
                  } catch (Exception var2) {
                     var2.printStackTrace();
                  }

               }
            }

            Thread thread = new Thread(new patcher());
            thread.start();
         } else if (last_version[0] == 0) {
            int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
            statement.executeUpdate("INSERT INTO " + Config.prefix + "version (time,version) VALUES ('" + unixtimestamp + "', '" + current_version[0] + "." + current_version[1] + "." + current_version[2] + "')");
         } else {
            current_version[2] = 0;
            last_version[2] = 0;
            if (Functions.newVersion(current_version, last_version)) {
               System.out.println("[CoreProtect] CoreProtect " + last_version[0] + "." + last_version[1] + "." + last_version[2] + " or higher is required.");
               return false;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return true;
   }
}
