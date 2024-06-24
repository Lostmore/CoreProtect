package net.coreprotect.command;

import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.Config;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LookupCommand {
   protected static void runCommand(final CommandSender player, boolean permission, String[] args) {
      int resultc = args.length;
      final Location lo = CommandHandler.parseLocation(player, args);
      List<String> arg_users = CommandHandler.parseUsers(args);
      Integer[] arg_radius = CommandHandler.parseRadius(args, player, lo);
      int arg_noisy = CommandHandler.parseNoisy(args);
      List<String> arg_exclude_users = CommandHandler.parseExcludedUsers(player, args);
      List<Object> arg_exclude = CommandHandler.parseExcluded(player, args);
      List<Object> arg_blocks = CommandHandler.parseRestricted(player, args);
      String ts = CommandHandler.parseTimeString(args);
      int rbseconds = CommandHandler.parseTime(args);
      int arg_wid = CommandHandler.parseWorld(args);
      List<Integer> arg_action = CommandHandler.parseAction(args);
      final boolean count = CommandHandler.parseCount(args);
      boolean worldedit = CommandHandler.parseWorldEdit(args);
      boolean page_lookup = false;
      if (arg_blocks != null && arg_exclude != null && arg_exclude_users != null) {
         int arg_excluded = arg_exclude.size();
         int arg_restricted = arg_blocks.size();
         if (arg_action.size() == 0 && arg_blocks.size() > 0) {
            Iterator var20 = arg_blocks.iterator();

            while(var20.hasNext()) {
               Object arg_block = var20.next();
               if (arg_block instanceof Material) {
                  arg_action.add(0);
                  arg_action.add(1);
               } else if (arg_block instanceof EntityType) {
                  arg_action.add(3);
               }
            }
         }

         if (arg_wid == -1) {
            String world_name = CommandHandler.parseWorldName(args);
            player.sendMessage("§3CoreProtect §f- Мир \"" + world_name + "\" не найден.");
         } else {
            int type = 0;
            if (Config.lookup_type.get(player.getName()) != null) {
               type = Config.lookup_type.get(player.getName());
            }

            String bname;
            String bid;
            if (type == 0 && resultc > 1) {
               type = 4;
            } else if (resultc > 2) {
               type = 4;
            } else if (resultc > 1) {
               page_lookup = true;
               String dat = args[1];
               if (dat.contains(":")) {
                  String[] split = dat.split(":");
                  String check1 = split[0].replaceAll("[^a-zA-Z_]", "");
                  bname = "";
                  if (split.length > 1) {
                     bname = split[1].replaceAll("[^a-zA-Z_]", "");
                  }

                  if (check1.length() > 0 || bname.length() > 0) {
                     type = 4;
                     page_lookup = false;
                  }
               } else {
                  bid = dat.replaceAll("[^a-zA-Z_]", "");
                  if (bid.length() > 0) {
                     type = 4;
                     page_lookup = false;
                  }
               }
            }

            if (arg_action.contains(6) || arg_action.contains(7) || arg_action.contains(8) || arg_action.contains(9)) {
               page_lookup = true;
            }

            if (!permission && (!page_lookup || !player.hasPermission("coreprotect.inspect"))) {
               player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
            } else if (Config.converter_running) {
               player.sendMessage("§3CoreProtect §f- Выполняется обновление. Пожалуйста, повторите попытку позже.");
            } else if (Config.purge_running) {
               player.sendMessage("§3CoreProtect §f- Выполняется очистка. Пожалуйста, повторите попытку позже.");
            } else if (resultc < 2) {
               player.sendMessage("§3CoreProtect §f- Используйте: \"/co l <params>\".");
            } else if (arg_action.contains(-1)) {
               player.sendMessage("§3CoreProtect §f- Недопустимый параметром.");
            } else if (worldedit && arg_radius == null) {
               player.sendMessage("§3CoreProtect §f- Выделение WorldEdit не найдено.");
            } else if (arg_radius != null && arg_radius[0] == -1) {
               player.sendMessage("§3CoreProtect §f- Введите радиус");
            } else {
               boolean allPermission = false;
               if (player.isOp()) {
                  allPermission = true;
               }

               if (!allPermission) {
                  if (!page_lookup && (arg_action.size() == 0 || arg_action.contains(0) || arg_action.contains(1)) && !player.hasPermission("coreprotect.lookup.block")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(2) && !player.hasPermission("coreprotect.lookup.click")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(3) && !player.hasPermission("coreprotect.lookup.kill")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(4) && !player.hasPermission("coreprotect.lookup.container")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(6) && !player.hasPermission("coreprotect.lookup.chat")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(7) && !player.hasPermission("coreprotect.lookup.command")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(8) && !player.hasPermission("coreprotect.lookup.session")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }

                  if (arg_action.contains(9) && !player.hasPermission("coreprotect.lookup.username")) {
                     player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                     return;
                  }
               }

               if (arg_action.contains(6) || arg_action.contains(7) || arg_action.contains(8) || arg_action.contains(9)) {
                  if (!arg_action.contains(8) && (arg_radius != null || arg_wid > 0 || worldedit)) {
                     player.sendMessage("§3CoreProtect §f- \"r:\" не может быть использовано с этим параметром.");
                     return;
                  }

                  if (arg_blocks.size() > 0) {
                     player.sendMessage("§3CoreProtect §f- \"b:\" не может быть использовано с этим параметром.");
                     return;
                  }

                  if (arg_exclude.size() > 0) {
                     player.sendMessage("§3CoreProtect §f- \"e:\" не может быть использовано с этим параметром.");
                     return;
                  }
               }

               if (resultc > 2) {
                  bid = args[1];
                  if (bid.equalsIgnoreCase("type") || bid.equalsIgnoreCase("id")) {
                     type = 6;
                  }
               }

               if (rbseconds > 0 || page_lookup || type != 4 || arg_blocks.size() <= 0 && arg_users.size() <= 0) {
                  String pages;
                  String[] data;
                  String results;
                  int c;
                  int cs;
                  int x;
                  int y;
                  boolean default_re;
                  int pa;
                  int re;
                  int max_radius;
                  int radius_value;
                  int z;
                  int wid;
                  String lcommand;
                  if (type == 1) {
                     default_re = true;
                     pa = 0;
                     re = 7;
                     if (resultc > 1) {
                        pages = args[1];
                        if (pages.contains(":")) {
                           data = pages.split(":");
                           pages = data[0];
                           results = "";
                           if (data.length > 1) {
                              results = data[1];
                           }

                           results = results.replaceAll("[^0-9]", "");
                           if (results.length() > 0) {
                              c = Integer.parseInt(results);
                              if (c > 0) {
                                 re = c;
                                 default_re = false;
                              }
                           }
                        }

                        pages = pages.replaceAll("[^0-9]", "");
                        if (pages.length() > 0) {
                           max_radius = Integer.parseInt(pages);
                           if (max_radius > 0) {
                              pa = max_radius;
                           }
                        }
                     }

                     if (pa <= 0) {
                         pa = 1;
                         return;
                     }

                     pages = (String)Config.lookup_command.get(player.getName());
                     data = pages.split("\\.");
                     radius_value = Integer.parseInt(data[0]);
                     c = Integer.parseInt(data[1]);
                     cs = Integer.parseInt(data[2]);
                     x = Integer.parseInt(data[3]);
                     y = Integer.parseInt(data[4]);
                     z = Integer.parseInt(data[5]);
                     wid = Integer.parseInt(data[6]);
                     if (default_re) {
                        re = Integer.parseInt(data[7]);
                     }

                     lcommand = radius_value + "." + c + "." + cs + "." + x + "." + y + "." + z + "." + wid + "." + re;
                     Config.lookup_command.put(player.getName(), lcommand);
                     lcommand = Functions.getWorldName(x);
                     double dx = 0.5D * (double)(radius_value + y);
                     double dy = 0.5D * (double)(c + z);
                     double dz = 0.5D * (double)(cs + wid);
                     final Location location = new Location(CoreProtect.getInstance().getServer().getWorld(lcommand), dx, dy, dz);

                     int finalPa = pa;
                     int finalRe = re;
                     class BasicThread implements Runnable {
                        public void run() {
                           try {
                              Connection connection = Database.getConnection(false);
                              if (connection != null) {
                                 Statement statement = connection.createStatement();
                                 String blockdata = Lookup.chest_transactions(statement, location, player.getName(), finalPa, finalRe);
                                 if (blockdata.contains("\n")) {
                                    String[] var4 = blockdata.split("\n");
                                    int var5 = var4.length;

                                    for(int var6 = 0; var6 < var5; ++var6) {
                                       String b = var4[var6];
                                       player.sendMessage(b);
                                    }
                                 } else {
                                    player.sendMessage(blockdata);
                                 }

                                 statement.close();
                                 connection.close();
                              } else {
                                 player.sendMessage("§3CoreProtect §f- Database busy. Please try again later.");
                              }
                           } catch (Exception var8) {
                              var8.printStackTrace();
                           }

                        }
                     }

                     Runnable runnable = new BasicThread();
                     Thread thread = new Thread(runnable);
                     thread.start();
                  } else if (type != 2 && type != 3 && type != 7) {
                     if (type != 4 && type != 5) {
                        if (type == 6) {
                           bid = args[2];
                           bid = bid.replaceAll("[^0-9]", "");
                           if (bid.length() > 0) {
                              pa = Integer.parseInt(bid);
                              if (pa > 0) {
                                 bname = Functions.block_name_lookup(pa);
                                 if (bname.length() > 0) {
                                    player.sendMessage("§3CoreProtect §f- ID блока #" + pa + bname + "\".");
                                 } else {
                                    player.sendMessage("§3CoreProtect §f- Блок с таким ID не найден #" + pa + ".");
                                 }
                              } else {
                                 player.sendMessage("§3CoreProtect §f- Используйте: \"/co lookup type <ID>\".");
                              }
                           } else {
                              player.sendMessage("§3CoreProtect §f- Используйте: \"/co lookup type <ID>\".");
                           }
                        } else {
                           player.sendMessage("§3CoreProtect §f- Используйте: \"/co l <params>\".");
                        }
                     } else {
                        default_re = true;
                        pa = 1;
                        re = 4;
                        if (arg_action.contains(6) || arg_action.contains(7) || arg_action.contains(9)) {
                           re = 7;
                        }

                        if (type == 5 && resultc > 1) {
                           pages = args[1];
                           if (pages.contains(":")) {
                              data = pages.split(":");
                              pages = data[0];
                              results = "";
                              if (data.length > 1) {
                                 results = data[1];
                              }

                              results = results.replaceAll("[^0-9]", "");
                              if (results.length() > 0) {
                                 c = Integer.parseInt(results);
                                 if (c > 0) {
                                    re = c;
                                    default_re = false;
                                 }
                              }
                           }

                           pages = pages.replaceAll("[^0-9]", "");
                           if (pages.length() > 0) {
                              max_radius = Integer.parseInt(pages);
                              if (max_radius > 0) {
                                 pa = max_radius;
                              }
                           }
                        }

                        boolean g = true;
                        if (arg_users.contains("#global") && arg_radius == null) {
                           g = false;
                        }

                        if (!g || !page_lookup && arg_blocks.size() <= 0 && arg_users.size() <= 0 && (arg_users.size() != 0 || arg_radius == null)) {
                           player.sendMessage("§3CoreProtect §f- Используйте: \"/co l <params>\".");
                        } else {
                           max_radius = (Integer)Config.config.get("max-radius");
                           if (arg_radius != null) {
                              radius_value = arg_radius[0];
                              if (radius_value > max_radius && max_radius > 0) {
                                 player.sendMessage("§3CoreProtect §f- Максимальный радиус поиска равен " + max_radius + ".");
                                 player.sendMessage("§3CoreProtect §f- Не указывайте радиус для выполнения глобального поискаp.");
                                 return;
                              }
                           }

                           if (arg_users.size() == 0) {
                              arg_users.add("#global");
                           }

                           List<String> rollbackusers = arg_users;
                           c = 0;
                           Iterator var71 = arg_users.iterator();

                           while(true) {
                              if (!var71.hasNext()) {
                                 cs = -1;
                                 x = 0;
                                 y = 0;
                                 z = 0;
                                 wid = 0;
                                 int lookup_type;
                                 if (type == 5) {
                                    lcommand = (String)Config.lookup_command.get(player.getName());
                                    data = lcommand.split("\\.");
                                    x = Integer.parseInt(data[0]);
                                    y = Integer.parseInt(data[1]);
                                    z = Integer.parseInt(data[2]);
                                    wid = Integer.parseInt(data[3]);
                                    cs = Integer.parseInt(data[4]);
                                    arg_noisy = Integer.parseInt(data[5]);
                                    arg_excluded = Integer.parseInt(data[6]);
                                    arg_restricted = Integer.parseInt(data[7]);
                                    arg_wid = Integer.parseInt(data[8]);
                                    if (default_re) {
                                       re = Integer.parseInt(data[9]);
                                    }

                                    rollbackusers = (List)Config.lookup_ulist.get(player.getName());
                                    arg_blocks = (List)Config.lookup_blist.get(player.getName());
                                    arg_exclude = (List)Config.lookup_elist.get(player.getName());
                                    arg_exclude_users = (List)Config.lookup_e_userlist.get(player.getName());
                                    arg_action = (List)Config.lookup_alist.get(player.getName());
                                    arg_radius = (Integer[])Config.lookup_radius.get(player.getName());
                                    ts = (String)Config.lookup_time.get(player.getName());
                                    rbseconds = 1;
                                 } else {
                                    if (lo != null) {
                                       x = lo.getBlockX();
                                       z = lo.getBlockZ();
                                       wid = Functions.getWorldId(lo.getWorld().getName());
                                    }

                                    if (rollbackusers.size() == 1 && rollbackusers.contains("#global") && arg_action.contains(9)) {
                                       player.sendMessage("§3CoreProtect §f- Используйте \"/co l a:username u:<user>\".");
                                       return;
                                    }

                                    if (rollbackusers.contains("#container")) {
                                       if (arg_action.contains(6) || arg_action.contains(7) || arg_action.contains(8) || arg_action.contains(9)) {
                                          player.sendMessage("§3CoreProtect §f- \"#container\" неверно.");
                                          return;
                                       }

                                       boolean valid = false;
                                       if (Config.lookup_type.get(player.getName()) != null) {
                                          lookup_type = (Integer)Config.lookup_type.get(player.getName());
                                          if (lookup_type == 1) {
                                             valid = true;
                                          } else if (lookup_type == 5 && ((List)Config.lookup_ulist.get(player.getName())).contains("#container")) {
                                             valid = true;
                                          }
                                       }

                                       if (!valid) {
                                          player.sendMessage("§3CoreProtect §f- Пожалуйста, сначала проверьте действительный контейнер.");
                                          return;
                                       }

                                       if (!player.hasPermission("coreprotect.lookup.container") && !allPermission) {
                                          player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
                                          return;
                                       }

                                       lcommand = (String)Config.lookup_command.get(player.getName());
                                       data = lcommand.split("\\.");
                                       x = Integer.parseInt(data[0]);
                                       y = Integer.parseInt(data[1]);
                                       z = Integer.parseInt(data[2]);
                                       wid = Integer.parseInt(data[3]);
                                       arg_action.add(5);
                                       arg_radius = null;
                                       arg_wid = 0;
                                    }
                                 }

                                 final List<String> rollbackusers2 = rollbackusers;
                                 lookup_type = (int)(System.currentTimeMillis() / 1000L);
                                 if (cs == -1) {
                                    if (rbseconds <= 0) {
                                       cs = 0;
                                    } else {
                                       cs = lookup_type - rbseconds;
                                    }
                                 }

                                 final int stime = cs;
                                 final Integer[] radius = arg_radius;

                                 try {
                                    player.sendMessage("§3CoreProtect §f- Выполнение...");

                                    int finalPa = pa;
                                    int finalRe = re;
                                    int finaltype = type;
                                    int finalWid = wid;
                                    int finalX = x;
                                    int finalY = y;
                                    int finalZ = z;
                                    List<Object> finalArg_blocks = arg_blocks;
                                    List<Object> finalArg_exclude = arg_exclude;
                                    String finalTs = ts;
                                    List<String> finalArg_exclude_users = arg_exclude_users;
                                    List<Integer> finalArg_action = arg_action;
                                    int finalArg_noisy = arg_noisy;
                                    int finalArg_excluded = arg_excluded;
                                    int finalArg_restricted = arg_restricted;
                                    int finalArg_wid = arg_wid;
                                    class BasicThread2 implements Runnable {
                                       public void run() {
                                          try {
                                             List<String> uuid_list = new ArrayList();
                                             Location location = lo;
                                             boolean exists = false;
                                             String bc = finalX + "." + finalY + "." + finalZ + "." + finalWid + "." + stime + "." + finalArg_noisy + "." + finalArg_excluded + "." + finalArg_restricted + "." + finalArg_wid + "." + finalRe;
                                             Config.lookup_command.put(player.getName(), bc);
                                             Config.lookup_page.put(player.getName(), finalPa);
                                             Config.lookup_time.put(player.getName(), finalTs);
                                             Config.lookup_type.put(player.getName(), 5);
                                             Config.lookup_elist.put(player.getName(), finalArg_exclude);
                                             Config.lookup_e_userlist.put(player.getName(), finalArg_exclude_users);
                                             Config.lookup_blist.put(player.getName(), finalArg_blocks);
                                             Config.lookup_ulist.put(player.getName(), rollbackusers2);
                                             Config.lookup_alist.put(player.getName(), finalArg_action);
                                             Config.lookup_radius.put(player.getName(), radius);
                                             Connection connection = Database.getConnection(false);
                                             if (connection != null) {
                                                Statement statement = connection.createStatement();
                                                String baduser = "";
                                                Iterator var8 = rollbackusers2.iterator();

                                                String check;
                                                label230:
                                                while(true) {
                                                   while(true) {
                                                      if (!var8.hasNext()) {
                                                         break label230;
                                                      }

                                                      check = (String)var8.next();
                                                      if (!check.equals("#global") && !check.equals("#container") || finalArg_action.contains(9)) {
                                                         exists = Lookup.playerExists(connection, check);
                                                         if (!exists) {
                                                            baduser = check;
                                                            break label230;
                                                         }

                                                         if (finalArg_action.contains(9) && Config.uuid_cache.get(check.toLowerCase()) != null) {
                                                            String uuid = (String)Config.uuid_cache.get(check.toLowerCase());
                                                            uuid_list.add(uuid);
                                                         }
                                                      } else {
                                                         exists = true;
                                                      }
                                                   }
                                                }

                                                if (exists) {
                                                   var8 = finalArg_exclude_users.iterator();

                                                   while(var8.hasNext()) {
                                                      check = (String)var8.next();
                                                      if (!check.equals("#global")) {
                                                         exists = Lookup.playerExists(connection, check);
                                                         if (!exists) {
                                                            baduser = check;
                                                            break;
                                                         }
                                                      } else {
                                                         baduser = "#global";
                                                         exists = false;
                                                      }
                                                   }
                                                }

                                                if (!exists) {
                                                   player.sendMessage("§3CoreProtect §f- Игрок \"" + baduser + "\" не найден.");
                                                } else {
                                                   List<String> user_list = new ArrayList();
                                                   if (!finalArg_action.contains(9)) {
                                                      user_list = rollbackusers2;
                                                   }

                                                   int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
                                                   boolean restrict_world = false;
                                                   if (radius != null) {
                                                      restrict_world = true;
                                                   }

                                                   if (location == null) {
                                                      restrict_world = false;
                                                   }

                                                   if (finalArg_wid > 0) {
                                                      restrict_world = true;
                                                      location = new Location(CoreProtect.getInstance().getServer().getWorld(Functions.getWorldName(finalArg_wid)), (double) finalX, (double) finalY, (double) finalZ);
                                                   } else if (location != null) {
                                                      location = new Location(CoreProtect.getInstance().getServer().getWorld(Functions.getWorldName(finalWid)), (double) finalX, (double) finalY, (double) finalZ);
                                                   }

                                                   int row_max = finalPa * finalRe;
                                                   int page_start = row_max - finalRe;
                                                   int rows = 0;
                                                   boolean check_rows = true;
                                                   if ((finaltype == 5) && (finalPa > 1)) {
                                                      rows = (Integer)Config.lookup_rows.get(player.getName());
                                                      if (page_start < rows) {
                                                         check_rows = false;
                                                      }
                                                   }

                                                   if (check_rows) {
                                                      rows = Lookup.countLookupRows(statement, player, uuid_list, (List)user_list, finalArg_blocks, finalArg_exclude, finalArg_exclude_users, finalArg_action, location, radius, stime, restrict_world, true);
                                                      Config.lookup_rows.put(player.getName(), rows);
                                                   }

                                                   String arrows;
                                                   if (count) {
                                                      arrows = NumberFormat.getInstance().format((long)rows);
                                                      player.sendMessage("§3CoreProtect §f- " + arrows + " найдено.");
                                                   } else if (page_start >= rows) {
                                                      if (rows > 0) {
                                                         player.sendMessage("§3CoreProtect §f- Нет результатов на этой странице.");
                                                      } else {
                                                         player.sendMessage("§3CoreProtect §f- Не найдено.");
                                                      }
                                                   } else {
                                                      arrows = "                      ";
                                                      if (rows > finalRe) {
                                                         int total_pagesx = (int)Math.ceil((double)rows / ((double) finalRe + 0.0D));
                                                         String page_back = "«";
                                                         String page_next = "»";
                                                         if (finalPa > 1 && finalPa < total_pagesx) {
                                                            (new StringBuilder()).append(page_back).append(" | ").append(page_next).toString();
                                                         } else if (finalPa > 1) {
                                                            (new StringBuilder()).append("    ").append(page_back).toString();
                                                         } else {
                                                            (new StringBuilder()).append("    ").append(page_next).toString();
                                                         }
                                                      }

                                                      arrows = "";
                                                      List<String[]> lookup_list = Lookup.performPartialLookup(statement, player, uuid_list, (List)user_list, finalArg_blocks, finalArg_exclude, finalArg_exclude_users, finalArg_action, location, radius, stime, page_start, finalRe, restrict_world, true);
                                                      player.sendMessage("§f----- §3CoreProtect Результаты поиска §f-----" + arrows);
                                                      String string_amount;
                                                      String dplayer;
                                                      String rbd;
                                                      double time_sincex;
                                                      String dplayerx;
                                                      Iterator var50;
                                                      String[] data;
                                                      if (!finalArg_action.contains(6) && !finalArg_action.contains(7)) {
                                                         int xx;
                                                         String dtype;
                                                         int amount;
                                                         if (finalArg_action.contains(8)) {
                                                            var50 = lookup_list.iterator();

                                                            while(var50.hasNext()) {
                                                               data = (String[])var50.next();
                                                               string_amount = data[0];
                                                               dplayer = data[1];
                                                               int widx = Integer.parseInt(data[2]);
                                                               amount = Integer.parseInt(data[3]);
                                                               int yxx = Integer.parseInt(data[4]);
                                                               int zx = Integer.parseInt(data[5]);
                                                               xx = Integer.parseInt(data[6]);
                                                               double time_sincexx = (double)unixtimestamp - Double.parseDouble(string_amount);
                                                               time_sincexx /= 60.0D;
                                                               time_sincexx /= 60.0D;
                                                               dtype = (new DecimalFormat("0.00")).format(time_sincexx);
                                                               String action_string = "in";
                                                               if (xx == 0) {
                                                                  action_string = "out";
                                                               }

                                                               String world = Functions.getWorldName(widx);
                                                               double time_length = (double)dtype.replaceAll("[^0-9]", "").length() * 1.5D;
                                                               int padding = (int)(time_length + 12.5D);
                                                               String left_padding = StringUtils.leftPad("", padding, ' ');
                                                               player.sendMessage("§7" + dtype + "/h ago §f- §3" + dplayer + " §flogged §3" + action_string + "§f.");
                                                               player.sendMessage("§f" + left_padding + "§7^ §o(x" + amount + "/y" + yxx + "/z" + zx + "/" + world + ")");
                                                            }
                                                         } else if (finalArg_action.contains(9)) {
                                                            var50 = lookup_list.iterator();

                                                            while(var50.hasNext()) {
                                                               data = (String[])var50.next();
                                                               string_amount = data[0];
                                                               dplayer = (String)Config.uuid_cache_reversed.get(data[1]);
                                                               rbd = data[2];
                                                               time_sincex = (double)unixtimestamp - Double.parseDouble(string_amount);
                                                               time_sincex /= 60.0D;
                                                               time_sincex /= 60.0D;
                                                               dplayerx = (new DecimalFormat("0.00")).format(time_sincex);
                                                               player.sendMessage("§7" + dplayerx + "/h ago §f- §3" + dplayer + " §flogged in as §3" + rbd + "§f.");
                                                            }
                                                         } else {
                                                            var50 = lookup_list.iterator();

                                                            while(var50.hasNext()) {
                                                               data = (String[])var50.next();
                                                               string_amount = "";
                                                               int drb = Integer.parseInt(data[8]);
                                                               rbd = "";
                                                               if (drb == 1) {
                                                                  rbd = "§m";
                                                               }

                                                               boolean amountx = false;
                                                               String time = data[0];
                                                               dplayerx = data[1];
                                                               xx = Integer.parseInt(data[2]);
                                                               int yx = Integer.parseInt(data[3]);
                                                               int zxx = Integer.parseInt(data[4]);
                                                               dtype = data[5];
                                                               int ddata = Integer.parseInt(data[6]);
                                                               int daction = Integer.parseInt(data[7]);
                                                               int widxx = Integer.parseInt(data[9]);
                                                               String a = "placed";
                                                               String tag = "§f-";
                                                               if (finalArg_action.contains(4) || finalArg_action.contains(5)) {
                                                                  amount = Integer.parseInt(data[10]);
                                                                  string_amount = "x" + amount + " ";
                                                                  a = "added";
                                                               }

                                                               if (daction == 0) {
                                                                  a = "removed";
                                                               } else if (daction == 2) {
                                                                  a = "clicked";
                                                               } else if (daction == 3) {
                                                                  a = "killed";
                                                               }

                                                               double time_since = (double)unixtimestamp - Double.parseDouble(time);
                                                               time_since /= 60.0D;
                                                               time_since /= 60.0D;
                                                               String timeago = (new DecimalFormat("0.00")).format(time_since);
                                                               double time_lengthx = (double)timeago.replaceAll("[^0-9]", "").length() * 1.5D;
                                                               int paddingx = (int)(time_lengthx + 12.5D);
                                                               String left_paddingx = StringUtils.leftPad("", paddingx, ' ');
                                                               String worldx = Functions.getWorldName(widxx);
                                                               String dname = "";
                                                               boolean isPlayer = false;
                                                               if (daction == 3) {
                                                                  int dTypeInt = Integer.parseInt(dtype);
                                                                  if (dTypeInt == 0) {
                                                                     if (Config.player_id_cache_reversed.get(ddata) == null) {
                                                                        Database.loadUserName(connection, ddata);
                                                                     }

                                                                     dname = (String)Config.player_id_cache_reversed.get(ddata);
                                                                     isPlayer = true;
                                                                  } else {
                                                                     dname = Functions.getEntityType(dTypeInt).name();
                                                                  }
                                                               } else {
                                                                  dname = Functions.getType(Integer.parseInt(dtype)).name().toLowerCase();
                                                                  dname = Functions.nameFilter(dname, ddata);
                                                               }

                                                               if (dname.length() > 0 && !isPlayer) {
                                                                  dname = "minecraft:" + dname.toLowerCase() + "";
                                                               }

                                                               if (dname.contains("minecraft:")) {
                                                                  String[] block_name_split = dname.split(":");
                                                                  dname = block_name_split[1];
                                                               }

                                                               player.sendMessage("§7" + timeago + "/h ago " + tag + " §3" + rbd + "" + dplayerx + " §f" + rbd + "" + a + " " + string_amount + "§3" + rbd + "" + dname + "§f.");
                                                               player.sendMessage("§f" + left_paddingx + "§7^ §o(x" + xx + "/y" + yx + "/z" + zxx + "/" + worldx + ")");
                                                            }
                                                         }
                                                      } else {
                                                         var50 = lookup_list.iterator();

                                                         while(var50.hasNext()) {
                                                            data = (String[])var50.next();
                                                            string_amount = data[0];
                                                            dplayer = data[1];
                                                            rbd = data[2];
                                                            time_sincex = (double)unixtimestamp - Double.parseDouble(string_amount);
                                                            time_sincex /= 60.0D;
                                                            time_sincex /= 60.0D;
                                                            dplayerx = (new DecimalFormat("0.00")).format(time_sincex);
                                                            player.sendMessage("§7" + dplayerx + "/h ago §f- §3" + dplayer + ": §f" + rbd + "");
                                                         }
                                                      }

                                                      if (rows > finalRe) {
                                                         int total_pages = (int)Math.ceil((double)rows / ((double) finalRe + 0.0D));
                                                         if (finalArg_action.contains(6) || finalArg_action.contains(7) || finalArg_action.contains(9)) {
                                                            player.sendMessage("-----");
                                                         }

                                                         player.sendMessage("§fPage " + finalPa + "/" + total_pages + ". Просмотреть историю данных \"§3/co l <page>§f\".");
                                                      }
                                                   }
                                                }

                                                statement.close();
                                                connection.close();
                                             } else {
                                                player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже.");
                                             }
                                          } catch (Exception var45) {
                                             var45.printStackTrace();
                                          }

                                       }
                                    }

                                    Runnable runnable = new BasicThread2();
                                    Thread thread = new Thread(runnable);
                                    thread.start();
                                 } catch (Exception var59) {
                                    var59.printStackTrace();
                                 }
                                 break;
                              }

                              String ruser = (String)var71.next();
                              List<Player> players = CoreProtect.getInstance().getServer().matchPlayer(ruser);
                              Iterator var75 = players.iterator();

                              while(var75.hasNext()) {
                                 Player p = (Player)var75.next();
                                 if (p.getName().equalsIgnoreCase(ruser)) {
                                    rollbackusers.set(c, p.getName());
                                 }
                              }

                              ++c;
                           }
                        }
                     }
                  } else {
                     default_re = true;
                     pa = 1;
                     re = 7;
                     if (resultc > 1) {
                        pages = args[1];
                        if (pages.contains(":")) {
                           data = pages.split(":");
                           pages = data[0];
                           results = "";
                           if (data.length > 1) {
                              results = data[1];
                           }

                           results = results.replaceAll("[^0-9]", "");
                           if (results.length() > 0) {
                              c = Integer.parseInt(results);
                              if (c > 0) {
                                 re = c;
                                 default_re = false;
                              }
                           }
                        }

                        pages = pages.replaceAll("[^0-9]", "");
                        if (pages.length() > 0) {
                           max_radius = Integer.parseInt(pages);
                           if (max_radius > 0) {
                              pa = max_radius;
                           }
                        }
                     }

                     pages = (String)Config.lookup_command.get(player.getName());
                     data = pages.split("\\.");
                     radius_value = Integer.parseInt(data[0]);
                     c = Integer.parseInt(data[1]);
                     cs = Integer.parseInt(data[2]);
                     x = Integer.parseInt(data[3]);
                     y = Integer.parseInt(data[4]);
                     if (default_re) {
                        re = Integer.parseInt(data[5]);
                     }

                     String bc = radius_value + "." + c + "." + cs + "." + x + "." + y + "." + re;
                     Config.lookup_command.put(player.getName(), bc);
                     String world = Functions.getWorldName(x);
                     final Block fblock = CoreProtect.getInstance().getServer().getWorld(world).getBlockAt(radius_value, c, cs);

                     int finalRe = re;
                     int finalPa = pa;
                     int finalType = type;
                     class BasicThread implements Runnable {
                        public void run() {
                           try {
                              Connection connection = Database.getConnection(false);
                              if (connection != null) {
                                 Statement statement = connection.createStatement();
                                 String blockdata = null;
                                 if (finalType == 7) {
                                    blockdata = Lookup.interaction_lookup(statement, fblock, player.getName(), 0, finalPa, finalRe);
                                 } else {
                                    blockdata = Lookup.block_lookup(statement, fblock, player.getName(), 0, finalPa, finalRe);
                                 }

                                 if (blockdata.contains("\n")) {
                                    String[] var4 = blockdata.split("\n");
                                    int var5 = var4.length;

                                    for(int var6 = 0; var6 < var5; ++var6) {
                                       String b = var4[var6];
                                       player.sendMessage(b);
                                    }
                                 } else if (blockdata.length() > 0) {
                                    player.sendMessage(blockdata);
                                 }

                                 statement.close();
                                 connection.close();
                              } else {
                                 player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже.");
                              }
                           } catch (Exception var8) {
                              var8.printStackTrace();
                           }

                        }
                     }

                     Runnable runnable = new BasicThread();
                     Thread thread = new Thread(runnable);
                     thread.start();
                  }

               } else {
                  player.sendMessage("§3CoreProtect §f- Пожалуйста, укажите количество времени для поиска.");
               }
            }
         }
      }
   }
}
