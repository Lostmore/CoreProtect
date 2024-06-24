package net.coreprotect.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.coreprotect.Functions;
import net.coreprotect.model.Config;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.block.banner.Pattern;
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

public class Logger {
   public static List<List<Map<String, Object>>> getItemMeta(ItemStack i, Material type, int slot) {
      List<List<Map<String, Object>>> metadata = new ArrayList();
      List<Map<String, Object>> list = new ArrayList();
      if (i.hasItemMeta() && i.getItemMeta() != null) {
         if (i.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta)i.getItemMeta().clone();
            LeatherArmorMeta sub_meta = meta.clone();
            meta.setColor(null);
            list.add(meta.serialize());
            metadata.add(list);
            list = new ArrayList();
            list.add(sub_meta.getColor().serialize());
            metadata.add(list);
         } else {
            Iterator var7;
            if (i.getItemMeta() instanceof FireworkMeta) {
               FireworkMeta meta = (FireworkMeta)i.getItemMeta().clone();
               FireworkMeta sub_meta = meta.clone();
               meta.clearEffects();
               list.add(meta.serialize());
               metadata.add(list);
               if (sub_meta.hasEffects()) {
                  var7 = sub_meta.getEffects().iterator();

                  while(var7.hasNext()) {
                     FireworkEffect effect = (FireworkEffect)var7.next();
                     getFireworkEffect(effect, metadata);
                  }
               }
            } else if (i.getItemMeta() instanceof PotionMeta) {
               PotionMeta meta = (PotionMeta)i.getItemMeta().clone();
               PotionMeta sub_meta = meta.clone();
               meta.clearCustomEffects();
               list.add(meta.serialize());
               metadata.add(list);
               if (sub_meta.hasCustomEffects()) {
                  var7 = sub_meta.getCustomEffects().iterator();

                  while(var7.hasNext()) {
                     PotionEffect effect = (PotionEffect)var7.next();
                     list = new ArrayList();
                     list.add(effect.serialize());
                     metadata.add(list);
                  }
               }
            } else if (i.getItemMeta() instanceof FireworkEffectMeta) {
               FireworkEffectMeta meta = (FireworkEffectMeta)i.getItemMeta().clone();
               FireworkEffectMeta sub_meta = meta.clone();
               meta.setEffect(null);
               list.add(meta.serialize());
               metadata.add(list);
               if (sub_meta.hasEffect()) {
                  FireworkEffect effect = sub_meta.getEffect();
                  getFireworkEffect(effect, metadata);
               }
            } else if (i.getItemMeta() instanceof BannerMeta) {
               BannerMeta meta = (BannerMeta)i.getItemMeta().clone();
               BannerMeta sub_meta = (BannerMeta)meta.clone();
               meta.setPatterns(new ArrayList());
               list.add(meta.serialize());
               metadata.add(list);
               var7 = sub_meta.getPatterns().iterator();

               while(var7.hasNext()) {
                  Pattern pattern = (Pattern)var7.next();
                  list = new ArrayList();
                  list.add(pattern.serialize());
                  metadata.add(list);
               }
            } else if (i.getItemMeta() instanceof MapMeta) {
               MapMeta meta = (MapMeta)i.getItemMeta().clone();
               MapMeta sub_meta = meta.clone();
               meta.setColor(null);
               list.add(meta.serialize());
               metadata.add(list);
               list = new ArrayList();
               list.add(sub_meta.getColor().serialize());
               metadata.add(list);
            } else {
               ItemMeta meta = i.getItemMeta().clone();
               list.add(meta.serialize());
               metadata.add(list);
            }
         }
      }

      if (type != null && type.equals(Material.ARMOR_STAND)) {
         Map<String, Object> meta = new HashMap();
         meta.put("slot", slot);
         list = new ArrayList();
         list.add(meta);
         metadata.add(list);
      }

