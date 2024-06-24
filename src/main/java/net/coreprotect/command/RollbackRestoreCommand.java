package net.coreprotect.command;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class RollbackRestoreCommand {
   protected static void runCommand(final CommandSender player, boolean permission, final String[] args, int force_seconds) {
      Location lo = CommandHandler.parseLocation(player, args);
      final List<String> arg_uuids = new ArrayList();
      List<String> arg_users = CommandHandler.parseUsers(args);
      Integer[] arg_radius = CommandHandler.parseRadius(args, player, lo);
      final int arg_noisy = CommandHandler.parseNoisy(args);
      final List<Object> arg_exclude = CommandHandler.parseExcluded(player, args);
      final List<String> arg_exclude_users = CommandHandler.parseExcludedUsers(player, args);
      final List<Object> arg_blocks = CommandHandler.parseRestricted(player, args);
      final String ts = CommandHandler.parseTimeString(args);
      int rbseconds = CommandHandler.parseTime(args);
      final int arg_wid = CommandHandler.parseWorld(args);
      final List<Integer> arg_action = CommandHandler.parseAction(args);
      boolean count = CommandHandler.parseCount(args);
      boolean worldedit = CommandHandler.parseWorldEdit(args);
      boolean forceglobal = CommandHandler.parseForceGlobal(args);
      int preview = CommandHandler.parsePreview(args);
      String corecommand = args[0].toLowerCase();
      if (arg_blocks != null && arg_exclude != null && arg_exclude_users != null) {
         if (arg_action.size() == 0 && arg_blocks.size() > 0) {
            Iterator var21 = arg_blocks.iterator();

            while(var21.hasNext()) {
               Object arg_block = var21.next();
               if (arg_block instanceof Material) {
                  arg_action.add(0);
                  arg_action.add(1);
               } else if (arg_block instanceof EntityType) {
                  arg_action.add(3);
               }
            }
         }

         if (count) {
            LookupCommand.runCommand(player, permission, args);
         } else if (Config.converter_running) {
            player.sendMessage("§3CoreProtect §f- Выполняется обновление. Пожалуйста, повторите попытку позже.");
         } else if (Config.purge_running) {
            player.sendMessage("§3CoreProtect §f- Выполняется очистка. Пожалуйста, повторите попытку позже.");
         } else if (arg_wid == -1) {
            String world_name = CommandHandler.parseWorldName(args);
            player.sendMessage("§3CoreProtect §f- Мир \"" + world_name + "\" не найден.");
         } else if (preview > 0 && !(player instanceof Player)) {
            player.sendMessage("§3CoreProtect §f- Вы можете просматривать откаты только в игре.");
         } else if (arg_action.contains(-1)) {
            player.sendMessage("§3CoreProtect §f- Это недопустимый параметр.");
         } else if (worldedit && arg_radius == null) {
            player.sendMessage("§3CoreProtect §f- Выделение WorldEdit не найдено.");
         } else if (arg_radius != null && arg_radius[0] == -1) {
            player.sendMessage("§3CoreProtect §f- Введите радиус.");
         } else if (Config.active_rollbacks.get(player.getName()) != null) {
            player.sendMessage("§3CoreProtect §f- Откат/восстановление уже выполняется.");
         } else {
            if (preview > 1 && force_seconds <= 0) {
               preview = 1;
            }

            if (!permission) {
               player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это.");
            } else {
               int a = 0;
               if (corecommand.equals("restore") || corecommand.equals("rs") || corecommand.equals("re")) {
                  a = 1;
               }

               final int final_action = a;
               int default_radius = (Integer)Config.config.get("default-radius");
               int max_radius;
               int radius_value;
               int c;
               int wid;
               if ((player instanceof Player || player instanceof BlockCommandSender) && arg_radius == null && default_radius > 0 && !forceglobal) {
                  max_radius = lo.getBlockX() - default_radius;
                  radius_value = lo.getBlockX() + default_radius;
                  c = lo.getBlockZ() - default_radius;
                  wid = lo.getBlockZ() + default_radius;
                  arg_radius = new Integer[]{default_radius, max_radius, radius_value, -1, -1, c, wid, 0};
               }

               boolean g = true;
               if (arg_users.contains("#global") && arg_radius == null) {
                  g = false;
               }

               if (arg_users.size() == 0 && arg_wid > 0) {
                  if (a == 0) {
                     player.sendMessage("§3CoreProtect §f- Вы не указали пользователя для отката.");
                  } else {
                     player.sendMessage("§3CoreProtect §f- Вы не указали пользователя для восстановления.");
                  }

                  return;
               }

               if (!g || arg_users.size() <= 0 && (arg_users.size() != 0 || arg_radius == null)) {
                  if (a == 0) {
                     player.sendMessage("§3CoreProtect §f- Вы не указали радиус отката.");
                  } else {
                     player.sendMessage("§3CoreProtect §f- Вы не указали радиус восстановления.");
                  }
               } else {
                  max_radius = (Integer)Config.config.get("max-radius");
                  if (arg_radius != null) {
                     radius_value = arg_radius[0];
                     if (radius_value > max_radius && max_radius > 0) {
                        player.sendMessage("§3CoreProtect §f- Максимальный " + corecommand.toLowerCase() + " радиус " + max_radius + ".");
                        player.sendMessage("§3CoreProtect §f- Используй \"r:#global\" для глобальной" + corecommand.toLowerCase() + ".");
                        return;
                     }
                  }

                  if (arg_action.size() > 0) {
                     if (arg_action.contains(4)) {
                        if (arg_users.contains("#global") || arg_users.size() == 0) {
                           player.sendMessage("§3CoreProtect §f- Чтобы использовать этот параметр, укажите игрока.");
                           return;
                        }

                        if (preview > 0) {
                           player.sendMessage("§3CoreProtect §f- Вы не можете предварительно просматривать историю.");
                           return;
                        }
                     } else if (!arg_action.contains(0) && !arg_action.contains(1) && !arg_action.contains(3)) {
                        if (a == 0) {
                           player.sendMessage("§3CoreProtect §f- Этот параметр нельзя использовать при откате.");
                        } else {
                           player.sendMessage("§3CoreProtect §f- Этот параметр нельзя использовать при восстановлении.");
                        }

                        return;
                     }
                  }

                  if (arg_users.size() == 0) {
                     arg_users.add("#global");
                  }

                  List<String> rollbackusers = arg_users;
                  c = 0;

                  for(Iterator var56 = arg_users.iterator(); var56.hasNext(); ++c) {
                     String ruser = (String)var56.next();
                     List<Player> players = CoreProtect.getInstance().getServer().matchPlayer(ruser);
                     Iterator var31 = players.iterator();

                     while(var31.hasNext()) {
                        Player p = (Player)var31.next();
                        if (p.getName().equalsIgnoreCase(ruser)) {
                           rollbackusers.set(c, p.getName());
                        }
                     }
                  }


                  int lookup_type;
                  if (rollbackusers.contains("#container")) {
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
                        player.sendMessage("§3CoreProtect §f- Пожалуйста, сначала проверьте историю.");
                        return;
                     }

                     if (preview > 0) {
                        player.sendMessage("§3CoreProtect §f- Вы не можете просмотреть предварительно историю.");
                        return;
                     }

                     String lcommand = (String)Config.lookup_command.get(player.getName());
                     String[] data = lcommand.split("\\.");
                     int x = Integer.parseInt(data[0]);
                     int y = Integer.parseInt(data[1]);
                     int z = Integer.parseInt(data[2]);
                     wid = Integer.parseInt(data[3]);
                     arg_action.add(5);
                     arg_radius = null;

                     lo = new Location(CoreProtect.getInstance().getServer().getWorld(Functions.getWorldName(wid)), (double)x, (double)y, (double)z);
                     Block block = lo.getBlock();
                     if (block.getState() instanceof Chest) {
                        BlockFace[] block_sides = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
                        BlockFace[] var37 = block_sides;
                        int var38 = block_sides.length;

                        for(int var39 = 0; var39 < var38; ++var39) {
                           BlockFace face = var37[var39];
                           if (block.getRelative(face, 1).getState() instanceof Chest) {
                              Block relative = block.getRelative(face, 1);
                              int x2 = relative.getX();
                              int z2 = relative.getZ();
                              double new_x = (double)(x + x2) / 2.0D;
                              double new_z = (double)(z + z2) / 2.0D;
                              lo.setX(new_x);
                              lo.setZ(new_z);
                              break;
                           }
                        }
                     }
                  }

                  final List<String> rollbackusers2 = rollbackusers;
                  if (rbseconds > 0) {
                     lookup_type = (int)(System.currentTimeMillis() / 1000L);
                     int seconds = lookup_type - rbseconds;
                     if (force_seconds > 0) {
                        seconds = force_seconds;
                     }

                     final int stime = seconds;
                     final Integer[] radius = arg_radius;

                     try {
                        Config.active_rollbacks.put(player.getName(), true);

                        Location finalLo = lo;
                        int finalPreview = preview;
                        class BasicThread2 implements Runnable {
                           public void run() {
                              try {
                                 int action = final_action;
                                 Location location = finalLo;
                                 Connection connection = Database.getConnection(false);
                                 if (connection != null) {
                                    Statement statement = connection.createStatement();
                                    String baduser = "";
                                    boolean exists = false;
                                    Iterator var7 = rollbackusers2.iterator();

                                    String check;
                                    label111:
                                    while(true) {
                                       while(true) {
                                          if (!var7.hasNext()) {
                                             break label111;
                                          }

                                          check = (String)var7.next();
                                          if (!check.equals("#global") && !check.equals("#container")) {
                                             exists = Lookup.playerExists(connection, check);
                                             if (!exists) {
                                                baduser = check;
                                                break label111;
                                             }
                                          } else {
                                             exists = true;
                                          }
                                       }
                                    }

                                    if (exists) {
                                       var7 = arg_exclude_users.iterator();

                                       while(var7.hasNext()) {
                                          check = (String)var7.next();
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
                                       boolean restrict_world = false;
                                       if (radius != null) {
                                          restrict_world = true;
                                       }

                                       if (location == null) {
                                          restrict_world = false;
                                       }

                                       if (arg_wid > 0) {
                                          restrict_world = true;
                                          location = new Location(CoreProtect.getInstance().getServer().getWorld(Functions.getWorldName(arg_wid)), 0.0D, 0.0D, 0.0D);
                                       }

                                       boolean verbose = false;
                                       if (arg_noisy == 1) {
                                          verbose = true;
                                       }

                                       String users = "";
                                       Iterator var10 = rollbackusers2.iterator();

                                       while(var10.hasNext()) {
                                          String value = (String)var10.next();
                                          if (users.length() == 0) {
                                             users = "" + value + "";
                                          } else {
                                             users = users + ", " + value;
                                          }
                                       }

                                       if (users.equals("#global") && restrict_world) {
                                          users = "#" + location.getWorld().getName();
                                       }

                                       if (finalPreview == 2) {
                                          player.sendMessage("§3CoreProtect §f- Отмена предварительного просмотра...");
                                       } else if (finalPreview == 1) {
                                          player.sendMessage("§3CoreProtect §f- Предварительный просмотр начался на \"" + users + "\".");
                                       } else if (action == 0) {
                                          player.sendMessage("§3CoreProtect §f- Откат начался на \"" + users + "\".");
                                       } else {
                                          player.sendMessage("§3CoreProtect §f- Восстановление начато на \"" + users + "\".");
                                       }

                                       if (arg_action.contains(5)) {
                                          Lookup.performContainerRollbackRestore(statement, player, arg_uuids, rollbackusers2, ts, arg_blocks, arg_exclude, arg_exclude_users, arg_action, location, radius, stime, restrict_world, false, verbose, action);
                                       } else {
                                          Lookup.performRollbackRestore(statement, player, arg_uuids, rollbackusers2, ts, arg_blocks, arg_exclude, arg_exclude_users, arg_action, location, radius, stime, restrict_world, false, verbose, action, finalPreview);
                                          if (finalPreview < 2) {
                                             List<Object[]> list = new ArrayList();
                                             list.add(new Object[]{stime});
                                             list.add(args);
                                             Config.last_rollback.put(player.getName(), list);
                                          }
                                       }
                                    }

                                    statement.close();
                                    connection.close();
                                 } else {
                                    player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже.");
                                 }
                              } catch (Exception var12) {
                                 var12.printStackTrace();
                              }

                              if (Config.active_rollbacks.get(player.getName()) != null) {
                                 Config.active_rollbacks.remove(player.getName());
                              }

                           }
                        }

                        Runnable runnable = new BasicThread2();
                        Thread thread = new Thread(runnable);
                        thread.start();
                     } catch (Exception var51) {
                        var51.printStackTrace();
                     }
                  } else if (a == 0) {
                     player.sendMessage("§3CoreProtect §f- Пожалуйста, укажите количество времени для отката.");
                  } else {
                     player.sendMessage("§3CoreProtect §f- Пожалуйста, укажите количество времени для восстановления.");
                  }
               }
            }

         }
      }
   }
}
