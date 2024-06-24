package net.coreprotect.database;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.consumer.Queue;
import net.coreprotect.model.BlockInfo;
import net.coreprotect.model.Config;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

public class Lookup extends Queue {
   public static String block_lookup(Statement statement, Block block, String user, int offset, int page, int limit) {
      String result = "";
//
      try {
         if (block == null) {
            return result;
         }

         boolean found = false;
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int time = (int) (System.currentTimeMillis() / 1000L);
         int wid = Functions.getWorldId(block.getWorld().getName());
         int check_time = 0;
         int count = 0;
         int row_max = page * limit;
         int page_start = row_max - limit;
         if (offset > 0) {
            check_time = time - offset;
         }

         String query = "SELECT COUNT(*) as count from " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND action IN(0,1) AND time >= '" + check_time + "' LIMIT 0, 1";

         ResultSet rs;
         for (rs = statement.executeQuery(query); rs.next(); count = rs.getInt("count")) {
         }

         rs.close();
         int total_pages = (int) Math.ceil((double) count / ((double) limit + 0.0D));
         query = "SELECT time,user,action,type,data,rolled_back FROM " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND action IN(0,1) AND time >= '" + check_time + "' ORDER BY rowid DESC LIMIT " + page_start + ", " + limit + "";

         String result_user;
         String timeago;
         String a2;
         String rbd;
         String dname;
         for (rs = statement.executeQuery(query); rs.next(); result = result + "§7" + timeago + "/h ago §f- §3" + rbd + "" + result_user + " §f" + rbd + "" + a2 + " §3" + rbd + "" + dname + "§f.\n") {
            int result_userid = rs.getInt("user");
            int result_action = rs.getInt("action");
            int result_type = rs.getInt("type");
            int result_data = rs.getInt("data");
            int result_time = rs.getInt("time");
            int result_rolled_back = rs.getInt("rolled_back");
            if (Config.player_id_cache_reversed.get(result_userid) == null) {
               Database.loadUserName(statement.getConnection(), result_userid);
            }

            result_user = (String) Config.player_id_cache_reversed.get(result_userid);
            double time_since = (double) time - ((double) result_time + 0.0D);
            time_since /= 60.0D;
            time_since /= 60.0D;
            timeago = (new DecimalFormat("0.00")).format(time_since);
            if (!found) {
               result = "§f----- §3CoreProtect §f----- §7(x" + x + "/y" + y + "/z" + z + ")\n";
            }

            found = true;
            a2 = "placed";
            if (result_action == 0) {
               a2 = "removed";
            } else if (result_action == 2) {
               a2 = "clicked";
            } else if (result_action == 3) {
               a2 = "killed";
            }

            rbd = "";
            if (result_rolled_back == 1) {
               rbd = "§m";
            }

            dname = "";
            if (result_action == 3) {
               dname = Functions.getEntityType(result_type).name();
            } else {
               dname = Functions.nameFilter(Functions.getType(result_type).name().toLowerCase(), result_data);
               dname = "minecraft:" + dname.toLowerCase();
            }

            if (dname.length() > 0) {
               dname = "" + dname + "";
            }

            if (dname.contains("minecraft:")) {
               String[] block_name_split = dname.split(":");
               dname = block_name_split[1];
            }
         }

         rs.close();
         String bc;
         if (found) {
            if (count > limit) {
               bc = "§f-----\n";
               bc = bc + "§fPage " + page + "/" + total_pages + ". View older data by typing \"§3/co l <page>§f\".\n";
               result = result + bc;
            }
         } else if (!found) {
            if (row_max > count && count > 0) {
               result = "§3CoreProtect §f- §fДанные о данном блоке не найдены..";
            } else {
               result = "§3CoreProtect §f- §fДанные блока не найдены в этом местоположении.";
            }
         }

         bc = x + "." + y + "." + z + "." + wid + ".0." + limit;
         Config.lookup_page.put(user, page);
         Config.lookup_type.put(user, 2);
         Config.lookup_command.put(user, bc);
      } catch (Exception var34) {
         var34.printStackTrace();
      }

      return result;
   }

   public static List<String[]> block_lookup_api(Block block, int offset) {
      ArrayList result = new ArrayList();

      try {
         if (block == null) {
            return result;
         }

         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int time = (int) (System.currentTimeMillis() / 1000L);
         int wid = Functions.getWorldId(block.getWorld().getName());
         int check_time = 0;
         if (offset > 0) {
            check_time = time - offset;
         }

         Connection connection = Database.getConnection(false);
         if (connection == null) {
            return result;
         }

         Statement statement = connection.createStatement();
         String query = "SELECT time,user,action,type,data,rolled_back FROM " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND time > '" + check_time + "' ORDER BY rowid DESC";
         ResultSet rs = statement.executeQuery(query);

         while (rs.next()) {
            int result_time = rs.getInt("time");
            int result_userid = rs.getInt("user");
            int result_action = rs.getInt("action");
            int result_type = rs.getInt("type");
            int result_data = rs.getInt("data");
            int result_rolled_back = rs.getInt("rolled_back");
            if (Config.player_id_cache_reversed.get(result_userid) == null) {
               Database.loadUserName(connection, result_userid);
            }

            String result_user = (String) Config.player_id_cache_reversed.get(result_userid);
            String line = result_time + "," + result_user + "," + x + "." + y + "." + z + "," + result_type + "," + result_data + "," + result_action + "," + result_rolled_back + "," + wid + ",";
            String[] ldata = Functions.toStringArray(line);
            result.add(ldata);
         }

         rs.close();
         statement.close();
         connection.close();
      } catch (Exception var22) {
         var22.printStackTrace();
      }

      return result;
   }

   public static String chest_transactions(Statement statement, Location l, String lookup_user, int page, int limit) {
      String result = "";

      try {
         if (l == null) {
            return result;
         }

         boolean found = false;
         int x = (int)Math.floor(l.getX());
         int y = (int)Math.floor(l.getY());
         int z = (int)Math.floor(l.getZ());
         int x2 = (int)Math.ceil(l.getX());
         int y2 = (int)Math.ceil(l.getY());
         int z2 = (int)Math.ceil(l.getZ());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int wid = Functions.getWorldId(l.getWorld().getName());
         int count = 0;
         int row_max = page * limit;
         int page_start = row_max - limit;
         String query = "SELECT COUNT(*) as count from " + Config.prefix + "container WHERE wid = '" + wid + "' AND (x = '" + x + "' OR x = '" + x2 + "') AND (z = '" + z + "' OR z = '" + z2 + "') AND y = '" + y + "' LIMIT 0, 1";

         ResultSet rs;
         for(rs = statement.executeQuery(query); rs.next(); count = rs.getInt("count")) {
         }

         rs.close();
         int total_pages = (int)Math.ceil((double)count / ((double)limit + 0.0D));
         query = "SELECT time,user,action,type,data,amount,rolled_back FROM " + Config.prefix + "container WHERE wid = '" + wid + "' AND (x = '" + x + "' OR x = '" + x2 + "') AND (z = '" + z + "' OR z = '" + z2 + "') AND y = '" + y + "' ORDER BY rowid DESC LIMIT " + page_start + ", " + limit + "";

         int result_amount;
         String result_user;
         String timeago;
         String a2;
         String rbd;
         String dname;
         for(rs = statement.executeQuery(query); rs.next(); result = result + "§7" + timeago + "/h ago §f- §3" + rbd + "" + result_user + " §f" + rbd + "" + a2 + " x" + result_amount + " §3" + rbd + "" + dname + "§f.\n") {
            int result_userid = rs.getInt("user");
            int result_action = rs.getInt("action");
            int result_type = rs.getInt("type");
            int result_data = rs.getInt("data");
            int result_time = rs.getInt("time");
            result_amount = rs.getInt("amount");
            int result_rolled_back = rs.getInt("rolled_back");
            if (Config.player_id_cache_reversed.get(result_userid) == null) {
               Database.loadUserName(statement.getConnection(), result_userid);
            }

            result_user = (String)Config.player_id_cache_reversed.get(result_userid);
            double time_since = (double)time - ((double)result_time + 0.0D);
            time_since /= 60.0D;
            time_since /= 60.0D;
            timeago = (new DecimalFormat("0.00")).format(time_since);
            if (!found) {
               result = "§f----- §3Container Transactions §f----- §7(x" + x + "/y" + y + "/z" + z + ")\n";
            }

            found = true;
            a2 = "added";
            if (result_action == 0) {
               a2 = "removed";
            }

            rbd = "";
            if (result_rolled_back == 1) {
               rbd = "§m";
            }

            dname = Functions.getType(result_type).name().toLowerCase();
            dname = Functions.nameFilter(dname, result_data);
            if (dname.length() > 0) {
               dname = "minecraft:" + dname.toLowerCase() + "";
            }

            if (dname.contains("minecraft:")) {
               String[] block_name_split = dname.split(":");
               dname = block_name_split[1];
            }
         }

         rs.close();
         String bc;
         if (found) {
            if (count > limit) {
               bc = "§f-----\n";
               bc = bc + "§fPage " + page + "/" + total_pages + ". View older data by typing \"§3/co l <page>§f\".\n";
               result = result + bc;
            }
         } else if (!found) {
            if (row_max > count && count > 0) {
               result = "§3CoreProtect §f- §fДля этой страницы не найдено контейнерных транзакций";
            } else {
               result = "§3CoreProtect §f- §fДля этой страницы не найдено контейнерных транзакций.";
            }
         }

         bc = x + "." + y + "." + z + "." + wid + "." + x2 + "." + y2 + "." + z2 + "." + limit;
         Config.lookup_type.put(lookup_user, 1);
         Config.lookup_page.put(lookup_user, page);
         Config.lookup_command.put(lookup_user, bc);
      } catch (Exception var36) {
         var36.printStackTrace();
      }

      return result;
   }