      return metadata;
   }

   public static void container_logger(PreparedStatement preparedStmt, String user, Material type, ItemStack[] items, int action, Location l) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int slot = 0;
         ItemStack[] var7 = items;
         int var8 = items.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            ItemStack i = var7[var9];
            if (i != null && i.getAmount() > 0 && !i.getType().equals(Material.AIR)) {
               List<List<Map<String, Object>>> metadata = getItemMeta(i, type, slot);
               int wid = Functions.getWorldId(l.getWorld().getName());
               int userid = Config.player_id_cache.get(user.toLowerCase());
               int time = (int)(System.currentTimeMillis() / 1000L);
               int x = l.getBlockX();
               int y = l.getBlockY();
               int z = l.getBlockZ();
               int type_id = Functions.block_id(i.getType().name(), true);
               int data = i.getDurability();
               int amount = i.getAmount();
               Database.insertContainer(preparedStmt, time, userid, wid, x, y, z, type_id, data, amount, metadata, action, 0);
            }

            ++slot;
         }
      } catch (Exception var21) {
         var21.printStackTrace();
      }

   }

   private static void getFireworkEffect(FireworkEffect effect, List<List<Map<String, Object>>> metadata) {
      List<Map<String, Object>> color_list = new ArrayList();
      List<Map<String, Object>> fade_list = new ArrayList();
      List<Map<String, Object>> list = new ArrayList();
      Iterator var5 = effect.getColors().iterator();

      Color color;
      while(var5.hasNext()) {
         color = (Color)var5.next();
         color_list.add(color.serialize());
      }

      var5 = effect.getFadeColors().iterator();

      while(var5.hasNext()) {
         color = (Color)var5.next();
         fade_list.add(color.serialize());
      }

      Map<String, Object> has_check = new HashMap();
      has_check.put("flicker", effect.hasFlicker());
      has_check.put("trail", effect.hasTrail());
      list.add(has_check);
      metadata.add(list);
      metadata.add(color_list);
      metadata.add(fade_list);
   }

   public static void log_break(PreparedStatement preparedStmt, String user, Location location, int type, int data, List<Object> meta) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null || location == null) {
            return;
         }

         Material check_type = Functions.getType(type);
         if (check_type == null) {
            return;
         }

         if (check_type.equals(Material.AIR)) {
            return;
         }

         int wid = Functions.getWorldId(location.getWorld().getName());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int x = location.getBlockX();
         int y = location.getBlockY();
         int z = location.getBlockZ();
         Config.break_cache.put("" + x + "." + y + "." + z + "." + wid + "", new Object[]{time, user, type});
         int userid = Config.player_id_cache.get(user.toLowerCase());
         Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, type, data, meta, 0, 0);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public static void log_chat(PreparedStatement preparedStmt, int time, String user, String message) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int userid = Config.player_id_cache.get(user.toLowerCase());
         Database.insertChat(preparedStmt, time, userid, message);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static void log_command(PreparedStatement preparedStmt, int time, String user, String message) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         if (Config.blacklist.get((message + " ").split(" ")[0].toLowerCase()) != null) {
            return;
         }

         int userid = Config.player_id_cache.get(user.toLowerCase());
         Database.insertCommand(preparedStmt, time, userid, message);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

   }

   public static void log_container(PreparedStatement preparedStmt, String player, Material type, Object container, Location l) {
      try {
         ItemStack[] contents = null;
         if (type.equals(Material.ARMOR_STAND)) {
            EntityEquipment equipment = (EntityEquipment)container;
            if (equipment != null) {
               contents = equipment.getArmorContents();
            }
         } else {
            Inventory inventory = (Inventory)container;
            if (inventory != null) {
               contents = inventory.getContents();
            }
         }

         if (contents == null) {
            return;
         }

         String logging_container_id = player.toLowerCase() + "." + l.getBlockX() + "." + l.getBlockY() + "." + l.getBlockZ();
         List<ItemStack[]> old_list = Config.old_container.get(logging_container_id);
         ItemStack[] oi1 = old_list.get(0);
         ItemStack[] old_inventory = Functions.get_container_state(oi1);
         ItemStack[] new_inventory = Functions.get_container_state(contents);
         if (Config.force_containers.get(logging_container_id) != null) {
            List<ItemStack[]> force_list = Config.force_containers.get(logging_container_id);
            new_inventory = Functions.get_container_state(force_list.get(0));
            force_list.remove(0);
            if (force_list.size() == 0) {
               Config.force_containers.remove(logging_container_id);
            } else {
               Config.force_containers.put(logging_container_id, force_list);
            }
         }

         ItemStack[] var24 = old_inventory;
         int var12 = old_inventory.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            ItemStack oldi = var24[var13];
            ItemStack[] var15 = new_inventory;
            int var16 = new_inventory.length;

            for(int var17 = 0; var17 < var16; ++var17) {
               ItemStack newi = var15[var17];
               if (oldi != null && newi != null && oldi.isSimilar(newi)) {
                  int oamount = oldi.getAmount();
                  int namount = newi.getAmount();
                  if (namount >= oamount) {
                     namount -= oamount;
                     oldi.setAmount(0);
                     newi.setAmount(namount);
                  } else {
                     oamount -= namount;
                     oldi.setAmount(oamount);
                     newi.setAmount(0);
                  }
               }
            }
         }

         Functions.combine_items(type, old_inventory);
         Functions.combine_items(type, new_inventory);
         container_logger(preparedStmt, player, type, old_inventory, 0, l);
         container_logger(preparedStmt, player, type, new_inventory, 1, l);
         old_list.remove(0);
         Config.old_container.put(logging_container_id, old_list);
      } catch (Exception var21) {
         var21.printStackTrace();
      }

   }

   public static void log_container_break(PreparedStatement preparedStmt, String player, Location l, Material type, ItemStack[] old_inventory) {
      try {
         Functions.combine_items(type, old_inventory);
         container_logger(preparedStmt, player, type, old_inventory, 0, l);
         String logging_container_id = player.toLowerCase() + "." + l.getBlockX() + "." + l.getBlockY() + "." + l.getBlockZ();
         if (Config.force_containers.get(logging_container_id) != null) {
            Config.force_containers.remove(logging_container_id);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public static void log_entity_kill(PreparedStatement preparedStmt, PreparedStatement preparedStmt2, String user, BlockState block, List<Object> data, int type) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int wid = Functions.getWorldId(block.getWorld().getName());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int userid = Config.player_id_cache.get(user.toLowerCase());
         Database.insertEntity(preparedStmt2, time, data);
         ResultSet keys = preparedStmt2.getGeneratedKeys();
         keys.next();
         int entity_key = keys.getInt(1);
         keys.close();
         Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, type, entity_key, (List)null, 3, 0);
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }

   public static void log_interact(PreparedStatement preparedStmt, String user, BlockState block) {
      try {
         int type = Functions.block_id(block.getType().name(), true);
         if (Config.blacklist.get(user.toLowerCase()) != null || Functions.getType(type).equals(Material.AIR)) {
            return;
         }

         int wid = Functions.getWorldId(block.getWorld().getName());
         int userid = Config.player_id_cache.get(user.toLowerCase());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int data = Functions.getData(block);
         Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, type, data, null, 2, 0);
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   public static void log_place(PreparedStatement preparedStmt, String user, BlockState block, int replaced_type, int replaced_data, Material force_type, int force_data, boolean force, List<Object> meta) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         Material type = block.getType();
         int data = Functions.getData(block);
         if (force_type != null && force) {
            type = force_type;
            if (!force_type.equals(Material.MOB_SPAWNER) && !force_type.equals(Material.PAINTING) && !force_type.equals(Material.ITEM_FRAME) && !force_type.equals(Material.SKULL) && !force_type.equals(Material.ARMOR_STAND) && !force_type.equals(Material.END_CRYSTAL)) {
               if (user.startsWith("#")) {
                  data = force_data;
               }
            } else {
               data = force_data;
            }
         } else if (force_type != null && !type.equals(force_type)) {
            type = force_type;
            data = force_data;
         }

         if (type.equals(Material.AIR)) {
            return;
         }

         int userid = Config.player_id_cache.get(user.toLowerCase());
         int wid = Functions.getWorldId(block.getWorld().getName());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int dx = x;
         int dy = y;
         int dz = z;
         int doubledata = data;
         boolean logdouble = false;
         if (user.length() > 0) {
            Config.lookup_cache.put("" + x + "." + y + "." + z + "." + wid + "", new Object[]{time, user, type});
         }

         if (type.equals(Material.BED_BLOCK) || type.equals(Material.WOODEN_DOOR) || type.equals(Material.SPRUCE_DOOR) || type.equals(Material.BIRCH_DOOR) || type.equals(Material.JUNGLE_DOOR) || type.equals(Material.ACACIA_DOOR) || type.equals(Material.DARK_OAK_DOOR) || type.equals(Material.IRON_DOOR_BLOCK)) {
            if (type.equals(Material.BED_BLOCK)) {
               doubledata = data + 8;
               if (data == 0) {
                  dz = z + 1;
               } else if (data == 1) {
                  dx = x - 1;
               } else if (data == 2) {
                  dz = z - 1;
               } else if (data == 3) {
                  dx = x + 1;
               }
            } else if ((type.equals(Material.WOODEN_DOOR) || type.equals(Material.SPRUCE_DOOR) || type.equals(Material.BIRCH_DOOR) || type.equals(Material.JUNGLE_DOOR) || type.equals(Material.ACACIA_DOOR) || type.equals(Material.DARK_OAK_DOOR) || type.equals(Material.IRON_DOOR_BLOCK)) && data < 9) {
               dy = y + 1;
               doubledata = data + 8;
            }

            logdouble = true;
         }

         int internal_type = Functions.block_id(type.name(), true);
         int internal_doubletype = Functions.block_id(type.name(), true);
         if (replaced_type > 0 && !Functions.getType(replaced_type).equals(Material.AIR)) {
            Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, replaced_type, replaced_data, (List)null, 0, 0);
         }

         Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, internal_type, data, meta, 1, 0);
         if (logdouble) {
            Database.insertBlock(preparedStmt, time, userid, wid, dx, dy, dz, internal_doubletype, doubledata, (List)null, 1, 0);
         }
      } catch (Exception var25) {
         var25.printStackTrace();
      }

   }

   public static void log_player_kill(PreparedStatement preparedStmt, String user, BlockState block, String player) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int wid = Functions.getWorldId(block.getWorld().getName());
         int time = (int)(System.currentTimeMillis() / 1000L);
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int userid = Config.player_id_cache.get(user.toLowerCase());
         if (Config.player_id_cache.get(player.toLowerCase()) == null) {
            Database.loadUserID(preparedStmt.getConnection(), player, (String)null);
         }

         int playerid = Config.player_id_cache.get(player.toLowerCase());
         Database.insertBlock(preparedStmt, time, userid, wid, x, y, z, 0, playerid, null, 3, 0);
      } catch (Exception var11) {
         var11.printStackTrace();
      }

   }

   public static void log_session(PreparedStatement preparedStmt, String user, BlockState block, int time, int action) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         int wid = Functions.getWorldId(block.getWorld().getName());
         int userid = Config.player_id_cache.get(user.toLowerCase());
         Database.insertSession(preparedStmt, time, userid, wid, x, y, z, action);
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   public static void log_skull_break(PreparedStatement preparedStmt, PreparedStatement preparedStmt2, String user, BlockState block) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null || block == null) {
            return;
         }

         int time = (int)(System.currentTimeMillis() / 1000L);
         int type = Functions.block_id(block.getType().name(), true);
         Skull skull = (Skull)block;
         String skull_owner = "";
         int skull_type = Functions.getSkullType(skull.getSkullType());
         int skull_rotation = Functions.getBlockFace(skull.getRotation());
         if (skull.hasOwner()) {
            skull_owner = skull.getOwner();
         }

         int skull_data = Functions.getRawData(skull);
         Database.insertSkull(preparedStmt2, time, skull_type, skull_data, skull_rotation, skull_owner);
         ResultSet keys = preparedStmt2.getGeneratedKeys();
         keys.next();
         int skull_key = keys.getInt(1);
         keys.close();
         log_break(preparedStmt, user, block.getLocation(), type, skull_key, (List)null);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public static void log_skull_place(PreparedStatement preparedStmt, PreparedStatement preparedStmt2, String user, BlockState block, int replace_type, int replace_data) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null || block == null) {
            return;
         }

         int time = (int)(System.currentTimeMillis() / 1000L);
         Material type = block.getType();
         int skull_key = 0;
         if (block instanceof Skull) {
            Skull skull = (Skull)block;
            String skull_owner = "";
            int skull_type = Functions.getSkullType(skull.getSkullType());
            int skull_rotation = Functions.getBlockFace(skull.getRotation());
            if (skull.hasOwner()) {
               skull_owner = skull.getOwner();
            }

            int skull_data = Functions.getRawData(skull);
            Database.insertSkull(preparedStmt2, time, skull_type, skull_data, skull_rotation, skull_owner);
            ResultSet keys = preparedStmt2.getGeneratedKeys();
            keys.next();
            skull_key = keys.getInt(1);
            keys.close();
         }

         log_place(preparedStmt, user, block, replace_type, replace_data, type, skull_key, true, (List)null);
      } catch (Exception var15) {
         var15.printStackTrace();
      }

   }

   public static void log_username(Connection connection, String user, String uuid, int configUsernames, int time) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int id_row = -1;
         String user_row = null;
         String query = "SELECT rowid as id, user FROM " + Config.prefix + "user WHERE uuid = ? LIMIT 0, 1";
         PreparedStatement preparedStmt = connection.prepareStatement(query);
         preparedStmt.setString(1, uuid);

         ResultSet rs;
         for(rs = preparedStmt.executeQuery(); rs.next(); user_row = rs.getString("user").toLowerCase()) {
            id_row = rs.getInt("id");
         }

         rs.close();
         preparedStmt.close();
         boolean update = false;
         if (user_row == null) {
            id_row = Config.player_id_cache.get(user.toLowerCase());
            update = true;
         } else if (!user.equalsIgnoreCase(user_row)) {
            update = true;
         }

         if (update) {
            preparedStmt = connection.prepareStatement("UPDATE " + Config.prefix + "user SET user = ?, uuid = ? WHERE rowid = ?");
            preparedStmt.setString(1, user);
            preparedStmt.setString(2, uuid);
            preparedStmt.setInt(3, id_row);
            preparedStmt.executeUpdate();
            preparedStmt.close();
         } else {
            boolean foundUUID = false;
            query = "SELECT rowid as id FROM " + Config.prefix + "username_log WHERE uuid = ? AND user = ? LIMIT 0, 1";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, uuid);
            preparedStatement.setString(2, user);

            for(rs = preparedStatement.executeQuery(); rs.next(); foundUUID = true) {
            }

            rs.close();
            preparedStatement.close();
            if (!foundUUID) {
               update = true;
            }
         }

         if (update && configUsernames == 1) {
            preparedStmt = connection.prepareStatement("INSERT INTO " + Config.prefix + "username_log (time, uuid, user) VALUES (?, ?, ?)");
            preparedStmt.setInt(1, time);
            preparedStmt.setString(2, uuid);
            preparedStmt.setString(3, user);
            preparedStmt.executeUpdate();
            preparedStmt.close();
         }

         Config.player_id_cache.put(user.toLowerCase(), id_row);
         Config.player_id_cache_reversed.put(id_row, user);
         Config.uuid_cache.put(user.toLowerCase(), uuid);
         Config.uuid_cache_reversed.put(uuid, user);
      } catch (Exception var13) {
         var13.printStackTrace();
      }

   }

   public static void sign_text(PreparedStatement preparedStmt, String user, BlockState block, String line1, String line2, String line3, String line4, int time_offset) {
      try {
         if (Config.blacklist.get(user.toLowerCase()) != null) {
            return;
         }

         int userid = Config.player_id_cache.get(user.toLowerCase());
         int wid = Functions.getWorldId(block.getWorld().getName());
         int time = (int)(System.currentTimeMillis() / 1000L) - time_offset;
         int x = block.getX();
         int y = block.getY();
         int z = block.getZ();
         Database.insertSign(preparedStmt, time, userid, wid, x, y, z, line1, line2, line3, line4);
      } catch (Exception var14) {
         var14.printStackTrace();
      }

   }
}
