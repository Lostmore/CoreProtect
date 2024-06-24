package net.coreprotect.command;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.database.Database;
import net.coreprotect.model.Config;
import net.coreprotect.patch.Patch;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PurgeCommand extends Consumer {
   protected static void runCommand(final CommandSender player, boolean permission, String[] args) {
      int resultc = args.length;
      final int seconds = CommandHandler.parseTime(args);
      if (Config.converter_running) {
         player.sendMessage("§3CoreProtect §f- Выполняется обновление. Пожалуйста, повторите попытку позже.");
      } else if (Config.purge_running) {
         player.sendMessage("§3CoreProtect §f- Выполняется очистка. Пожалуйста, повторите попытку позже.");
      } else if (!permission) {
         player.sendMessage("§3CoreProtect §f- У вас нет прав на это!.");
      } else if (resultc <= 1) {
         player.sendMessage("§3CoreProtect §f- Используйте \"/co purge t:<time>\".");
      } else if (seconds <= 0) {
         player.sendMessage("§3CoreProtect §f- Используйте \"/co purge t:<time>\".\n");
      } else if (player instanceof Player && seconds < 2592000) {
         player.sendMessage("§3CoreProtect §f- Вы можете удалять данные только за 30 дней.");
      } else if (seconds < 86400) {
         player.sendMessage("§3CoreProtect §f- Вы можете удалять данные только после 24 часов.");
      } else {
         boolean optimizeCheck = false;
         String[] var6 = args;
         int var7 = args.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String arg = var6[var8];
            if (arg.trim().equalsIgnoreCase("#optimize")) {
               optimizeCheck = true;
               break;
            }
         }

         boolean finalOptimizeCheck = optimizeCheck;
         class BasicThread implements Runnable {
            public void run() {
               try {
                  int timestamp = (int)(System.currentTimeMillis() / 1000L);
                  int ptime = timestamp - seconds;
                  long removed = 0L;
                  Connection connection = null;

                  for(int ix = 0; ix <= 5; ++ix) {
                     connection = Database.getConnection(false);
                     if (connection != null) {
                        break;
                     }

                     Thread.sleep(1000L);
                  }

                  if (connection == null) {
                     Functions.messageOwnerAndUser(player, "База данных занята. Пожалуйста, повторите попытку позже.");
                     return;
                  }

                  Functions.messageOwnerAndUser(player, "Начата очистка данных. Это может занять некоторое время.");
                  Functions.messageOwnerAndUser(player, "Не перезапускайте свой сервер до завершения.");
                  Config.purge_running = true;

                  while(!PurgeCommand.pause_success) {
                     Thread.sleep(1L);
                  }

                  Consumer.is_paused = true;
                  String query = "";
                  PreparedStatement preparedStmt = null;
                  boolean abort = false;
                  String purge_prefix = "tmp_" + Config.prefix;
                  if ((Integer)Config.config.get("use-mysql") == 0) {
                     query = "ATTACH DATABASE '" + Config.sqlite + ".tmp' AS tmp_db";
                     preparedStmt = connection.prepareStatement(query);
                     preparedStmt.execute();
                     preparedStmt.close();
                     purge_prefix = "tmp_db." + Config.prefix;
                  }

                  String[] version_split = CoreProtect.getInstance().getDescription().getVersion().split("\\.");
                  Integer[] current_version = new Integer[]{Integer.parseInt(version_split[0]), Integer.parseInt(version_split[1]), Integer.parseInt(version_split[2])};
                  Integer[] last_version = Patch.getLastVersion(connection);
                  boolean newVersion = Functions.newVersion(last_version, current_version);
                  if (newVersion) {
                     Functions.messageOwnerAndUser(player, "Выполнить очистку не удалось. Пожалуйста, повторите попытку позже.");
                     Consumer.is_paused = false;
                     Config.purge_running = false;
                     return;
                  }

                  if ((Integer)Config.config.get("use-mysql") == 0) {
                     Iterator var14 = Config.databaseTables.iterator();

                     while(var14.hasNext()) {
                        String table = (String)var14.next();

                        try {
                           query = "DROP TABLE IF EXISTS " + purge_prefix + table + "";
                           preparedStmt = connection.prepareStatement(query);
                           preparedStmt.execute();
                           preparedStmt.close();
                        } catch (Exception var31) {
                           var31.printStackTrace();
                        }
                     }

                     Functions.createDatabaseTables(purge_prefix, true);
                  }

                  List<String> purge_tables = Arrays.asList("sign", "container", "skull", "session", "chat", "command", "entity", "block");
                  Iterator var38 = Config.databaseTables.iterator();

                  String tablex;
                  while(var38.hasNext()) {
                     tablex = (String)var38.next();
                     String tableName = tablex.replaceAll("_", " ");
                     Functions.messageOwnerAndUser(player, "Processing " + tableName + " data...");
                     if ((Integer)Config.config.get("use-mysql") == 0) {
                        String columns = "";
                        ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + purge_prefix + tablex);
                        ResultSetMetaData resultSetMetaData = rs.getMetaData();
                        int columnCount = resultSetMetaData.getColumnCount();

                        String index;
                        for(int i = 1; i <= columnCount; ++i) {
                           index = resultSetMetaData.getColumnName(i);
                           if (columns.length() == 0) {
                              columns = index;
                           } else {
                              columns = columns + "," + index;
                           }
                        }

                        rs.close();
                        boolean error = false;

                        try {
                           index = "";
                           if (purge_tables.contains(tablex)) {
                              index = " WHERE time >= '" + ptime + "'";
                           }

                           query = "INSERT INTO " + purge_prefix + tablex + " SELECT " + columns + " FROM " + Config.prefix + tablex + index;
                           preparedStmt = connection.prepareStatement(query);
                           preparedStmt.execute();
                           preparedStmt.close();
                        } catch (Exception var30) {
                           error = true;
                           var30.printStackTrace();
                        }

                        if (error) {
                           Functions.messageOwnerAndUser(player, "Unable to process " + tableName + " data!");
                           Functions.messageOwnerAndUser(player, "Attempting to repair. This may take some time...");

                           try {
                              query = "DELETE FROM " + purge_prefix + tablex;
                              preparedStmt = connection.prepareStatement(query);
                              preparedStmt.execute();
                              preparedStmt.close();
                           } catch (Exception var29) {
                              var29.printStackTrace();
                           }

                           try {
                              query = "REINDEX " + Config.prefix + tablex;
                              preparedStmt = connection.prepareStatement(query);
                              preparedStmt.execute();
                              preparedStmt.close();
                           } catch (Exception var28) {
                              var28.printStackTrace();
                           }

                           try {
                              index = " NOT INDEXED";
                              query = "INSERT INTO " + purge_prefix + tablex + " SELECT " + columns + " FROM " + Config.prefix + tablex + index;
                              preparedStmt = connection.prepareStatement(query);
                              preparedStmt.execute();
                              preparedStmt.close();
                           } catch (Exception var32) {
                              var32.printStackTrace();
                              abort = true;
                              break;
                           }

                           if (purge_tables.contains(tablex)) {
                              try {
                                 query = "DELETE FROM " + purge_prefix + tablex + " WHERE time < '" + ptime + "'";
                                 preparedStmt = connection.prepareStatement(query);
                                 preparedStmt.execute();
                                 preparedStmt.close();
                              } catch (Exception var27) {
                                 var27.printStackTrace();
                              }
                           }
                        }

                        int old_count = 0;

                        try {
                           query = "SELECT COUNT(*) as count FROM " + Config.prefix + tablex + " LIMIT 0, 1";
                           preparedStmt = connection.prepareStatement(query);

                           ResultSet resultSet;
                           for(resultSet = preparedStmt.executeQuery(); resultSet.next(); old_count = resultSet.getInt("count")) {
                           }

                           resultSet.close();
                           preparedStmt.close();
                        } catch (Exception var34) {
                           var34.printStackTrace();
                        }

                        int new_count = 0;

                        try {
                           query = "SELECT COUNT(*) as count FROM " + purge_prefix + tablex + " LIMIT 0, 1";
                           preparedStmt = connection.prepareStatement(query);

                           ResultSet resultSetx;
                           for(resultSetx = preparedStmt.executeQuery(); resultSetx.next(); new_count = resultSetx.getInt("count")) {
                           }

                           resultSetx.close();
                           preparedStmt.close();
                        } catch (Exception var33) {
                           var33.printStackTrace();
                        }

                        removed += (long)(old_count - new_count);
                     }

                     if ((Integer)Config.config.get("use-mysql") == 1) {
                        try {
                           if (purge_tables.contains(tablex)) {
                              query = "DELETE FROM " + Config.prefix + tablex + " WHERE time < '" + ptime + "'";
                              preparedStmt = connection.prepareStatement(query);
                              preparedStmt.execute();
                              removed += (long)preparedStmt.getUpdateCount();
                              preparedStmt.close();
                           }
                        } catch (Exception var26) {
                           var26.printStackTrace();
                        }
                     }
                  }

                  if ((Integer)Config.config.get("use-mysql") == 1 && finalOptimizeCheck) {
                     Functions.messageOwnerAndUser(player, "Optimizing database. Please wait...");
                     var38 = Config.databaseTables.iterator();

                     while(var38.hasNext()) {
                        tablex = (String)var38.next();
                        query = "OPTIMIZE LOCAL TABLE " + Config.prefix + tablex + "";
                        preparedStmt = connection.prepareStatement(query);
                        preparedStmt.execute();
                        preparedStmt.close();
                     }
                  }

                  connection.close();
                  if (abort) {
                     if ((Integer)Config.config.get("use-mysql") == 0) {
                        (new File(Config.sqlite + ".tmp")).delete();
                     }

                     Config.loadDatabase();
                     Functions.messageOwnerAndUser(player, "§cНе удалось выполнить очистку. База данных может быть повреждена.");
                     Consumer.is_paused = false;
                     Config.purge_running = false;
                     return;
                  }

                  if ((Integer)Config.config.get("use-mysql") == 0) {
                     (new File(Config.sqlite)).delete();
                     (new File(Config.sqlite + ".tmp")).renameTo(new File(Config.sqlite));
                     Functions.messageOwnerAndUser(player, "Индексация базы данных. Пожалуйста, подождите...");
                  }

                  Config.loadDatabase();
                  Functions.messageOwnerAndUser(player, "База данных успешно очищена.");
                  Functions.messageOwnerAndUser(player, NumberFormat.getInstance().format(removed) + " удалено строк данных.");
               } catch (Exception var35) {
                  Functions.messageOwnerAndUser(player, "Выполнить очистку не удалось. Пожалуйста, повторите попытку позже.");
                  var35.printStackTrace();
               }

               Consumer.is_paused = false;
               Config.purge_running = false;
            }
         }

         Runnable runnable = new BasicThread();
         Thread thread = new Thread(runnable);
         thread.start();
      }
   }
}