   private static List<String[]> convertRawLookup(Statement statement, List<Object[]> list) {
      List<String[]> new_list = new ArrayList();
      if (list == null) {
         return null;
      } else {
         for(int i = 0; i < list.size(); ++i) {
            Object[] map = (Object[])list.get(i);
            int new_length = map.length - 1;
            String[] results = new String[new_length];

            for(int i2 = 0; i2 < map.length; ++i2) {
               try {
                  int new_id = i2 - 1;
                  if (i2 == 2) {
                     if (map[i2] instanceof Integer) {
                        int user_id = (Integer)map[i2];
                        if (Config.player_id_cache_reversed.get(user_id) == null) {
                           Database.loadUserName(statement.getConnection(), user_id);
                        }

                        String user_result = (String)Config.player_id_cache_reversed.get(user_id);
                        results[new_id] = user_result;
                     } else {
                        results[new_id] = (String)map[i2];
                     }
                  } else if (i2 > 0) {
                     if (map[i2] instanceof Integer) {
                        results[new_id] = map[i2].toString();
                     } else if (map[i2] instanceof String) {
                        results[new_id] = (String)map[i2];
                     }
                  }
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            }

            new_list.add(results);
         }

         return new_list;
      }
   }

   public static int countLookupRows(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, boolean restrict_world, boolean lookup) {
      int rows = 0;

      try {
         while(Consumer.is_paused) {
            Thread.sleep(1L);
         }

         Consumer.is_paused = true;

         ResultSet rs;
         for(rs = rawLookupResultSet(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, -1, -1, restrict_world, lookup, true); rs.next(); rows = rs.getInt("count")) {
         }

         rs.close();
      } catch (Exception var15) {
         var15.printStackTrace();
      }

      Consumer.is_paused = false;
      return rows;
   }

   public static void finishRollbackRestore(CommandSender user, Location location, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, String time_string, int file, int seconds, int item_count, int block_count, int entity_count, int rollback_type, Integer[] radius, boolean verbose, boolean restrict_world, int preview) {
      try {
         if (preview == 2) {
            user.sendMessage("§3CoreProtect §f- Предварительный просмотр отменен.");
            return;
         }

         user.sendMessage("-----");
         String users = "";
         Iterator var19 = check_users.iterator();

         while(var19.hasNext()) {
            String value = (String)var19.next();
            if (users.length() == 0) {
               users = "" + value + "";
            } else {
               users = users + ", " + value;
            }
         }

         if (users.equals("#global") && restrict_world) {
            users = "#" + location.getWorld().getName();
         }

         if (preview > 0) {
            user.sendMessage("§3CoreProtect §f- Предварительный просмотр завершен для \"" + users + "\".");
         } else if (rollback_type == 1) {
            user.sendMessage("§3CoreProtect §f- Восстановление завершено для \"" + users + "\".");
         } else if (rollback_type == 0) {
            user.sendMessage("§3CoreProtect §f- Откат завершен для \"" + users + "\".");
         }

         if (preview == 1) {
            user.sendMessage("§3CoreProtect §f- Время:" + time_string + ".");
         } else if (rollback_type == 1) {
            user.sendMessage("§3CoreProtect §f- Восстановление" + time_string + ".");
         } else if (rollback_type == 0) {
            user.sendMessage("§3CoreProtect §f- Откат" + time_string + ".");
         }

         int ec;
         if (radius != null) {
            int worldedit = radius[7];
            if (worldedit == 0) {
               ec = radius[0];
               user.sendMessage("§3CoreProtect §f- Радиус: " + ec + " блок(ов).");
            } else {
               user.sendMessage("§3CoreProtect §f- Радиус: #worldedit.");
            }
         }

         if (restrict_world && radius == null && location != null) {
            user.sendMessage("§3CoreProtect §f- Ограниченно миром: \"" + location.getWorld().getName() + "\".");
         }

         if (action_list.contains(4)) {
            if (action_list.contains(0)) {
               user.sendMessage("§3CoreProtect §f- Ограниченно: \"-container\".");
            } else if (action_list.contains(1)) {
               user.sendMessage("§3CoreProtect §f- Ограниченно: \"+container\".");
            }
         } else if (action_list.contains(0) && action_list.contains(1)) {
            user.sendMessage("§3CoreProtect §f- Ограниченно: \"block-change\".");
         } else if (action_list.contains(0)) {
            user.sendMessage("§3CoreProtect §f- Ограниченно: \"block-break\".");
         } else if (action_list.contains(1)) {
            user.sendMessage("§3CoreProtect §f- Ограниченно: \"block-place\".");
         } else if (action_list.contains(3)) {
            user.sendMessage("§3CoreProtect §f- Ограниченно: \"entity-kill\".");
         }

         Iterator var21;
         Object et;
         String value_name;
         String e;
         if (restrict_list.size() > 0) {
            e = "";
            ec = 0;

            for(var21 = restrict_list.iterator(); var21.hasNext(); ++ec) {
               et = var21.next();
               value_name = "";
               if (et instanceof Material) {
                  value_name = ((Material)et).name().toLowerCase();
               } else if (et instanceof EntityType) {
                  value_name = ((EntityType)et).name().toLowerCase();
               }

               if (ec == 0) {
                  e = "" + value_name + "";
               } else {
                  e = e + ", " + value_name;
               }
            }

            user.sendMessage("§3CoreProtect §f- Ограниченно типом(ами) блока: " + e + ".");
         }

         if (exclude_list.size() > 0) {
            e = "";
            ec = 0;

            for(var21 = exclude_list.iterator(); var21.hasNext(); ++ec) {
               et = var21.next();
               value_name = "";
               if (et instanceof Material) {
                  value_name = ((Material)et).name().toLowerCase();
               } else if (et instanceof EntityType) {
                  value_name = ((EntityType)et).name().toLowerCase();
               }

               if (ec == 0) {
                  e = "" + value_name + "";
               } else {
                  e = e + ", " + value_name;
               }
            }

            user.sendMessage("§3CoreProtect §f- Блоки исключения: " + e + ".");
         }

         if (exclude_user_list.size() > 0) {
            e = "";
            ec = 0;

            for(var21 = exclude_user_list.iterator(); var21.hasNext(); ++ec) {
               et = (String)var21.next();
               if (ec == 0) {
                  e = "" + et + "";
               } else {
                  e = e + ", " + et;
               }
            }

            user.sendMessage("§3CoreProtect §f- Белый лист игроков: " + e + ".");
         }

         if (action_list.contains(5)) {
            user.sendMessage("§3CoreProtect §f- Примерно. " + block_count + " предметов заменено.");
         } else if (preview == 0) {
            if (item_count > 0) {
               user.sendMessage("§3CoreProtect §f- Примерно. " + item_count + " предметов заменено.");
            }

            if (entity_count > 0) {
               if (entity_count == 1) {
                  user.sendMessage("§3CoreProtect §f- Примерно. " + entity_count + " обьектов заменено.");
               } else {
                  user.sendMessage("§3CoreProtect §f- Примерно. " + entity_count + " объектов заменено.");
               }
            }

            user.sendMessage("§3CoreProtect §f- Примерно. " + block_count + " блоков заменено.");
         } else if (preview > 0) {
            user.sendMessage("§3CoreProtect §f- Примерно. " + block_count + " блоков to заменено.");
         }

         if (verbose && preview == 0 && file > -1) {
            user.sendMessage("§3CoreProtect §f- Модифицированный " + file + " чанк.");
         }

         if (preview == 0) {
            user.sendMessage("§3CoreProtect §f- Выполнено за : " + seconds + " секунд.");
         }

         user.sendMessage("-----");
         if (preview > 0) {
            user.sendMessage("§3CoreProtect §f- Выберите: \"/co apply\" or \"/co cancel\".");
         }
      } catch (Exception var24) {
         var24.printStackTrace();
      }

   }

   public static String interaction_lookup(Statement statement, Block block, String user, int offset, int page, int limit) {
      String result = "";

      try {
         if (block == null) {
            return result;
         }

         boolean found = false;
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int time = (int)(System.currentTimeMillis() / 1000L);
         int wid = Functions.getWorldId(block.getWorld().getName());
         int check_time = 0;
         int count = 0;
         int row_max = page * limit;
         int page_start = row_max - limit;
         if (offset > 0) {
            check_time = time - offset;
         }

         String query = "SELECT COUNT(*) as count from " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND action='2' AND time >= '" + check_time + "' LIMIT 0, 1";

         ResultSet rs;
         for(rs = statement.executeQuery(query); rs.next(); count = rs.getInt("count")) {
         }

         rs.close();
         int total_pages = (int)Math.ceil((double)count / ((double)limit + 0.0D));
         query = "SELECT time,user,action,type,data,rolled_back FROM " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND action='2' AND time >= '" + check_time + "' ORDER BY rowid DESC LIMIT " + page_start + ", " + limit + "";

         String result_user;
         String timeago;
         String a2;
         String rbd;
         String dname;
         for(rs = statement.executeQuery(query); rs.next(); result = result + "§7" + timeago + "/h ago §f- §3" + rbd + "" + result_user + " §f" + rbd + "" + a2 + " §3" + rbd + "" + dname + "§f.\n") {
            int result_userid = rs.getInt("user");
            int result_action = rs.getInt("action");
            int result_type = rs.getInt("type");
            int result_data = rs.getInt("data");
            int result_time = rs.getInt("time");
            int result_rolled_back = rs.getInt("rolled_back");
            if (Config.player_id_cache_reversed.get(result_userid) == null) {
               Database.loadUserName(statement.getConnection(), result_userid);
            }

            result_user = (String)Config.player_id_cache_reversed.get(result_userid);
            double time_since = (double)time - ((double)result_time + 0.0D);
            time_since /= 60.0D;
            time_since /= 60.0D;
            timeago = (new DecimalFormat("0.00")).format(time_since);
            if (!found) {
               result = "§f----- §3Взаимодействие игроков §f----- §7(x" + x + "/y" + y + "/z" + z + ")\n";
            }

            found = true;
            a2 = "placed";
            if (result_action == 0) {
               a2 = "removed";
            } else if (result_action == 2) {
               a2 = "clicked";
            }

            rbd = "";
            if (result_rolled_back == 1) {
               rbd = "§m";
            }

            dname = Functions.getType(result_type).name().toLowerCase();
            dname = Functions.nameFilter(dname, result_data);
            if (dname.length() > 0) {
               dname = "minecraft:" + dname.toLowerCase() + "";
            }

            if (dname.contains("minecraft:")) {
               String[] block_name_split = dname.split(":");
               dname = block_name_split[1];
            }
         }

         rs.close();
         String bc;
         if (found) {
            if (count > limit) {
               bc = "§f-----\n";
               bc = bc + "§fPage " + page + "/" + total_pages + ". Просмотреть старые данные: \"§3/co l <page>§f\".\n";
               result = result + bc;
            }
         } else if (!found) {
            if (row_max > count && count > 0) {
               result = "§3CoreProtect §f- §fНе обнаружено взаимодействий игроков.";
            } else {
               result = "§3CoreProtect §f- §fВокруг не обнаружено взаимедйствие игроков.";
            }
         }

         bc = x + "." + y + "." + z + "." + wid + ".2." + limit;
         Config.lookup_page.put(user, page);
         Config.lookup_type.put(user, 7);
         Config.lookup_command.put(user, bc);
      } catch (Exception var34) {
         var34.printStackTrace();
      }

      return result;
   }

   public static void modifyContainerItems(Material type, Object container, int slot, ItemStack itemstack, int action) {
      try {
         ItemStack[] contents = null;
         if (type.equals(Material.ARMOR_STAND)) {
            EntityEquipment equipment = (EntityEquipment)container;
            if (equipment != null) {
               contents = equipment.getArmorContents();
               if (action == 1) {
                  itemstack.setAmount(1);
               } else {
                  itemstack.setType(Material.AIR);
                  itemstack.setAmount(0);
               }

               if (slot >= 0) {
                  contents[slot] = itemstack;
               }

               equipment.setArmorContents(contents);
            }
         } else {
            Inventory inventory = (Inventory)container;
            if (inventory != null) {
               int count = 0;
               int amount = itemstack.getAmount();
               itemstack.setAmount(1);

               for(; count < amount; ++count) {
                  if (action == 1) {
                     inventory.addItem(new ItemStack[]{itemstack});
                  } else {
                     inventory.removeItem(new ItemStack[]{itemstack});
                  }
               }
            }
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }

   public static void performContainerRollbackRestore(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, String time_string, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, final Location location, Integer[] radius, int check_time, boolean restrict_world, boolean lookup, boolean verbose, final int rollback_type) {
      try {
         long time1 = System.currentTimeMillis();
         final List<Object[]> lookup_list = performLookupRaw(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, -1, -1, restrict_world, lookup);
         if (rollback_type == 1) {
            Collections.reverse(lookup_list);
         }

         String user_string = "#server";
         if (user != null) {
            user_string = user.getName();
         }

         Queue.queueContainerRollbackUpdate(user_string, location, lookup_list, rollback_type);
         String final_user_string = user_string;
         Config.rollback_hash.put(user_string, new int[]{0, 0, 0, 0});
         String finalUser_string = user_string;
         CoreProtect.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(CoreProtect.getInstance(), new Runnable() {
            public void run() {
               try {
                  int[] rollback_hash_data = (int[])Config.rollback_hash.get(finalUser_string);
                  int item_count = rollback_hash_data[0];
                  int entity_count = rollback_hash_data[2];
                  Block block = location.getBlock();
                  if (!block.getWorld().isChunkLoaded(block.getChunk())) {
                     block.getWorld().loadChunk(block.getChunk());
                  }

                  Object container = null;
                  Material type = block.getType();
                  if (BlockInfo.containers.contains(type)) {
                     container = Functions.getContainerInventory(block.getState(), false);
                  } else {
                     Entity[] var7 = block.getChunk().getEntities();
                     int var8 = var7.length;

                     for(int var9 = 0; var9 < var8; ++var9) {
                        Entity entity = var7[var9];
                        if (entity instanceof ArmorStand && entity.getLocation().getBlockX() == location.getBlockX() && entity.getLocation().getBlockY() == location.getBlockY() && entity.getLocation().getBlockZ() == location.getBlockZ()) {
                           type = Material.ARMOR_STAND;
                           container = Functions.getEntityEquipment((LivingEntity)entity);
                        }
                     }
                  }

                  int modify_count = 0;
                  if (container != null) {
                     Iterator var23 = lookup_list.iterator();

                     label63:
                     while(true) {
                        int row_data;
                        int row_action;
                        int row_rolled_back;
                        int row_amount;
                        byte[] row_metadata;
                        Material row_type;
                        do {
                           if (!var23.hasNext()) {
                              break label63;
                           }

                           Object[] row = (Object[])var23.next();
                           int row_type_raw = (Integer)row[6];
                           row_data = (Integer)row[7];
                           row_action = (Integer)row[8];
                           row_rolled_back = (Integer)row[9];
                           row_amount = (Integer)row[11];
                           row_metadata = (byte[])((byte[])row[12]);
                           row_type = Functions.getType(row_type_raw);
                        } while((rollback_type != 0 || row_rolled_back != 0) && (rollback_type != 1 || row_rolled_back != 1));

                        modify_count += row_amount;
                        int action = 0;
                        if (rollback_type == 0 && row_action == 0) {
                           action = 1;
                        }

                        if (rollback_type == 1 && row_action == 1) {
                           action = 1;
                        }

                        ItemStack itemstack = new ItemStack(row_type, row_amount, (short)row_data);
                        Object[] populatedStack = Lookup.populateItemStack(itemstack, row_metadata);
                        int slot = (Integer)populatedStack[0];
                        itemstack = (ItemStack)populatedStack[1];
                        Lookup.modifyContainerItems(type, container, slot, itemstack, action);
                     }
                  }

                  Config.rollback_hash.put(finalUser_string, new int[]{item_count, modify_count, entity_count, 1});
               } catch (Exception var21) {
                  var21.printStackTrace();
               }

            }
         }, 0L);
         int[] rollback_hash_data = (int[])Config.rollback_hash.get(user_string);
         int next = rollback_hash_data[3];
         int sleep_time = 0;

         while(next == 0) {
            sleep_time += 5;
            Thread.sleep(5L);
            rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
            next = rollback_hash_data[3];
            if (sleep_time > 300000) {
               System.out.println("[CoreProtect] Откат или восстановление прервано.");
               break;
            }
         }

         rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
         int block_count = rollback_hash_data[1];
         long time2 = System.currentTimeMillis();
         int seconds = (int)((time2 - time1) / 1000L);
         if (user != null) {
            int file = -1;
            if (block_count > 0) {
               file = 1;
            }

            int item_count = 0;
            int entity_count = 0;
            finishRollbackRestore(user, location, check_users, restrict_list, exclude_list, exclude_user_list, action_list, time_string, file, seconds, item_count, block_count, entity_count, rollback_type, radius, verbose, restrict_world, 0);
         }
      } catch (Exception var31) {
         var31.printStackTrace();
      }

   }

   public static List<String[]> performLookup(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, boolean restrict_world, boolean lookup) {
      Object new_list = new ArrayList();

      try {
         List<Object[]> lookup_list = performLookupRaw(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, -1, -1, restrict_world, lookup);
         new_list = convertRawLookup(statement, lookup_list);
      } catch (Exception var15) {
         var15.printStackTrace();
      }

      return (List)new_list;
   }

   public static List<Object[]> performLookupRaw(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, int limit_offset, int limit_count, boolean restrict_world, boolean lookup) {
      List<Object[]> list = new ArrayList();
      List<Integer> invalid_rollback_actions = new ArrayList();
      invalid_rollback_actions.add(2);
      if ((Integer)Config.config.get("rollback-entities") == 0 && !action_list.contains(3)) {
         invalid_rollback_actions.add(3);
      }

      try {
         while(Consumer.is_paused) {
            Thread.sleep(1L);
         }

         Consumer.is_paused = true;
         ResultSet rs = rawLookupResultSet(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, limit_offset, limit_count, restrict_world, lookup, false);

         label68:
         while(true) {
            while(true) {
               while(rs.next()) {
                  int result_amount;
                  int result_time;
                  int result_id;
                  String result_user;
                  Object[] data_array;
                  data_array = new Object[0];
                  if (!action_list.contains(6) && !action_list.contains(7)) {
                     int result_type;
                     int result_data;
                     int result_rolled_back;
                     int result_userid;
                     int result_action;
                     if (action_list.contains(8)) {
                        result_amount = rs.getInt("id");
                        result_time = rs.getInt("time");
                        result_id = rs.getInt("user");
                        result_userid = rs.getInt("wid");
                        result_action = rs.getInt("x");
                        result_type = rs.getInt("y");
                        result_data = rs.getInt("z");
                        result_rolled_back = rs.getInt("action");
                        data_array = new Object[]{result_amount, result_time, result_id, result_userid, result_action, result_type, result_data, result_rolled_back};
                        list.add(data_array);
                     } else if (action_list.contains(9)) {
                        result_amount = rs.getInt("id");
                        result_time = rs.getInt("time");
                        String result_uuid = rs.getString("uuid");
                        result_user = rs.getString("user");
                        data_array = new Object[]{result_amount, result_time, result_uuid, result_user};
                        list.add(data_array);
                     } else {
                        result_amount = 0;
                        byte[] result_meta = null;
                        result_id = rs.getInt("id");
                        result_userid = rs.getInt("user");
                        result_action = rs.getInt("action");
                        result_type = rs.getInt("type");
                        result_data = rs.getInt("data");
                        result_rolled_back = rs.getInt("rolled_back");
                        result_time = rs.getInt("time");
                        int result_x = rs.getInt("x");
                        int result_y = rs.getInt("y");
                        int result_z = rs.getInt("z");
                        int result_wid = rs.getInt("wid");
                        if (!action_list.contains(4) && !action_list.contains(5)) {
                           result_meta = rs.getBytes("meta");
                        } else {
                           result_amount = rs.getInt("amount");
                           result_meta = rs.getBytes("metadata");
                        }

                        boolean valid = true;
                        if (!lookup && invalid_rollback_actions.contains(result_action)) {
                           valid = false;
                        }

                        if (valid) {
                            if (!action_list.contains(4) && !action_list.contains(5)) {
                              data_array = new Object[]{result_id, result_time, result_userid, result_x, result_y, result_z, result_type, result_data, result_action, result_rolled_back, result_wid, result_meta};
                              list.add(data_array);
                           } else {
                              data_array = new Object[]{result_id, result_time, result_userid, result_x, result_y, result_z, result_type, result_data, result_action, result_rolled_back, result_wid, result_amount, result_meta};
                              list.add(data_array);
                           }
                        }
                     }
                  } else {
                     result_amount = rs.getInt("id");
                     result_time = rs.getInt("time");
                     result_id = rs.getInt("user");
                     result_user = rs.getString("message");
                     data_array = new Object[]{result_amount, result_time, result_id, result_user};
                     list.add(data_array);
                  }
               }

               rs.close();
               break label68;
            }
         }
      } catch (Exception var33) {
         var33.printStackTrace();
      }

      Consumer.is_paused = false;
      return list;
   }

   public static List<String[]> performPartialLookup(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, int limit_offset, int limit_count, boolean restrict_world, boolean lookup) {
      Object new_list = new ArrayList();

      try {
         List<Object[]> lookup_list = performLookupRaw(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, limit_offset, limit_count, restrict_world, lookup);
         new_list = convertRawLookup(statement, lookup_list);
      } catch (Exception var17) {
         var17.printStackTrace();
      }

      return (List)new_list;
   }

   public static List<String[]> performRollbackRestore(Statement statement, final CommandSender user, List<String> check_uuids, List<String> check_users, String time_string, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, boolean restrict_world, boolean lookup, boolean verbose, final int rollback_type, final int preview) {
      new ArrayList();

      try {
         long time1 = System.currentTimeMillis();
         List<Object[]> lookup_list = new ArrayList();
         if (!action_list.contains(4) && !action_list.contains(5) && !check_users.contains("#container")) {
            lookup_list = performLookupRaw(statement, user, check_uuids, check_users, restrict_list, exclude_list, exclude_user_list, action_list, location, radius, check_time, -1, -1, restrict_world, lookup);
         }

         if (lookup_list == null) {
            return null;
         } else {
            boolean rollbackItems = false;
            List<Object> itemRestrictList = new ArrayList(restrict_list);
            List<Object> itemExcludeList = new ArrayList(exclude_list);
            if (action_list.contains(1)) {
               Iterator var24 = restrict_list.iterator();

               while(var24.hasNext()) {
                  Object value = var24.next();
                  if (value instanceof Material && !exclude_list.contains(value) && BlockInfo.containers.contains(value)) {
                     rollbackItems = true;
                     itemRestrictList.clear();
                     itemExcludeList.clear();
                     break;
                  }
               }
            }

            List<Object[]> item_list = new ArrayList();
            if ((Integer)Config.config.get("rollback-items") == 1 && !check_users.contains("#container") && (action_list.size() == 0 || action_list.contains(4) || rollbackItems) && preview == 0) {
               List<Integer> item_action_list = new ArrayList();
               item_action_list.addAll(action_list);
               if (!item_action_list.contains(4)) {
                  item_action_list.add(4);
               }

               item_list = performLookupRaw(statement, user, check_uuids, check_users, itemRestrictList, itemExcludeList, exclude_user_list, item_action_list, location, radius, check_time, -1, -1, restrict_world, lookup);
            }

            TreeMap<String, Integer> chunk_list = new TreeMap();
            final HashMap<String, ArrayList<Object[]>> data_list = new HashMap();
            final HashMap<String, ArrayList<Object[]>> item_data_list = new HashMap();

            int chunk_x;
            int item_count;
            int block_count;
            for(int list_c = 0; list_c < 2; ++list_c) {
               List<Object[]> scan_list = lookup_list;
               if (list_c == 1) {
                  scan_list = item_list;
               }

               Object[] result;
               HashMap modify_list;
               for(Iterator var30 = ((List)scan_list).iterator(); var30.hasNext(); ((ArrayList)modify_list.get(chunk_x + "." + item_count)).add(result)) {
                  result = (Object[])var30.next();
                  int user_id = (Integer)result[2];
                  chunk_x = (Integer)result[3] >> 4;
                  item_count = (Integer)result[5] >> 4;
                  if (chunk_list.get(chunk_x + "." + item_count) == null) {
                     block_count = 0;
                     if (location != null) {
                        block_count = (int)Math.sqrt(Math.pow((double)((Integer)result[3] - location.getBlockX()), 2.0D) + Math.pow((double)((Integer)result[5] - location.getBlockZ()), 2.0D));
                     }

                     chunk_list.put(chunk_x + "." + item_count, block_count);
                  }

                  if (Config.player_id_cache_reversed.get(user_id) == null) {
                     Database.loadUserName(statement.getConnection(), user_id);
                  }

                  modify_list = data_list;
                  if (list_c == 1) {
                     modify_list = item_data_list;
                  }

                  if (modify_list.get(chunk_x + "." + item_count) == null) {
                     data_list.put(chunk_x + "." + item_count, new ArrayList());
                     item_data_list.put(chunk_x + "." + item_count, new ArrayList());
                  }
               }
            }

            if (rollback_type == 1) {
               Iterator it = data_list.entrySet().iterator();

               while(it.hasNext()) {
                  Collections.reverse((List)((Entry)it.next()).getValue());
               }

               it = item_data_list.entrySet().iterator();

               while(it.hasNext()) {
                  Collections.reverse((List)((Entry)it.next()).getValue());
               }
            }

            int file = 0;
            String user_string = "#server";
            if (user != null) {
               user_string = user.getName();
               if (verbose && preview == 0) {
                  user.sendMessage("§3CoreProtect §f- Найдено " + chunk_list.size() + " чанков к изменению.");
               }
            }

            if (preview == 0) {
               Queue.queueRollbackUpdate(user_string, location, (List)lookup_list, rollback_type);
               Queue.queueContainerRollbackUpdate(user_string, location, (List)item_list, rollback_type);
            }

            Config.rollback_hash.put(user_string, new int[]{0, 0, 0, 0});
            final String final_user_string = user_string;
            Iterator var55 = Functions.entriesSortedByValues(chunk_list).iterator();

            while(var55.hasNext()) {
               Entry<String, Integer> entry = (Entry)var55.next();
               ++file;
               int entity_count;
               int[] rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
               item_count = rollback_hash_data[0];
               block_count = rollback_hash_data[1];
               entity_count = rollback_hash_data[2];
               String[] chunk_cords = ((String)entry.getKey()).split("\\.");
               final int final_chunk_x = Integer.parseInt(chunk_cords[0]);
               final int final_chunk_z = Integer.parseInt(chunk_cords[1]);
               Config.rollback_hash.put(final_user_string, new int[]{item_count, block_count, entity_count, 0});
               CoreProtect.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(CoreProtect.getInstance(), new Runnable() {
                  public void run() {
                     try {
                        boolean clearInventories = false;
                        if ((Integer)Config.config.get("rollback-items") == 1) {
                           clearInventories = true;
                        }

                        ArrayList<Object[]> data = (ArrayList)data_list.get(final_chunk_x + "." + final_chunk_z);
                        ArrayList<Object[]> item_data = (ArrayList)item_data_list.get(final_chunk_x + "." + final_chunk_z);
                        Map<String, Integer> hanging_delay = new HashMap();
                        Iterator var55 = data.iterator();

                        int last_y;
                        int last_z;
                        int entity_countx;
                        int item_countx;
                        int row_x;
                        int row_y;
                        int row_z;
                        int row_type_raw;
                        int row_data;
                        int row_action;
                        int player_x;
                        int row_wid;
                        String world;
                        int old_type_raw;
                        int entity_id;
                        Block block;
                        while(var55.hasNext()) {
                           Object[] row = (Object[])var55.next();
                           int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
                           int[] rollback_hash_datax = (int[])Config.rollback_hash.get(final_user_string);
                           last_y = rollback_hash_datax[0];
                           last_z = rollback_hash_datax[1];
                           entity_countx = rollback_hash_datax[2];
                           int row_time = (Integer)row[1];
                           item_countx = (Integer)row[2];
                           row_x = (Integer)row[3];
                           row_y = (Integer)row[4];
                           row_z = (Integer)row[5];
                           row_type_raw = (Integer)row[6];
                           row_data = (Integer)row[7];
                           row_action = (Integer)row[8];
                           player_x = (Integer)row[9];
                           row_wid = (Integer)row[10];
                           byte[] row_meta = (byte[])((byte[])row[11]);
                           Material row_type = Functions.getType(row_type_raw);
                           List<Object> meta = null;
                           if (row_meta != null) {
                              ByteArrayInputStream bais = new ByteArrayInputStream(row_meta);
                              ObjectInputStream ins = new ObjectInputStream(bais);
                              List<Object> list = (List)ins.readObject();
                              meta = list;
                           }

                           String row_user = (String)Config.player_id_cache_reversed.get(item_countx);
                           old_type_raw = row_type_raw;
                           Material old_type_material = Functions.getType(row_type_raw);
                           if (row_action == 1 && rollback_type == 0) {
                              row_type = Material.AIR;
                              row_type_raw = 0;
                           } else if (row_action == 0 && rollback_type == 1) {
                              row_type = Material.AIR;
                              row_type_raw = 0;
                           } else if (row_action == 4 && rollback_type == 0) {
                              row_type = null;
                              row_type_raw = 0;
                           } else if (row_action == 3 && rollback_type == 1) {
                              row_type = null;
                              row_type_raw = 0;
                           }

                           if (preview > 0) {
                              if (row_action != 3) {
                                 Player player = (Player)user;
                                 world = Functions.getWorldName(row_wid);
                                 if (world.length() == 0) {
                                    continue;
                                 }

                                 Location locationx = new Location(CoreProtect.getInstance().getServer().getWorld(world), (double)row_x, (double)row_y, (double)row_z);
                                 if (preview == 2) {
                                    Block blockx = locationx.getBlock();
                                    Material block_type = blockx.getType();
                                    byte block_data = Functions.getData(blockx);
                                    if (!block_type.equals(Material.PAINTING) && !block_type.equals(Material.ITEM_FRAME) && !block_type.equals(Material.ARMOR_STAND)) {
                                       Functions.sendBlockChange(player, locationx, block_type, block_data);
                                       ++last_z;
                                    }
                                 } else if (!row_type.equals(Material.PAINTING) && !row_type.equals(Material.ITEM_FRAME) && !row_type.equals(Material.ARMOR_STAND)) {
                                    Functions.sendBlockChange(player, locationx, row_type, (byte)row_data);
                                    ++last_z;
                                 }
                              }
                           } else {
                              int delay;
                              int xmax;
                              int ymin;
                              int ymax;
                              int zmin;
                              if (row_action == 3) {
                                 String worldx = Functions.getWorldName(row_wid);
                                 if (worldx.length() == 0) {
                                    continue;
                                 }

                                 Block blockxx = CoreProtect.getInstance().getServer().getWorld(worldx).getBlockAt(row_x, row_y, row_z);
                                 if (!CoreProtect.getInstance().getServer().getWorld(worldx).isChunkLoaded(blockxx.getChunk())) {
                                    CoreProtect.getInstance().getServer().getWorld(worldx).loadChunk(blockxx.getChunk());
                                 }

                                 if (row_type_raw > 0) {
                                    if (player_x == 0) {
                                       EntityType entity_type = Functions.getEntityType(row_type_raw);
                                       Lookup.queueEntitySpawn(row_user, blockxx.getState(), entity_type, row_data);
                                       ++entity_countx;
                                    }
                                 } else if (old_type_raw > 0 && player_x == 1) {
                                    boolean removed = false;
                                    entity_id = -1;
                                    String entity_name = Functions.getEntityType(old_type_raw).name();
                                    String token = "" + row_x + "." + row_y + "." + row_z + "." + row_wid + "." + entity_name + "";
                                    Object[] cached_entity = (Object[])Config.entity_cache.get(token);
                                    if (cached_entity != null) {
                                       entity_id = (Integer)cached_entity[1];
                                    }

                                    delay = row_x - 5;
                                    xmax = row_x + 5;
                                    ymin = row_y - 1;
                                    ymax = row_y + 1;
                                    zmin = row_z - 5;
                                    int zmax = row_z + 5;
                                    Entity[] var41 = blockxx.getChunk().getEntities();
                                    int var42 = var41.length;

                                    int id;
                                    for(id = 0; id < var42; ++id) {
                                       Entity e = var41[id];
                                       if (entity_id > -1) {
                                          int idx = e.getEntityId();
                                          if (idx == entity_id) {
                                             ++entity_countx;
                                             removed = true;
                                             e.remove();
                                             break;
                                          }
                                       } else if (e.getType().equals(Functions.getEntityType(old_type_raw))) {
                                          Location el = e.getLocation();
                                          int e_x = el.getBlockX();
                                          int e_y = el.getBlockY();
                                          int e_z = el.getBlockZ();
                                          if (e_x >= delay && e_x <= xmax && e_y >= ymin && e_y <= ymax && e_z >= zmin && e_z <= zmax) {
                                             ++entity_countx;
                                             removed = true;
                                             e.remove();
                                             break;
                                          }
                                       }
                                    }

                                    if (!removed && entity_id > -1) {
                                       Iterator var131 = blockxx.getWorld().getLivingEntities().iterator();

                                       while(var131.hasNext()) {
                                          Entity ex = (Entity)var131.next();
                                          id = ex.getEntityId();
                                          if (id == entity_id) {
                                             ++entity_countx;
                                             removed = true;
                                             ex.remove();
                                             break;
                                          }
                                       }
                                    }
                                 }
                              } else {
                                 List<Material> update_state = Arrays.asList(Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.TORCH, Material.REDSTONE_WIRE, Material.BURNING_FURNACE, Material.LEVER, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.GLOWSTONE, Material.JACK_O_LANTERN, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.REDSTONE_LAMP_ON, Material.BEACON, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.DAYLIGHT_DETECTOR, Material.REDSTONE_BLOCK, Material.HOPPER, Material.ACTIVATOR_RAIL);
                                 world = Functions.getWorldName(row_wid);
                                 if (world.length() == 0) {
                                    continue;
                                 }

                                 block = CoreProtect.getInstance().getServer().getWorld(world).getBlockAt(row_x, row_y, row_z);
                                 if (!CoreProtect.getInstance().getServer().getWorld(world).isChunkLoaded(block.getChunk())) {
                                    CoreProtect.getInstance().getServer().getWorld(world).loadChunk(block.getChunk());
                                 }

                                 boolean change_block = true;
                                 boolean count_block = true;
                                 Material ctype = block.getType();
                                 int cdata = Functions.getData(block);
                                 if (player_x == 1 && rollback_type == 0) {
                                    count_block = false;
                                 }

                                 if (row_type.equals(ctype) && !old_type_material.equals(Material.PAINTING) && !old_type_material.equals(Material.ITEM_FRAME) && !old_type_material.equals(Material.ARMOR_STAND) && !old_type_material.equals(Material.END_CRYSTAL)) {
                                    if (row_data == cdata) {
                                       change_block = false;
                                    }

                                    count_block = false;
                                 } else if (!ctype.equals(Material.AIR)) {
                                    count_block = true;
                                 }

                                 if (count_block) {
                                    List<Material> c1 = Arrays.asList(Material.GRASS, Material.WATER, Material.LAVA);
                                    List<Material> c2 = Arrays.asList(Material.DIRT, Material.STATIONARY_WATER, Material.STATIONARY_LAVA);
                                    ymin = 0;

                                    for(Iterator var112 = c1.iterator(); var112.hasNext(); ++ymin) {
                                       Material cv1 = (Material)var112.next();
                                       Material cv2 = (Material)c2.get(ymin);
                                       if (row_type.equals(cv1) && ctype.equals(cv2) || row_type.equals(cv2) && ctype.equals(cv1)) {
                                          count_block = false;
                                       }
                                    }
                                 }

                                 try {
                                    if (change_block) {
                                       if (row_type.equals(Material.AIR) && (old_type_material.equals(Material.PAINTING) || old_type_material.equals(Material.ITEM_FRAME))) {
                                          delay = Functions.getHangingDelay(hanging_delay, row_wid, row_x, row_y, row_z);
                                          Lookup.queueHangingRemove(row_user, block.getState(), delay);
                                       } else if (row_type.equals(Material.DOUBLE_PLANT) || row_type.equals(Material.AIR) && old_type_material.equals(Material.DOUBLE_PLANT)) {
                                          if (row_data < 8) {
                                             int top_data = 8;
                                             if (row_data == 0 || row_data == 4) {
                                                top_data = 9;
                                             }

                                             Block block_above = CoreProtect.getInstance().getServer().getWorld(world).getBlockAt(row_x, row_y + 1, row_z);
                                             Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                             Functions.setTypeAndData(block_above, row_type, (byte)top_data, false);
                                          }
                                       } else if (!row_type.equals(Material.PAINTING) && !row_type.equals(Material.ITEM_FRAME)) {
                                          Location location;
                                          boolean exists;
                                          Entity[] var126;
                                          Entity entityxx;
                                          Entity entityxxx;
                                          if (row_type.equals(Material.ARMOR_STAND)) {
                                             location = block.getLocation();
                                             location.setX(location.getX() + 0.5D);
                                             location.setZ(location.getZ() + 0.5D);
                                             location.setYaw((float)row_data);
                                             exists = false;
                                             var126 = block.getChunk().getEntities();
                                             ymax = var126.length;

                                             for(zmin = 0; zmin < ymax; ++zmin) {
                                                entityxxx = var126[zmin];
                                                if (entityxxx instanceof ArmorStand && entityxxx.getLocation().getBlockX() == location.getBlockX() && entityxxx.getLocation().getBlockY() == location.getBlockY() && entityxxx.getLocation().getBlockZ() == location.getBlockZ()) {
                                                   exists = true;
                                                }
                                             }

                                             if (!exists) {
                                                entityxx = block.getLocation().getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
                                                entityxx.teleport(location);
                                             }
                                          } else if (row_type.equals(Material.END_CRYSTAL)) {
                                             location = block.getLocation();
                                             location.setX(location.getX() + 0.5D);
                                             location.setZ(location.getZ() + 0.5D);
                                             exists = false;
                                             var126 = block.getChunk().getEntities();
                                             ymax = var126.length;

                                             for(zmin = 0; zmin < ymax; ++zmin) {
                                                entityxxx = var126[zmin];
                                                if (entityxxx instanceof EnderCrystal && entityxxx.getLocation().getBlockX() == location.getBlockX() && entityxxx.getLocation().getBlockY() == location.getBlockY() && entityxxx.getLocation().getBlockZ() == location.getBlockZ()) {
                                                   exists = true;
                                                }
                                             }

                                             if (!exists) {
                                                entityxx = block.getLocation().getWorld().spawnEntity(location, EntityType.ENDER_CRYSTAL);
                                                EnderCrystal enderCrystal = (EnderCrystal)entityxx;
                                                enderCrystal.setShowingBottom(row_data != 0);
                                                entityxx.teleport(location);
                                             }
                                          } else {
                                             Entity[] var107;
                                             Entity entityx;
                                             if (row_type.equals(Material.AIR) && old_type_material.equals(Material.END_CRYSTAL)) {
                                                var107 = block.getChunk().getEntities();
                                                xmax = var107.length;

                                                for(ymin = 0; ymin < xmax; ++ymin) {
                                                   entityx = var107[ymin];
                                                   if (entityx instanceof EnderCrystal && entityx.getLocation().getBlockX() == row_x && entityx.getLocation().getBlockY() == row_y && entityx.getLocation().getBlockZ() == row_z) {
                                                      entityx.remove();
                                                   }
                                                }
                                             } else if (rollback_type != 0 || row_action != 0 || !row_type.equals(Material.AIR)) {
                                                Inventory inventory;
                                                if (!row_type.equals(Material.AIR) && !row_type.equals(Material.TNT)) {
                                                   if (row_type.equals(Material.MOB_SPAWNER)) {
                                                      try {
                                                         Functions.setTypeAndData(block, row_type, (byte)0, false);
                                                         CreatureSpawner mobSpawner = (CreatureSpawner)block.getState();
                                                         mobSpawner.setSpawnedType(Functions.getSpawnerType(row_data));
                                                         if (count_block) {
                                                            ++last_z;
                                                         }
                                                      } catch (Exception var49) {
                                                      }
                                                   } else if (row_type.equals(Material.SKULL)) {
                                                      block.setType(row_type, false);
                                                      Lookup.queueSkullUpdate(row_user, block.getState(), row_data);
                                                      if (count_block) {
                                                         ++last_z;
                                                      }
                                                   } else if (!row_type.equals(Material.SIGN_POST) && !row_type.equals(Material.WALL_SIGN)) {
                                                      Iterator var110;
                                                      Object value;
                                                      if (BlockInfo.shulker_boxes.contains(row_type)) {
                                                         Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                         if (count_block) {
                                                            ++last_z;
                                                         }

                                                         if (meta != null) {
                                                            inventory = Functions.getContainerInventory(block.getState(), false);
                                                            var110 = meta.iterator();

                                                            while(var110.hasNext()) {
                                                               value = var110.next();
                                                               if (value instanceof Map) {
                                                                  Map<Integer, Object> itemMap = (Map)value;
                                                                  ItemStack item = ItemStack.deserialize((Map)itemMap.get(0));
                                                                  List<List<Map<String, Object>>> metadata = (List)itemMap.get(1);
                                                                  Object[] populatedStackx = Lookup.populateItemStack(item, metadata);
                                                                  item = (ItemStack)populatedStackx[1];
                                                                  Lookup.modifyContainerItems(item.getType(), inventory, 0, item, 1);
                                                               }
                                                            }
                                                         }
                                                      } else if (row_type.equals(Material.COMMAND)) {
                                                         Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                         if (count_block) {
                                                            ++last_z;
                                                         }

                                                         if (meta != null) {
                                                            CommandBlock command_block = (CommandBlock)block.getState();
                                                            var110 = meta.iterator();

                                                            while(var110.hasNext()) {
                                                               value = var110.next();
                                                               if (value instanceof String) {
                                                                  String string = (String)value;
                                                                  command_block.setCommand(string);
                                                                  command_block.update();
                                                               }
                                                            }
                                                         }
                                                      } else if (!row_type.equals(Material.WALL_BANNER) && !row_type.equals(Material.STANDING_BANNER)) {
                                                         if (update_state.contains(row_type)) {
                                                            Functions.setTypeAndData(block, row_type, (byte)row_data, true);
                                                            if (count_block) {
                                                               ++last_z;
                                                            }
                                                         } else if (row_type != ctype && BlockInfo.containers.contains(row_type) && BlockInfo.containers.contains(ctype)) {
                                                            block.setType(Material.AIR);
                                                            Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                            if (count_block) {
                                                               ++last_z;
                                                            }
                                                         } else {
                                                            if (BlockInfo.containers.contains(row_type)) {
                                                               block.setType(row_type);
                                                               Functions.setData(block, (byte)row_data);
                                                            } else {
                                                               Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                            }

                                                            if (count_block) {
                                                               ++last_z;
                                                            }
                                                         }
                                                      } else {
                                                         Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                         if (count_block) {
                                                            ++last_z;
                                                         }

                                                         if (meta != null) {
                                                            Banner banner = (Banner)block.getState();
                                                            var110 = meta.iterator();

                                                            while(var110.hasNext()) {
                                                               value = var110.next();
                                                               if (value instanceof DyeColor) {
                                                                  banner.setBaseColor((DyeColor)value);
                                                               } else if (value instanceof Map) {
                                                                  Pattern pattern = new Pattern((Map)value);
                                                                  banner.addPattern(pattern);
                                                               }
                                                            }

                                                            banner.update();
                                                         }
                                                      }
                                                   } else {
                                                      Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                      Lookup.queueSignUpdate(row_user, block.getState(), rollback_type, row_time);
                                                      if (count_block) {
                                                         ++last_z;
                                                      }
                                                   }
                                                } else {
                                                   if (clearInventories) {
                                                      if (BlockInfo.containers.contains(ctype)) {
                                                         inventory = Functions.getContainerInventory(block.getState(), false);
                                                         if (inventory != null) {
                                                            inventory.clear();
                                                         }
                                                      } else if (BlockInfo.containers.contains(Material.ARMOR_STAND) && old_type_material.equals(Material.ARMOR_STAND)) {
                                                         var107 = block.getChunk().getEntities();
                                                         xmax = var107.length;

                                                         for(ymin = 0; ymin < xmax; ++ymin) {
                                                            entityx = var107[ymin];
                                                            if (entityx instanceof ArmorStand && entityx.getLocation().getBlockX() == row_x && entityx.getLocation().getBlockY() == row_y && entityx.getLocation().getBlockZ() == row_z) {
                                                               EntityEquipment equipment = Functions.getEntityEquipment((LivingEntity)entityx);
                                                               if (equipment != null) {
                                                                  equipment.clear();
                                                               }

                                                               Location locationxx = entityx.getLocation();
                                                               locationxx.setY(locationxx.getY() - 1.0D);
                                                               entityx.teleport(locationxx);
                                                               entityx.remove();
                                                            }
                                                         }
                                                      }
                                                   }

                                                   Functions.setTypeAndData(block, row_type, (byte)row_data, false);
                                                   if (count_block) {
                                                      ++last_z;
                                                   }
                                                }
                                             }
                                          }
                                       } else {
                                          delay = Functions.getHangingDelay(hanging_delay, row_wid, row_x, row_y, row_z);
                                          Lookup.queueHangingSpawn(row_user, block.getState(), row_type, row_data, delay);
                                       }
                                    }
                                 } catch (Exception var50) {
                                    var50.printStackTrace();
                                 }

                                 if (!row_type.equals(Material.AIR) && change_block && row_user.length() > 0) {
                                    Config.lookup_cache.put("" + row_x + "." + row_y + "." + row_z + "." + row_wid + "", new Object[]{unixtimestamp, row_user, row_type});
                                 }
                              }
                           }

                           Config.rollback_hash.put(final_user_string, new int[]{last_y, last_z, entity_countx, 0});
                        }

                        hanging_delay.clear();
                        Object container = null;
                        Material container_type = null;
                        boolean container_init = false;
                        int last_x = 0;
                        last_y = 0;
                        last_z = 0;
                        entity_countx = 0;

                        int player_z;
                        int check_y;
                        for(Iterator var60 = item_data.iterator(); var60.hasNext(); Config.rollback_hash.put(final_user_string, new int[]{row_y, row_z, row_type_raw, 0})) {
                           Object[] rowx = (Object[])var60.next();
                           int[] rollback_hash_dataxxx = (int[])Config.rollback_hash.get(final_user_string);
                           row_y = rollback_hash_dataxxx[0];
                           row_z = rollback_hash_dataxxx[1];
                           row_type_raw = rollback_hash_dataxxx[2];
                           row_data = (Integer)rowx[3];
                           row_action = (Integer)rowx[4];
                           player_x = (Integer)rowx[5];
                           row_wid = (Integer)rowx[6];
                           player_z = (Integer)rowx[7];
                           check_y = (Integer)rowx[8];
                           int row_rolled_back = (Integer)rowx[9];
                           int row_widx = (Integer)rowx[10];
                           old_type_raw = (Integer)rowx[11];
                           byte[] row_metadata = (byte[])((byte[])rowx[12]);
                           Material row_typex = Functions.getType(row_wid);
                           if (rollback_type == 0 && row_rolled_back == 0 || rollback_type == 1 && row_rolled_back == 1) {
                              int slot;
                              if (!container_init || row_data != last_x || row_action != last_y || player_x != last_z || row_widx != entity_countx) {
                                 container = null;
                                 world = Functions.getWorldName(row_widx);
                                 block = CoreProtect.getInstance().getServer().getWorld(world).getBlockAt(row_data, row_action, player_x);
                                 if (!CoreProtect.getInstance().getServer().getWorld(world).isChunkLoaded(block.getChunk())) {
                                    CoreProtect.getInstance().getServer().getWorld(world).loadChunk(block.getChunk());
                                 }

                                 if (BlockInfo.containers.contains(block.getType())) {
                                    container = Functions.getContainerInventory(block.getState(), false);
                                    container_type = block.getType();
                                 } else if (BlockInfo.containers.contains(Material.ARMOR_STAND)) {
                                    Entity[] var97 = block.getChunk().getEntities();
                                    slot = var97.length;

                                    for(int var98 = 0; var98 < slot; ++var98) {
                                       Entity entity = var97[var98];
                                       if (entity instanceof ArmorStand && entity.getLocation().getBlockX() == row_data && entity.getLocation().getBlockY() == row_action && entity.getLocation().getBlockZ() == player_x) {
                                          container = Functions.getEntityEquipment((LivingEntity)entity);
                                          container_type = Material.ARMOR_STAND;
                                       }
                                    }
                                 }

                                 last_x = row_data;
                                 last_y = row_action;
                                 last_z = player_x;
                                 entity_countx = row_widx;
                              }

                              if (container != null) {
                                 int action = 0;
                                 if (rollback_type == 0 && check_y == 0) {
                                    action = 1;
                                 }

                                 if (rollback_type == 1 && check_y == 1) {
                                    action = 1;
                                 }

                                 ItemStack itemstack = new ItemStack(row_typex, old_type_raw, (short)player_z);
                                 Object[] populatedStack = Lookup.populateItemStack(itemstack, row_metadata);
                                 slot = (Integer)populatedStack[0];
                                 itemstack = (ItemStack)populatedStack[1];
                                 Lookup.modifyContainerItems(container_type, container, slot, itemstack, action);
                                 row_y += old_type_raw;
                              }

                              container_init = true;
                           }
                        }

                        int[] rollback_hash_dataxx = (int[])Config.rollback_hash.get(final_user_string);
                        item_countx = rollback_hash_dataxx[0];
                        row_x = rollback_hash_dataxx[1];
                        row_y = rollback_hash_dataxx[2];
                        Config.rollback_hash.put(final_user_string, new int[]{item_countx, row_x, row_y, 1});
                        if (user instanceof Player && preview == 0) {
                           Player playerx = (Player)user;
                           Location locationxxx = playerx.getLocation();
                           Chunk chunk = locationxxx.getChunk();
                           if (chunk.getX() == final_chunk_x && chunk.getZ() == final_chunk_z) {
                              List<Material> unsafe_blocks = Arrays.asList(Material.LAVA, Material.FIRE);
                              player_x = locationxxx.getBlockX();
                              row_wid = locationxxx.getBlockY();
                              player_z = locationxxx.getBlockZ();
                              check_y = row_wid - 1;
                              boolean safe_block = false;

                              for(boolean place_safe = false; !safe_block; ++check_y) {
                                 old_type_raw = check_y + 1;
                                 if (old_type_raw > 256) {
                                    old_type_raw = 256;
                                 }

                                 Block block_type1 = locationxxx.getWorld().getBlockAt(player_x, check_y, player_z);
                                 Block block_type2 = locationxxx.getWorld().getBlockAt(player_x, old_type_raw, player_z);
                                 Material type1 = block_type1.getType();
                                 Material type2 = block_type2.getType();
                                 if (!Functions.solidBlock(type1) && !Functions.solidBlock(type2)) {
                                    if (unsafe_blocks.contains(type1)) {
                                       place_safe = true;
                                    } else {
                                       safe_block = true;
                                       if (place_safe) {
                                          entity_id = check_y - 1;
                                          Block block_below = locationxxx.getWorld().getBlockAt(player_x, entity_id, player_z);
                                          if (unsafe_blocks.contains(block_below.getType())) {
                                             block_type1.setType(Material.DIRT);
                                             ++check_y;
                                          }
                                       }
                                    }
                                 }

                                 if (check_y >= 256) {
                                    safe_block = true;
                                 }

                                 if (safe_block && check_y > row_wid) {
                                    if (check_y > 256) {
                                       check_y = 256;
                                    }

                                    locationxxx.setY((double)check_y);
                                    playerx.teleport(locationxxx);
                                    playerx.sendMessage("§3CoreProtect §f- Телепорт в безопасное место.");
                                    if (place_safe) {
                                       playerx.sendMessage("§3CoreProtect §f- Поставлен блок земли под тобой.");
                                    }
                                 }
                              }
                           }
                        }
                     } catch (Exception var51) {
                        var51.printStackTrace();
                        int[] rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
                        int item_count = rollback_hash_data[0];
                        int block_count = rollback_hash_data[1];
                        int entity_count = rollback_hash_data[2];
                        Config.rollback_hash.put(final_user_string, new int[]{item_count, block_count, entity_count, 2});
                     }

                  }
               }, 0L);
               rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
               int next = rollback_hash_data[3];
               int sleep_time = 0;
               boolean abort = false;

               while(next == 0) {
                  if (preview == 1) {
                     ++sleep_time;
                     Thread.sleep(1L);
                  } else {
                     sleep_time += 5;
                     Thread.sleep(5L);
                  }

                  rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
                  next = rollback_hash_data[3];
                  if (sleep_time > 300000) {
                     abort = true;
                     break;
                  }
               }

               if (abort || next == 2) {
                  System.out.println("[CoreProtect] Откат и восстановление прервано.");
                  break;
               }

               rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
               item_count = rollback_hash_data[0];
               block_count = rollback_hash_data[1];
               entity_count = rollback_hash_data[2];
               Config.rollback_hash.put(final_user_string, new int[]{item_count, block_count, entity_count, 0});
               if (verbose && user != null && preview == 0) {
                  user.sendMessage("§3CoreProtect §f- Изменено " + file + "/" + chunk_list.size() + " чанков.");
               }
            }

            int[] rollback_hash_data = (int[])Config.rollback_hash.get(final_user_string);
            chunk_x = rollback_hash_data[0];
            item_count = rollback_hash_data[1];
            block_count = rollback_hash_data[2];
            long time2 = System.currentTimeMillis();
            int seconds = (int)((time2 - time1) / 1000L);
            if (user != null) {
               finishRollbackRestore(user, location, check_users, restrict_list, exclude_list, exclude_user_list, action_list, time_string, file, seconds, chunk_x, item_count, block_count, rollback_type, radius, verbose, restrict_world, preview);
            }

            List<String[]> list = convertRawLookup(statement, lookup_list);
            return list;
         }
      } catch (Exception var47) {
         var47.printStackTrace();
         return null;
      }
   }

   public static boolean playerExists(Connection connection, String user) {
      try {
         int id = -1;
         String uuid = null;
         if (Config.player_id_cache.get(user.toLowerCase()) != null) {
            return true;
         }

         String query = "SELECT rowid as id, uuid FROM " + Config.prefix + "user WHERE user LIKE ? LIMIT 0, 1";
         PreparedStatement preparedStmt = connection.prepareStatement(query);
         preparedStmt.setString(1, user);

         ResultSet rs;
         for(rs = preparedStmt.executeQuery(); rs.next(); uuid = rs.getString("uuid")) {
            id = rs.getInt("id");
         }

         rs.close();
         preparedStmt.close();
         if (id > -1) {
            if (uuid != null) {
               Config.uuid_cache.put(user.toLowerCase(), uuid);
               Config.uuid_cache_reversed.put(uuid, user);
            }

            Config.player_id_cache.put(user.toLowerCase(), id);
            Config.player_id_cache_reversed.put(id, user);
            return true;
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return false;
   }

   public static Object[] populateItemStack(ItemStack itemstack, List<List<Map<String, Object>>> list) {
      int slot = 0;

      try {
         Material row_type = itemstack.getType();
         int item_count = 0;
         Builder effect_builder = FireworkEffect.builder();

         for(Iterator var6 = list.iterator(); var6.hasNext(); ++item_count) {
            List<Map<String, Object>> map = (List)var6.next();
            Map<String, Object> mapData = map.get(0);
            if (mapData.get("slot") != null) {
               slot = (Integer)mapData.get("slot");
            } else if (item_count == 0) {
               ItemMeta meta = Functions.deserializeItemMeta(itemstack.getItemMeta().getClass(), map.get(0));
               itemstack.setItemMeta(meta);
            } else {
               Iterator var9;
               Map l;
               Color color;
               if (!row_type.equals(Material.LEATHER_HELMET) && !row_type.equals(Material.LEATHER_CHESTPLATE) && !row_type.equals(Material.LEATHER_LEGGINGS) && !row_type.equals(Material.LEATHER_BOOTS)) {
                  if (row_type.equals(Material.POTION)) {
                     var9 = map.iterator();

                     while(var9.hasNext()) {
                        l = (Map)var9.next();
                        PotionMeta meta = (PotionMeta)itemstack.getItemMeta();
                        PotionEffect effect = new PotionEffect(l);
                        meta.addCustomEffect(effect, true);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (row_type.equals(Material.BANNER)) {
                     var9 = map.iterator();

                     while(var9.hasNext()) {
                        l = (Map)var9.next();
                        BannerMeta meta = (BannerMeta)itemstack.getItemMeta();
                        Pattern pattern = new Pattern(l);
                        meta.addPattern(pattern);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (row_type.equals(Material.MAP)) {
                     var9 = map.iterator();

                     while(var9.hasNext()) {
                        l = (Map)var9.next();
                        MapMeta meta = (MapMeta)itemstack.getItemMeta();
                        color = Color.deserialize(l);
                        meta.setColor(color);
                        itemstack.setItemMeta(meta);
                     }
                  } else if (row_type.equals(Material.FIREWORK) || row_type.equals(Material.FIREWORK_CHARGE)) {
                     if (item_count == 1) {
                        var9 = map.iterator();

                        while(var9.hasNext()) {
                           l = (Map)var9.next();
                           boolean hasFlicker = (Boolean)l.get("flicker");
                           boolean hasTrail = (Boolean)l.get("trail");
                           effect_builder.flicker(hasFlicker);
                           effect_builder.trail(hasTrail);
                        }
                     } else {
                        if (item_count == 2) {
                           var9 = map.iterator();

                           while(var9.hasNext()) {
                              l = (Map)var9.next();
                              color = Color.deserialize(l);
                              effect_builder.withColor(color);
                           }
                        } else if (item_count == 3) {
                           var9 = map.iterator();

                           while(var9.hasNext()) {
                              l = (Map)var9.next();
                              color = Color.deserialize(l);
                              effect_builder.withFade(color);
                           }

                           FireworkEffect effect = effect_builder.build();
                           if (row_type.equals(Material.FIREWORK)) {
                              FireworkMeta meta = (FireworkMeta)itemstack.getItemMeta();
                              meta.addEffect(effect);
                              itemstack.setItemMeta(meta);
                           } else if (row_type.equals(Material.FIREWORK_CHARGE)) {
                              FireworkEffectMeta meta = (FireworkEffectMeta)itemstack.getItemMeta();
                              meta.setEffect(effect);
                              itemstack.setItemMeta(meta);
                           }

                           effect_builder = FireworkEffect.builder();
                           item_count = 0;
                        }
                     }
                  }
               } else {
                  var9 = map.iterator();

                  while(var9.hasNext()) {
                     l = (Map)var9.next();
                     LeatherArmorMeta meta = (LeatherArmorMeta)itemstack.getItemMeta();
                     color = Color.deserialize(l);
                     meta.setColor(color);
                     itemstack.setItemMeta(meta);
                  }
               }
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

      return new Object[]{slot, itemstack};
   }

   private static Object[] populateItemStack(ItemStack itemstack, byte[] metadata) {
      try {
         ByteArrayInputStream bais = new ByteArrayInputStream(metadata);
         ObjectInputStream ins = new ObjectInputStream(bais);
         List<List<Map<String, Object>>> list = (List)ins.readObject();
         return populateItemStack(itemstack, list);
      } catch (Exception var5) {
         var5.printStackTrace();
         return new Object[]{0, itemstack};
      }
   }

   private static ResultSet rawLookupResultSet(Statement statement, CommandSender user, List<String> check_uuids, List<String> check_users, List<Object> restrict_list, List<Object> exclude_list, List<String> exclude_user_list, List<Integer> action_list, Location location, Integer[] radius, int check_time, int limit_offset, int limit_count, boolean restrict_world, boolean lookup, boolean count) {
      ResultSet rs = null;

      try {
         List<Integer> valid_actions = Arrays.asList(0, 1, 2, 3);
         if (radius != null) {
            restrict_world = true;
         }

         boolean valid_action = false;
         String query_extra = "";
         String query_limit = "";
         String query_table = "block";
         String action = "";
         String exclude = "";
         String restrict = "";
         String users = "";
         String uuids = "";
         String exclude_users = "";
         String index = "";
         String rows;
         Iterator var30;
         String query;
         if (check_uuids.size() > 0) {
            rows = "";
            var30 = check_uuids.iterator();

            while(var30.hasNext()) {
               query = (String)var30.next();
               if (rows.length() == 0) {
                  rows = "'" + query + "'";
               } else {
                  rows = rows + ",'" + query + "'";
               }
            }

            uuids = rows;
         }

         int ymax;
         if (!check_users.contains("#global")) {
            rows = "";
            var30 = check_users.iterator();

            while(var30.hasNext()) {
               query = (String)var30.next();
               if (!query.equals("#container")) {
                  if (Config.player_id_cache.get(query.toLowerCase()) == null) {
                     Database.loadUserID(statement.getConnection(), query, null);
                  }

                  ymax = Config.player_id_cache.get(query.toLowerCase());
                  if (rows.length() == 0) {
                     rows = "" + ymax;
                  } else {
                     rows = rows + "," + ymax;
                  }
               }
            }

            users = rows;
         }

         Object value;
         String value_name;
         if (restrict_list.size() > 0) {
            rows = "";
            var30 = restrict_list.iterator();

            while(var30.hasNext()) {
               value = var30.next();
               value_name = "";
               if (value instanceof Material) {
                  value_name = ((Material)value).name();
                  if (rows.length() == 0) {
                     rows = "" + Functions.block_id(value_name, false);
                  } else {
                     rows = rows + "," + Functions.block_id(value_name, false);
                  }
               } else if (value instanceof EntityType) {
                  value_name = ((EntityType)value).name();
                  if (rows.length() == 0) {
                     rows = "" + Functions.getEntityId(value_name, false);
                  } else {
                     rows = rows + "," + Functions.getEntityId(value_name, false);
                  }
               }
            }

            restrict = rows;
         }

         if (exclude_list.size() > 0) {
            rows = "";
            var30 = exclude_list.iterator();

            while(var30.hasNext()) {
               value = var30.next();
               value_name = "";
               if (value instanceof Material) {
                  value_name = ((Material)value).name();
                  if (rows.length() == 0) {
                     rows = "" + Functions.block_id(value_name, false);
                  } else {
                     rows = rows + "," + Functions.block_id(value_name, false);
                  }
               } else if (value instanceof EntityType) {
                  value_name = ((EntityType)value).name();
                  if (rows.length() == 0) {
                     rows = "" + Functions.getEntityId(value_name, false);
                  } else {
                     rows = rows + "," + Functions.getEntityId(value_name, false);
                  }
               }
            }

            exclude = rows;
         }

         if (exclude_user_list.size() > 0) {
            rows = "";
            var30 = exclude_user_list.iterator();

            while(var30.hasNext()) {
               query = (String)var30.next();
               if (Config.player_id_cache.get(query.toLowerCase()) == null) {
                  Database.loadUserID(statement.getConnection(), query, null);
               }

               ymax = Config.player_id_cache.get(query.toLowerCase());
               if (rows.length() == 0) {
                  rows = "" + ymax;
               } else {
                  rows = rows + "," + ymax;
               }
            }

            exclude_users = rows;
         }

         if (action_list.size() > 0) {
            rows = "";
            var30 = action_list.iterator();

            while(var30.hasNext()) {
                value = var30.next();
               if (valid_actions.contains(value)) {
                  if (rows.length() == 0) {
                     rows = "" + value;
                  } else {
                     rows = rows + "," + value;
                  }
               }
            }

            action = rows;
         }

         Iterator var44 = action_list.iterator();

         while(var44.hasNext()) {
             value = var44.next();
            if (valid_actions.contains(value)) {
               valid_action = true;
            }
         }

         int xmin;
         if (restrict_world) {
            xmin = Functions.getWorldId(location.getWorld().getName());
            query_extra = query_extra + " wid=" + xmin + " AND";
         }

         int zmin;
         int xmax;
         int ymin;
         if (radius != null) {
            xmin = radius[1];
            xmax = radius[2];
            ymin = radius[3];
            ymax = radius[4];
            zmin = radius[5];
            int zmax = radius[6];
            String query_y = "";
            if (ymin > -1 && ymax > -1) {
               query_y = " y >= '" + ymin + "' AND y <= '" + ymax + "' AND";
            }

            query_extra = query_extra + " x >= '" + xmin + "' AND x <= '" + xmax + "' AND z >= '" + zmin + "' AND z <= '" + zmax + "' AND" + query_y;
         } else if (action_list.contains(5)) {
            xmin = Functions.getWorldId(location.getWorld().getName());
            xmax = (int)Math.floor(location.getX());
            ymin = (int)Math.floor(location.getZ());
            ymax = (int)Math.ceil(location.getX());
            zmin = (int)Math.ceil(location.getZ());
            query_extra = query_extra + " wid=" + xmin + " AND (x = '" + xmax + "' OR x = '" + ymax + "') AND (z = '" + ymin + "' OR z = '" + zmin + "') AND y = '" + location.getBlockY() + "' AND";
         }

         if (valid_action) {
            query_extra = query_extra + " action IN(" + action + ") AND";
         }

         if (restrict.length() > 0) {
            query_extra = query_extra + " type IN(" + restrict + ") AND";
         }

         if (exclude.length() > 0) {
            query_extra = query_extra + " type NOT IN(" + exclude + ") AND";
         }

         if (uuids.length() > 0) {
            query_extra = query_extra + " uuid IN(" + uuids + ") AND";
         }

         if (users.length() > 0) {
            query_extra = query_extra + " user IN(" + users + ") AND";
         }

         if (exclude_users.length() > 0) {
            query_extra = query_extra + " user NOT IN(" + exclude_users + ") AND";
         }

         if (check_time > 0) {
            query_extra = query_extra + " time > '" + check_time + "' AND";
         }

         if (query_extra.length() > 0) {
            query_extra = query_extra.substring(0, query_extra.length() - 4);
         }

         if (query_extra.length() == 0) {
            query_extra = " 1";
         }

         if (limit_offset > -1 && limit_count > -1) {
            query_limit = " LIMIT " + limit_offset + ", " + limit_count;
         }

         rows = "rowid as id,time,user,wid,x,y,z,action,type,data,meta,rolled_back";
         String query_order = " ORDER BY rowid DESC";
         if (lookup) {
            query_order = " ORDER BY time DESC";
         }

         if (!action_list.contains(4) && !action_list.contains(5)) {
            if (!action_list.contains(6) && !action_list.contains(7)) {
               if (action_list.contains(8)) {
                  query_table = "session";
                  rows = "rowid as id,time,user,wid,x,y,z,action";
               } else if (action_list.contains(9)) {
                  query_table = "username_log";
                  rows = "rowid as id,time,uuid,user";
               }
            } else {
               query_table = "chat";
               rows = "rowid as id,time,user,message";
               if (action_list.contains(7)) {
                  query_table = "command";
               }
            }
         } else {
            query_table = "container";
            rows = "rowid as id,time,user,wid,x,y,z,action,type,data,rolled_back,amount,metadata";
         }

         if (count) {
            rows = "COUNT(*) as count";
            query_limit = " LIMIT 0, 1";
            query_order = "";
         }

         if (Config.config.get("use-mysql") == 1) {
            if ((radius == null || users.length() > 0 || restrict.length() > 0) && users.length() > 0) {
            }
         } else if (query_table.equals("block")) {
            if (restrict.length() > 0 || exclude.length() > 0) {
               index = "INDEXED BY block_type_index ";
            }

            if (users.length() > 0 || exclude_users.length() > 0) {
               index = "INDEXED BY block_user_index ";
            }

            if (radius != null || action_list.contains(5) || index.equals("") && restrict_world) {
               index = "INDEXED BY block_index ";
            }
         }

         query = "SELECT " + rows + " FROM " + Config.prefix + query_table + " " + index + "WHERE" + query_extra + query_order + query_limit;
         rs = statement.executeQuery(query);
      } catch (Exception var36) {
         var36.printStackTrace();
      }

      return rs;
   }

   public static String who_placed(Statement statement, BlockState block) {
      String result = "";

      try {
         if (block == null) {
            return result;
         }

         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int time = (int)(System.currentTimeMillis() / 1000L);
         int wid = Functions.getWorldId(block.getWorld().getName());
         String query = "SELECT user,type FROM " + Config.prefix + "block WHERE wid = '" + wid + "' AND x = '" + x + "' AND z = '" + z + "' AND y = '" + y + "' AND rolled_back = '0' AND action='1' ORDER BY rowid DESC LIMIT 0, 1";
         ResultSet rs = statement.executeQuery(query);

         while(rs.next()) {
            int result_userid = rs.getInt("user");
            int result_type = rs.getInt("type");
            if (Config.player_id_cache_reversed.get(result_userid) == null) {
               Database.loadUserName(statement.getConnection(), result_userid);
            }

            result = Config.player_id_cache_reversed.get(result_userid);
            if (result.length() > 0) {
               Material result_material = Functions.getType(result_type);
               Config.lookup_cache.put(x + "." + y + "." + z + "." + wid, new Object[]{time, result, result_material});
            }
         }

         rs.close();
      } catch (Exception var13) {
         var13.printStackTrace();
      }

      return result;
   }

   public static String who_placed_cache(Block block) {
      String result = "";

      try {
         if (block == null) {
            return result;
         }

         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int wid = Functions.getWorldId(block.getWorld().getName());
         String cords = x + "." + y + "." + z + "." + wid;
         Object[] data = Config.lookup_cache.get(cords);
         if (data != null) {
            result = (String)data[1];
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      return result;
   }

   public static String who_removed_cache(BlockState block) {
      String result = "";

      try {
         if (block != null) {
            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();
            int wid = Functions.getWorldId(block.getWorld().getName());
            String cords = x + "." + y + "." + z + "." + wid;
            Object[] data = Config.break_cache.get(cords);
            if (data != null) {
               result = (String)data[1];
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      return result;
   }
}
