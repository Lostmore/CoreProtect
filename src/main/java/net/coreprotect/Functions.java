package net.coreprotect;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.Logger;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.BlockInfo;
import net.coreprotect.model.Config;
import net.coreprotect.worldedit.CoreProtectEditSessionEvent;
import org.bukkit.Art;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public class Functions extends Queue {
   private static Pattern csvSplitter = Pattern.compile(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

   public static int block_id(Material material) {
      if (material == null) {
         material = Material.AIR;
      }

      return block_id(material.name(), true);
   }

   public static int block_id(String name, boolean internal) {
      int id = -1;
      name = name.toLowerCase().trim();
      if (!name.contains(":")) {
         name = "minecraft:" + name;
      }

      if (Config.materials.get(name) != null) {
         id = Config.materials.get(name);
      } else if (internal) {
         int mid = Config.material_id + 1;
         Config.materials.put(name, mid);
         Config.materials_reversed.put(mid, name);
         Config.material_id = mid;
         Queue.queueMaterialInsert(mid, name);
         id = Config.materials.get(name);
      }

      return id;
   }

   public static String block_name_lookup(int id) {
      String name = "";
      if (Config.materials_reversed.get(id) != null) {
         name = Config.materials_reversed.get(id);
      } else if (BlockInfo.legacy_block_names.get(id) != null) {
         name = BlockInfo.legacy_block_names.get(id);
      }

      return name;
   }

   public static String block_name_short(int id) {
      String name = block_name_lookup(id);
      if (name.contains(":")) {
         String[] block_name_split = name.split(":");
         name = block_name_split[1];
      }

      return name;
   }

   public static int checkConfig(World world, String option) {
      int result = -1;
      if (Config.config.get(world.getName() + "-" + option) != null) {
         result = Config.config.get(world.getName() + "-" + option);
      } else if (Config.config.get(option) != null) {
         result = Config.config.get(option);
      }

      return result;
   }

   public static void combine_items(Material material, ItemStack[] items) {
      if (!material.equals(Material.ARMOR_STAND)) {
         try {
            int c1 = 0;
            ItemStack[] var3 = items;
            int var4 = items.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               ItemStack o1 = var3[var5];
               int c2 = 0;
               ItemStack[] var8 = items;
               int var9 = items.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  ItemStack o2 = var8[var10];
                  if (o1 != null && o2 != null && o1.isSimilar(o2) && c2 > c1) {
                     int namount = o1.getAmount() + o2.getAmount();
                     o1.setAmount(namount);
                     o2.setAmount(0);
                  }

                  ++c2;
               }

               ++c1;
            }
         } catch (Exception var13) {
            var13.printStackTrace();
         }

      }
   }

   public static Integer[] convertArray(String[] array) {
      List<Integer> list = new ArrayList();
      String[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String item = var2[var4];
         list.add(Integer.parseInt(item));
      }

      return list.toArray(new Integer[list.size()]);
   }

   public static byte[] convertByteData(Object data) {
      byte[] result = null;

      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(bos);
         oos.writeObject(data);
         oos.flush();
         oos.close();
         bos.close();
         result = bos.toByteArray();
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return result;
   }

   public static void createDatabaseTables(String prefix, boolean purge) {
      Config.databaseTables.clear();
      Config.databaseTables.addAll(Arrays.asList("art_map", "block", "chat", "command", "container", "entity", "entity_map", "material_map", "session", "sign", "skull", "user", "username_log", "version", "world"));
      if (Config.config.get("use-mysql") == 1) {
         boolean success = false;

         try {
            Connection connection = Database.getConnection(true);
            if (connection != null) {
               String index = "";
               Statement statement = connection.createStatement();
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "art_map(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),id int(8),art varchar(255)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(wid,x,z,time), INDEX(user,time), INDEX(type,time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "block(rowid int(10) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid), time int(10), user int(8), wid int(4), x int(8), y int(3), z int(8), type int(6), data int(8), meta blob, action int(2), rolled_back tinyint(1)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(time), INDEX(user,time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "chat(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10), user int(8), message varchar(255)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(time), INDEX(user,time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "command(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10), user int(8), message varchar(255)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(wid,x,z,time), INDEX(user,time), INDEX(type,time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "container(rowid int(10) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid), time int(10), user int(8), wid int(4), x int(8), y int(3), z int(8), type int(6), data int(6), amount int(4), metadata blob, action int(2), rolled_back tinyint(1)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "entity(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid), time int(10), data blob) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "entity_map(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),id int(8),entity varchar(255)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "material_map(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),id int(8),material varchar(255)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(wid,x,z,time), INDEX(action,time), INDEX(user,time), INDEX(time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "session(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10), user int(8), wid int(4), x int(8), y int (3), z int(8), action int(1)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(wid,x,z,y,time)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "sign(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10), user int(8), wid int(4), x int(8), y int(3), z int(8), line_1 varchar(100), line_2 varchar(100), line_3 varchar(100), line_4 varchar(100)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "skull(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid), time int(10), type int(2), data int(1), rotation int(2), owner varchar(16)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(user), INDEX(uuid)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "user(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10),user varchar(32),uuid varchar(64)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               index = ", INDEX(uuid,user)";
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "username_log(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10),uuid varchar(64),user varchar(32)" + index + ") ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "version(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),time int(10),version varchar(16)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "world(rowid int(8) NOT NULL AUTO_INCREMENT,PRIMARY KEY(rowid),id int(8),world varchar(255)) ENGINE=InnoDB DEFAULT CHARACTER SET utf8");
               statement.close();
               connection.close();
               success = true;
            }
         } catch (Exception var10) {
            var10.printStackTrace();
         }

         if (!success) {
            Config.config.put("use-mysql", 0);
         }
      }

      if (Config.config.get("use-mysql") == 0) {
         try {
            Connection connection = Database.getConnection(true);
            Statement statement = connection.createStatement();
            List<String> tableData = new ArrayList();
            List<String> indexData = new ArrayList();
            String query = "SELECT type,name FROM sqlite_master WHERE type='table' OR type='index';";
            ResultSet rs = statement.executeQuery(query);

            while(rs.next()) {
               String type = rs.getString("type");
               if (type.equalsIgnoreCase("table")) {
                  tableData.add(rs.getString("name"));
               } else if (type.equalsIgnoreCase("index")) {
                  indexData.add(rs.getString("name"));
               }
            }

            rs.close();
            if (purge) {
               query = "ATTACH DATABASE '" + Config.sqlite + ".tmp' AS tmp_db";
               PreparedStatement preparedStmt = connection.prepareStatement(query);
               preparedStmt.execute();
               preparedStmt.close();
            }

            if (!tableData.contains(prefix + "art_map")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "art_map (id INTEGER, art TEXT);");
            }

            if (!tableData.contains(prefix + "block")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "block (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, type INTEGER, data INTEGER, meta BLOB, action INTEGER, rolled_back INTEGER);");
            }

            if (!tableData.contains(prefix + "chat")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "chat (time INTEGER, user INTEGER, message TEXT);");
            }

            if (!tableData.contains(prefix + "command")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "command (time INTEGER, user INTEGER, message TEXT);");
            }

            if (!tableData.contains(prefix + "container")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "container (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, type INTEGER, data INTEGER, amount INTEGER, metadata BLOB, action INTEGER, rolled_back INTEGER);");
            }

            if (!tableData.contains(prefix + "entity")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "entity (id INTEGER PRIMARY KEY ASC, time INTEGER, data BLOB);");
            }

            if (!tableData.contains(prefix + "entity_map")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "entity_map (id INTEGER, entity TEXT);");
            }

            if (!tableData.contains(prefix + "material_map")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "material_map (id INTEGER, material TEXT);");
            }

            if (!tableData.contains(prefix + "session")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "session (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, action INTEGER);");
            }

            if (!tableData.contains(prefix + "sign")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "sign (time INTEGER, user INTEGER, wid INTEGER, x INTEGER, y INTEGER, z INTEGER, line_1 TEXT, line_2 TEXT, line_3 TEXT, line_4 TEXT);");
            }

            if (!tableData.contains(prefix + "skull")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "skull (id INTEGER PRIMARY KEY ASC, time INTEGER, type INTEGER, data INTEGER, rotation INTEGER, owner TEXT);");
            }

            if (!tableData.contains(prefix + "user")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "user (id INTEGER PRIMARY KEY ASC, time INTEGER, user TEXT, uuid TEXT);");
            }

            if (!tableData.contains(prefix + "username_log")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "username_log (id INTEGER PRIMARY KEY ASC, time INTEGER, uuid TEXT, user TEXT);");
            }

            if (!tableData.contains(prefix + "version")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "version (time INTEGER, version TEXT);");
            }

            if (!tableData.contains(prefix + "world")) {
               statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "world (id INTEGER, world TEXT);");
            }

            if (!purge) {
               try {
                  if (!indexData.contains("block_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS block_index ON " + prefix + "block(wid,x,z,time);");
                  }

                  if (!indexData.contains("block_user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS block_user_index ON " + prefix + "block(user,time);");
                  }

                  if (!indexData.contains("block_type_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS block_type_index ON " + prefix + "block(type,time);");
                  }

                  if (!indexData.contains("chat_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS chat_index ON " + prefix + "chat(time);");
                  }

                  if (!indexData.contains("chat_user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS chat_user_index ON " + prefix + "chat(user,time);");
                  }

                  if (!indexData.contains("command_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS command_index ON " + prefix + "command(time);");
                  }

                  if (!indexData.contains("command_user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS command_user_index ON " + prefix + "command(user,time);");
                  }

                  if (!indexData.contains("container_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS container_index ON " + prefix + "container(wid,x,z,time);");
                  }

                  if (!indexData.contains("container_user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS container_user_index ON " + prefix + "container(user,time);");
                  }

                  if (!indexData.contains("container_type_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS container_type_index ON " + prefix + "container(type,time);");
                  }

                  if (!indexData.contains("session_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS session_index ON " + prefix + "session(wid,x,z,time);");
                  }

                  if (!indexData.contains("session_action_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS session_action_index ON " + prefix + "session(action,time);");
                  }

                  if (!indexData.contains("session_user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS session_user_index ON " + prefix + "session(user,time);");
                  }

                  if (!indexData.contains("session_time_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS session_time_index ON " + prefix + "session(time);");
                  }

                  if (!indexData.contains("sign_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS sign_index ON " + prefix + "sign(wid,x,z,y,time);");
                  }

                  if (!indexData.contains("user_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS user_index ON " + prefix + "user(user);");
                  }

                  if (!indexData.contains("uuid_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS uuid_index ON " + prefix + "user(uuid);");
                  }

                  if (!indexData.contains("username_log_uuid_index")) {
                     statement.executeUpdate("CREATE INDEX IF NOT EXISTS username_log_uuid_index ON " + prefix + "username_log(uuid,user);");
                  }
               } catch (Exception var9) {
                  System.out.println("[CoreProtect] Unable to validate database structure.");
               }
            }

            statement.close();
            connection.close();
         } catch (Exception var11) {
            var11.printStackTrace();
         }
      }

   }

   public static ItemMeta deserializeItemMeta(Class<? extends ItemMeta> itemMetaClass, Map<String, Object> args) {
      DelegateDeserialization delegate = itemMetaClass.getAnnotation(DelegateDeserialization.class);
      return (ItemMeta)ConfigurationSerialization.deserializeObject(args, delegate.value());
   }

   public static <K, V extends Comparable<? super V>> SortedSet<Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
      SortedSet<Entry<K, V>> sortedEntries = new TreeSet(new Comparator<Entry<K, V>>() {
         public int compare(Entry<K, V> e1, Entry<K, V> e2) {
            int res = (e1.getValue()).compareTo(e2.getValue());
            return res != 0 ? res : 1;
         }
      });
      sortedEntries.addAll(map.entrySet());
      return sortedEntries;
   }

   public static Block fallingSand(Block b, BlockState bs, String player) {
      Block bl = b;
      int timestamp = (int)(System.currentTimeMillis() / 1000L);
      Material type = b.getType();
      if (bs != null) {
         type = bs.getType();
      }

      int x = b.getX();
      int y = b.getY();
      int z = b.getZ();
      World world = b.getWorld();
      int wid = getWorldId(world.getName());
      int yc = y - 1;
      if (BlockInfo.falling_block_types.contains(type)) {
         boolean bottomfound = false;

         while(true) {
            while(!bottomfound) {
               if (yc < 0) {
                  bl = world.getBlockAt(x, yc + 1, z);
                  bottomfound = true;
               } else {
                  Block block_down = world.getBlockAt(x, yc, z);
                  Material down = block_down.getType();
                  if (!down.equals(Material.AIR) && !down.equals(Material.WATER) && !down.equals(Material.STATIONARY_WATER) && !down.equals(Material.LAVA) && !down.equals(Material.STATIONARY_LAVA) && !down.equals(Material.SNOW)) {
                     bl = world.getBlockAt(x, yc + 1, z);
                     bottomfound = true;
                  } else {
                     String cords = "" + x + "." + yc + "." + z + "." + wid + "";
                     Object[] data = (Object[])Config.lookup_cache.get(cords);
                     if (data != null) {
                        Material t = (Material)data[2];
                        if (type.equals(t)) {
                           bl = world.getBlockAt(x, yc + 1, z);
                           bottomfound = true;
                        }
                     }
                  }

                  --yc;
               }
            }

            Config.lookup_cache.put("" + x + "." + bl.getY() + "." + z + "." + wid + "", new Object[]{timestamp, player, type});
            break;
         }
      }

      return bl;
   }

   public static ItemStack[] get_container_state(ItemStack[] array) {
      ItemStack[] result = array.clone();
      int c = 0;
      ItemStack[] var3 = array;
      int var4 = array.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         ItemStack i = var3[var5];
         ItemStack clone = null;
         if (i != null) {
            clone = i.clone();
         }

         result[c] = clone;
         ++c;
      }

      return result;
   }

   public static int getArtId(String name, boolean internal) {
      int id = -1;
      name = name.toLowerCase().trim();
      if (Config.art.get(name) != null) {
         id = Config.art.get(name);
      } else if (internal) {
         int artID = Config.art_id + 1;
         Config.art.put(name, artID);
         Config.art_reversed.put(artID, name);
         Config.art_id = artID;
         Queue.queueArtInsert(artID, name);
         id = Config.art.get(name);
      }

      return id;
   }

   public static String getArtName(int id) {
      String artname = "";
      if (Config.art_reversed.get(id) != null) {
         artname = Config.art_reversed.get(id);
      }

      return artname;
   }

   public static int getBlockFace(BlockFace rotation) {
      switch(rotation) {
      case NORTH:
         return 0;
      case NORTH_NORTH_EAST:
         return 1;
      case NORTH_EAST:
         return 2;
      case EAST_NORTH_EAST:
         return 3;
      case EAST:
         return 4;
      case EAST_SOUTH_EAST:
         return 5;
      case SOUTH_EAST:
         return 6;
      case SOUTH_SOUTH_EAST:
         return 7;
      case SOUTH:
         return 8;
      case SOUTH_SOUTH_WEST:
         return 9;
      case SOUTH_WEST:
         return 10;
      case WEST_SOUTH_WEST:
         return 11;
      case WEST:
         return 12;
      case WEST_NORTH_WEST:
         return 13;
      case NORTH_WEST:
         return 14;
      case NORTH_NORTH_WEST:
         return 15;
      default:
         throw new IllegalArgumentException("Invalid BlockFace rotation: " + rotation);
      }
   }

   public static BlockFace getBlockFace(int rotation) {
      switch(rotation) {
      case 0:
         return BlockFace.NORTH;
      case 1:
         return BlockFace.NORTH_NORTH_EAST;
      case 2:
         return BlockFace.NORTH_EAST;
      case 3:
         return BlockFace.EAST_NORTH_EAST;
      case 4:
         return BlockFace.EAST;
      case 5:
         return BlockFace.EAST_SOUTH_EAST;
      case 6:
         return BlockFace.SOUTH_EAST;
      case 7:
         return BlockFace.SOUTH_SOUTH_EAST;
      case 8:
         return BlockFace.SOUTH;
      case 9:
         return BlockFace.SOUTH_SOUTH_WEST;
      case 10:
         return BlockFace.SOUTH_WEST;
      case 11:
         return BlockFace.WEST_SOUTH_WEST;
      case 12:
         return BlockFace.WEST;
      case 13:
         return BlockFace.WEST_NORTH_WEST;
      case 14:
         return BlockFace.NORTH_WEST;
      case 15:
         return BlockFace.NORTH_NORTH_WEST;
      default:
         throw new AssertionError(rotation);
      }
   }

   public static ItemStack[] getContainerContents(Material type, Object container, Location location) {
      ItemStack[] contents = null;
      if (checkConfig(location.getWorld(), "item-transactions") == 1 && BlockInfo.containers.contains(type)) {
         try {
            if (type.equals(Material.ARMOR_STAND)) {
               LivingEntity entity = (LivingEntity)container;
               EntityEquipment equipment = getEntityEquipment(entity);
               if (equipment != null) {
                  contents = equipment.getArmorContents();
               }
            } else {
               Block block = (Block)container;
               Inventory inventory = getContainerInventory(block.getState(), true);
               if (inventory != null) {
                  contents = inventory.getContents();
               }
            }

            if (contents != null) {
               contents = get_container_state(contents);
            }
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }

      return contents;
   }

   public static Inventory getContainerInventory(BlockState block_state, boolean singleBlock) {
      Inventory inventory = null;

      try {
         if (block_state instanceof InventoryHolder) {
            if (singleBlock) {
               List<Material> chests = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST);
               Material block_type = block_state.getType();
               if (chests.contains(block_type)) {
                  inventory = ((Chest)block_state).getBlockInventory();
               }
            }

            if (inventory == null) {
               inventory = ((InventoryHolder)block_state).getInventory();
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return inventory;
   }

   public static byte getData(Block block) {
      return block.getData();
   }

   public static byte getData(BlockState block) {
      return block.getData().getData();
   }

   public static EntityEquipment getEntityEquipment(LivingEntity entity) {
      EntityEquipment equipment = null;

      try {
         equipment = entity.getEquipment();
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return equipment;
   }

   public static int getEntityId(EntityType type) {
      return getEntityId(type.name(), true);
   }

   public static int getEntityId(String name, boolean internal) {
      int id = -1;
      name = name.toLowerCase().trim();
      if (Config.entities.get(name) != null) {
         id = Config.entities.get(name);
      } else if (internal) {
         int entityID = Config.entity_id + 1;
         Config.entities.put(name, entityID);
         Config.entities_reversed.put(entityID, name);
         Config.entity_id = entityID;
         Queue.queueEntityInsert(entityID, name);
         id = Config.entities.get(name);
      }

      return id;
   }

   public static Material getEntityMaterial(EntityType type) {
      switch(type) {
      case ARMOR_STAND:
         return Material.ARMOR_STAND;
      case ENDER_CRYSTAL:
         return Material.END_CRYSTAL;
      default:
         return null;
      }
   }

   public static String getEntityName(int id) {
      String entityName = "";
      if (Config.entities_reversed.get(id) != null) {
         entityName = Config.entities_reversed.get(id);
      }

      return entityName;
   }

   public static EntityType getEntityType(int id) {
      EntityType entitytype = null;
      if (Config.entities_reversed.get(id) != null) {
         String name = Config.entities_reversed.get(id);
         if (name.contains("minecraft:")) {
            String[] block_name_split = name.split(":");
            name = block_name_split[1];
         }

         entitytype = EntityType.valueOf(name.toUpperCase());
      }

      return entitytype;
   }

   public static EntityType getEntityType(String name) {
      EntityType type = null;
      name = name.toLowerCase().trim();
      if (name.contains("minecraft:")) {
         name = name.split(":")[1];
      }

      if (Config.entities.get(name) != null) {
         type = EntityType.valueOf(name.toUpperCase());
      }

      return type;
   }

   public static int getHangingDelay(Map<String, Integer> hanging_delay, int row_wid, int row_x, int row_y, int row_z) {
      String token = row_wid + "." + row_x + "." + row_y + "." + row_z;
      int delay = 0;
      if (hanging_delay.get(token) != null) {
         delay = hanging_delay.get(token) + 1;
      }

      hanging_delay.put(token, delay);
      return delay;
   }

   public static Material getMaterialFromId(Integer id) {
      return Material.getMaterial(id);
   }

   public static int getMaterialId(Material material) {
      return block_id(material.name(), true);
   }

   public static byte getRawData(BlockState block) {
      return block.getRawData();
   }

   public static SkullType getSkullType(int type) {
      switch(type) {
      case 0:
         return SkullType.SKELETON;
      case 1:
         return SkullType.WITHER;
      case 2:
         return SkullType.ZOMBIE;
      case 3:
         return SkullType.PLAYER;
      case 4:
         return SkullType.CREEPER;
      case 5:
         return SkullType.DRAGON;
      default:
         return SkullType.SKELETON;
      }
   }

   public static int getSkullType(SkullType type) {
      switch(type) {
      case SKELETON:
         return 0;
      case WITHER:
         return 1;
      case ZOMBIE:
         return 2;
      case PLAYER:
         return 3;
      case CREEPER:
         return 4;
      case DRAGON:
         return 5;
      default:
         return 0;
      }
   }

   public static int getSpawnerType(EntityType type) {
      switch(type) {
      case ZOMBIE:
         return 1;
      case SKELETON:
         return 2;
      case SPIDER:
         return 3;
      case CAVE_SPIDER:
         return 4;
      case SILVERFISH:
         return 5;
      case BLAZE:
         return 6;
      default:
         return 0;
      }
   }

   public static EntityType getSpawnerType(int type) {
      switch(type) {
      case 1:
         return EntityType.ZOMBIE;
      case 2:
         return EntityType.SKELETON;
      case 3:
         return EntityType.SPIDER;
      case 4:
         return EntityType.CAVE_SPIDER;
      case 5:
         return EntityType.SILVERFISH;
      case 6:
         return EntityType.BLAZE;
      default:
         return EntityType.PIG;
      }
   }

   public static Material getType(Block block) {
      return block.getType();
   }

   public static Material getType(int id) {
      Material material = null;
      if (Config.materials_reversed.get(id) != null && id > 0) {
         String name = Config.materials_reversed.get(id);
         if (name.contains("minecraft:")) {
            String[] block_name_split = name.split(":");
            name = block_name_split[1];
         }

         material = Material.getMaterial(name.toUpperCase());
      }

      return material;
   }

   public static Material getType(String name) {
      Material material;
      name = name.toLowerCase().trim();
      if (!name.contains(":")) {
         name = "minecraft:" + name;
      }

      if (BlockInfo.legacy_block_ids.get(name) != null) {
         int legacy_id = BlockInfo.legacy_block_ids.get(name);
         material = Material.getMaterial(legacy_id);
      } else {
         if (name.contains("minecraft:")) {
            name = name.split(":")[1];
         }

         name = name.toUpperCase();
         material = Material.getMaterial(name);
         if (material == null) {
            List<String> stone_map = Arrays.asList("granite", "polished_granite", "diorite", "polished_diorite", "andesite", "polished_andesite");
            if (stone_map.contains(name.toLowerCase())) {
               material = Material.getMaterial("STONE");
            }
         }
      }

      return material;
   }

   public static boolean solidBlock(Material type) {
      return type.isSolid();
   }

   public static int getWorldId(String name) {
      int id = -1;

      try {
         if (Config.worlds.get(name) == null) {
            int wid = Config.world_id + 1;
            Config.worlds.put(name, wid);
            Config.worlds_reversed.put(wid, name);
            Config.world_id = wid;
            Queue.queueWorldInsert(wid, name);
         }

         id = Config.worlds.get(name);
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return id;
   }

   public static String getWorldName(int id) {
      String name = "";

      try {
         if (Config.worlds_reversed.get(id) != null) {
            name = Config.worlds_reversed.get(id);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return name;
   }

   public static void iceBreakCheck(BlockState block, String user, Material type) {
      if (type.equals(Material.ICE)) {
         int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
         int wid = getWorldId(block.getWorld().getName());
         Config.lookup_cache.put("" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "", new Object[]{unixtimestamp, user, Material.WATER});
      }

   }

   public static boolean listContains(List<Material> list, Material value) {
      boolean result = false;
      Iterator var3 = list.iterator();

      while(var3.hasNext()) {
         Material list_value = (Material)var3.next();
         if (list_value.equals(value)) {
            result = true;
            break;
         }
      }

      return result;
   }

   public static void loadWorldEdit() {
      try {
         boolean validVersion = true;
         CoreProtect plugin = CoreProtect.getInstance();
         String version = plugin.getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion();
         if (version.contains(".")) {
            String[] version_split = version.replaceAll("[^0-9.]", "").split("\\.");
            double value = Double.parseDouble(version_split[0] + "." + version_split[1]);
            if (value > 0.0D && value < 6.0D) {
               validVersion = false;
            }
         } else if (version.contains("-")) {
            int value = Integer.parseInt(version.split("-")[0].replaceAll("[^0-9]", ""));
            if (value > 0 && value < 3122) {
               validVersion = false;
            }
         }

         if (validVersion) {
            CoreProtectEditSessionEvent.register();
         } else {
            System.out.println("[CoreProtect] Invalid WorldEdit version found.");
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public static int matchWorld(String name) {
      int id = -1;

      try {
         String result = "";
         name = name.replaceFirst("#", "").toLowerCase().trim();
         Iterator var3 = CoreProtect.getInstance().getServer().getWorlds().iterator();

         while(var3.hasNext()) {
            World world = (World)var3.next();
            String world_name = world.getName();
            if (world_name.toLowerCase().equals(name)) {
               result = world.getName();
               break;
            }

            if (world_name.toLowerCase().endsWith(name)) {
               result = world.getName();
            } else if (world_name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").endsWith(name)) {
               result = world.getName();
            }
         }

         if (result.length() > 0) {
            id = getWorldId(result);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return id;
   }

   public static void messageOwner(String string) {
      Iterator var1;
      Player player;
      if (string.startsWith("-")) {
         CoreProtect.getInstance().getServer().getConsoleSender().sendMessage(string);
         var1 = CoreProtect.getInstance().getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            if (player.isOp()) {
               player.sendMessage(string);
            }
         }
      } else {
         CoreProtect.getInstance().getServer().getConsoleSender().sendMessage("[CoreProtect] " + string);
         var1 = CoreProtect.getInstance().getServer().getOnlinePlayers().iterator();

         while(var1.hasNext()) {
            player = (Player)var1.next();
            if (player.isOp()) {
               player.sendMessage("§3CoreProtect §f- " + string);
            }
         }
      }

   }

   public static void messageOwnerAndUser(CommandSender user, String string) {
      CoreProtect.getInstance().getServer().getConsoleSender().sendMessage("[CoreProtect] " + string);
      Iterator var2 = CoreProtect.getInstance().getServer().getOnlinePlayers().iterator();

      while(var2.hasNext()) {
         Player player = (Player)var2.next();
         if (player.isOp() && !player.getName().equals(user.getName())) {
            player.sendMessage("§3CoreProtect §f- " + string);
         }
      }

      if (user instanceof Player && ((Player)user).isOnline()) {
         user.sendMessage("§3CoreProtect §f- " + string);
      }

   }

   public static String nameFilter(String name, int data) {
      if (name.equals("stone")) {
         switch(data) {
         case 1:
            name = "granite";
            break;
         case 2:
            name = "polished_granite";
            break;
         case 3:
            name = "diorite";
            break;
         case 4:
            name = "polished_diorite";
            break;
         case 5:
            name = "andesite";
            break;
         case 6:
            name = "polished_andesite";
         }
      }

      return name;
   }

   public static ItemStack newItemStack1(int type, int amount) {
      return new ItemStack(type, amount);
   }

   public static ItemStack newItemStack1(int type, int amount, short data) {
      return new ItemStack(type, amount, data);
   }

   public static boolean newVersion(Integer[] old_version, Integer[] current_version) {
      boolean result = false;
      if (old_version[0] < current_version[0]) {
         result = true;
      } else if (old_version[0] == current_version[0] && old_version[1] < current_version[1]) {
         result = true;
      } else if (old_version.length >= 3 && old_version[0] == current_version[0] && old_version[1] == current_version[1] && old_version[2] < current_version[2]) {
         result = true;
      }

      return result;
   }

   public static boolean newVersion(Integer[] oldVersion, String currentVersion) {
      String[] currentVersionSplit = currentVersion.split("\\.");
      return newVersion(oldVersion, convertArray(currentVersionSplit));
   }

   public static boolean newVersion(String oldVersion, Integer[] currentVersion) {
      String[] oldVersionSplit = oldVersion.split("\\.");
      return newVersion(convertArray(oldVersionSplit), currentVersion);
   }

   public static boolean newVersion(String oldVersion, String currentVersion) {
      String[] oldVersionSplit = oldVersion.split("\\.");
      String[] currentVersionSplit = currentVersion.split("\\.");
      return newVersion(convertArray(oldVersionSplit), convertArray(currentVersionSplit));
   }

   public static String[] parseCSVString(String string) {
      String[] result = null;
      if (string.indexOf("\"") > -1) {
         result = csvSplitter.split(string, -1);
      } else {
         result = string.split(",", -1);
      }

      for(int i = 0; i < result.length; ++i) {
         String value = result[i];
         if (value.length() == 0) {
            value = null;
         } else if (string.indexOf("\"") > -1) {
            value = value.replaceAll("^\"|\"$", "");
            value = value.replaceAll("\"\"", "\"");
         }

         result[i] = value;
      }

      return result;
   }

   public static List<Object> processMeta(BlockState block) {
      ArrayList meta = new ArrayList();

      try {
         if (block instanceof CommandBlock) {
            CommandBlock command_block = (CommandBlock)block;
            String command = command_block.getCommand();
            if (command.length() > 0) {
               meta.add(command);
            }
         } else if (block instanceof Banner) {
            Banner banner = (Banner)block;
            meta.add(banner.getBaseColor());
            List<org.bukkit.block.banner.Pattern> patterns = banner.getPatterns();
            Iterator var4 = patterns.iterator();

            while(var4.hasNext()) {
               org.bukkit.block.banner.Pattern pattern = (org.bukkit.block.banner.Pattern)var4.next();
               meta.add(pattern.serialize());
            }
         } else if (block instanceof ShulkerBox) {
            ShulkerBox shulkerBox = (ShulkerBox)block;
            ItemStack[] inventory = shulkerBox.getInventory().getStorageContents();
            int slot = 0;
            ItemStack[] var18 = inventory;
            int var6 = inventory.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               ItemStack itemStack = var18[var7];
               if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                  Map<Integer, Object> itemMap = new HashMap();
                  ItemStack item = itemStack.clone();
                  List<List<Map<String, Object>>> metadata = Logger.getItemMeta(item, item.getType(), slot);
                  item.setItemMeta((ItemMeta)null);
                  itemMap.put(0, item.serialize());
                  itemMap.put(1, metadata);
                  meta.add(itemMap);
               }

               ++slot;
            }
         }
      } catch (Exception var12) {
         var12.printStackTrace();
      }

      if (meta.size() == 0) {
         meta = null;
      }

      return meta;
   }

   public static void removeHanging(final BlockState block, int delay) {
      CoreProtect.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(CoreProtect.getInstance(), new Runnable() {
         public void run() {
            try {
               Entity[] var1 = block.getChunk().getEntities();
               int var2 = var1.length;

               for(int var3 = 0; var3 < var2; ++var3) {
                  Entity e = var1[var3];
                  if (e instanceof ItemFrame || e instanceof Painting) {
                     Location el = e.getLocation();
                     if (el.getBlockX() == block.getX() && el.getBlockY() == block.getY() && el.getBlockZ() == block.getZ()) {
                        e.remove();
                     }
                  }
               }
            } catch (Exception var6) {
               var6.printStackTrace();
            }

         }
      }, (long)delay);
   }

   public static void sendBlockChange(Player player, Location location, Material type, byte data) {
      player.sendBlockChange(location, type, data);
   }

   public static void setData(Block block, byte data) {
      block.setData(data);
   }

   public static BlockState setRawData(BlockState block, byte data) {
      block.setRawData(data);
      return block;
   }

   public static void setTypeAndData(Block block, Material type, byte data, boolean update) {
      block.setType(type, update);
      block.setData(data);
   }

   public static void setTypeId1(Block block, int type) {
      block.setTypeId(type);
   }

   public static void setTypeId1(Block block, int type, boolean update) {
      block.setTypeId(type, update);
   }

   public static void setTypeIdAndData1(Block block, int type, byte data, boolean update) {
      block.setTypeIdAndData(type, data, update);
   }

   public static void spawnEntity(final BlockState block, final EntityType type, final List<Object> list) {
      CoreProtect.getInstance().getServer().getScheduler().runTask(CoreProtect.getInstance(), new Runnable() {
         public void run() {
            try {
               Location location = block.getLocation();
               location.setX(location.getX() + 0.5D);
               location.setZ(location.getZ() + 0.5D);
               Entity entity = block.getLocation().getWorld().spawnEntity(location, type);
               if (list.size() == 0) {
                  return;
               }

               List<Object> age = (List)list.get(0);
               List<Object> tame = (List)list.get(1);
               List<Object> data = (List)list.get(2);
               if (list.size() >= 5) {
                  entity.setCustomNameVisible((Boolean)list.get(3));
                  entity.setCustomName((String)list.get(4));
               }

               int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
               int wid = Functions.getWorldId(block.getWorld().getName());
               String token = "" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "." + type.name() + "";
               Config.entity_cache.put(token, new Object[]{unixtimestamp, entity.getEntityId()});
               int count;
               Iterator var11;
               Object value;
               int setxx;
               boolean setxxxxxxxxxxxxxxx;
               double set;
               if (entity instanceof Ageable) {
                  count = 0;
                  Ageable ageable = (Ageable)entity;

                  for(var11 = age.iterator(); var11.hasNext(); ++count) {
                     value = var11.next();
                     if (count == 0) {
                        setxx = (Integer)value;
                        ageable.setAge(setxx);
                     } else if (count == 1) {
                        setxxxxxxxxxxxxxxx = (Boolean)value;
                        ageable.setAgeLock(setxxxxxxxxxxxxxxx);
                     } else if (count == 2) {
                        setxxxxxxxxxxxxxxx = (Boolean)value;
                        if (setxxxxxxxxxxxxxxx) {
                           ageable.setAdult();
                        } else {
                           ageable.setBaby();
                        }
                     } else if (count == 3) {
                        setxxxxxxxxxxxxxxx = (Boolean)value;
                        ageable.setBreed(setxxxxxxxxxxxxxxx);
                     } else if (count == 4 && value != null) {
                        set = (Double)value;
                        ageable.setMaxHealth(set);
                     }
                  }
               }

               if (entity instanceof Tameable) {
                  count = 0;
                  Tameable tameable = (Tameable)entity;

                  for(var11 = tame.iterator(); var11.hasNext(); ++count) {
                     value = var11.next();
                     if (count == 0) {
                        setxxxxxxxxxxxxxxx = (Boolean)value;
                        tameable.setTamed(setxxxxxxxxxxxxxxx);
                     } else if (count == 1) {
                        String setx = (String)value;
                        if (setx.length() > 0) {
                           Player owner = CoreProtect.getInstance().getServer().getPlayer(setx);
                           if (owner == null) {
                              OfflinePlayer offline_player = CoreProtect.getInstance().getServer().getOfflinePlayer(setx);
                              if (offline_player != null) {
                                 tameable.setOwner(offline_player);
                              }
                           } else {
                              tameable.setOwner(owner);
                           }
                        }
                     }
                  }
               }

               if (entity instanceof Attributable && list.size() >= 6) {
                  Attributable attributable = (Attributable)entity;
                  List<Object> attributes = (List)list.get(5);
                  var11 = attributes.iterator();

                  label293:
                  while(true) {
                     List attributeModifiers;
                     AttributeInstance entityAttribute;
                     Double baseValue;
                     do {
                        if (!var11.hasNext()) {
                           break label293;
                        }

                        value = var11.next();
                        List<Object> attributeData = (List)value;
                        Attribute attribute = (Attribute)attributeData.get(0);
                        baseValue = (Double)attributeData.get(1);
                        attributeModifiers = (List)attributeData.get(2);
                        entityAttribute = attributable.getAttribute(attribute);
                     } while(entityAttribute == null);

                     entityAttribute.setBaseValue(baseValue);
                     Iterator var18 = entityAttribute.getModifiers().iterator();

                     while(var18.hasNext()) {
                        AttributeModifier modifier = (AttributeModifier)var18.next();
                        entityAttribute.removeModifier(modifier);
                     }

                     var18 = attributeModifiers.iterator();

                     while(var18.hasNext()) {
                        Object modifierx = var18.next();
                        Map<String, Object> serializedModifier = (Map)modifierx;
                        entityAttribute.addModifier(AttributeModifier.deserialize(serializedModifier));
                     }
                  }
               }

               count = 0;

               for(Iterator var36 = data.iterator(); var36.hasNext(); ++count) {
                  Object valuex = var36.next();
                  if (entity instanceof Creeper) {
                     Creeper creeper = (Creeper)entity;
                     if (count == 0) {
                        setxxxxxxxxxxxxxxx = (Boolean)valuex;
                        creeper.setPowered(setxxxxxxxxxxxxxxx);
                     }
                  } else if (entity instanceof Enderman) {
                     Enderman enderman = (Enderman)entity;
                     if (count == 0) {
                        Map<String, Object> setxxxxxxxxxxxxxx = (Map)valuex;
                        MaterialData materialdata = ItemStack.deserialize(setxxxxxxxxxxxxxx).getData();
                        enderman.setCarriedMaterial(materialdata);
                     }
                  } else if (entity instanceof IronGolem) {
                     IronGolem irongolem = (IronGolem)entity;
                     if (count == 0) {
                        setxxxxxxxxxxxxxxx = (Boolean)valuex;
                        irongolem.setPlayerCreated(setxxxxxxxxxxxxxxx);
                     }
                  } else if (entity instanceof Ocelot) {
                     Ocelot ocelot = (Ocelot)entity;
                     if (count == 0) {
                        Type setxxxxxxxxxxxxx = (Type)valuex;
                        ocelot.setCatType(setxxxxxxxxxxxxx);
                     } else if (count == 1) {
                        setxxxxxxxxxxxxxxx = (Boolean)valuex;
                        ocelot.setSitting(setxxxxxxxxxxxxxxx);
                     }
                  } else if (entity instanceof Pig) {
                     Pig pig = (Pig)entity;
                     if (count == 0) {
                        setxxxxxxxxxxxxxxx = (Boolean)valuex;
                        pig.setSaddle(setxxxxxxxxxxxxxxx);
                     }
                  } else {
                     DyeColor setxxxxxxxxxxxx;
                     if (entity instanceof Sheep) {
                        Sheep sheep = (Sheep)entity;
                        if (count == 0) {
                           setxxxxxxxxxxxxxxx = (Boolean)valuex;
                           sheep.setSheared(setxxxxxxxxxxxxxxx);
                        } else if (count == 1) {
                           setxxxxxxxxxxxx = (DyeColor)valuex;
                           sheep.setColor(setxxxxxxxxxxxx);
                        }
                     } else if (entity instanceof Slime) {
                        Slime slime = (Slime)entity;
                        if (count == 0) {
                           setxx = (Integer)valuex;
                           slime.setSize(setxx);
                        }
                     } else {
                        Profession setxxxxxxxxxxx;
                        if (entity instanceof Villager) {
                           Villager villager = (Villager)entity;
                           if (count == 0) {
                              setxxxxxxxxxxx = (Profession)valuex;
                              villager.setProfession(setxxxxxxxxxxx);
                           } else if (count == 1) {
                              setxx = (Integer)valuex;
                              villager.setRiches(setxx);
                           } else if (count == 2) {
                              List<MerchantRecipe> merchantRecipes = new ArrayList();
                              List<Object> setxxxxxxxxxx = (List)valuex;
                              Iterator var55 = setxxxxxxxxxx.iterator();

                              while(var55.hasNext()) {
                                 Object recipes = var55.next();
                                 List<Object> recipe = (List)recipes;
                                 List<Object> itemMap = (List)recipe.get(0);
                                 ItemStack result = ItemStack.deserialize((Map)itemMap.get(0));
                                 List<List<Map<String, Object>>> metadata = (List)itemMap.get(1);
                                 Object[] populatedStack = Lookup.populateItemStack(result, metadata);
                                 result = (ItemStack)populatedStack[1];
                                 int uses = (Integer)recipe.get(1);
                                 int maxUses = (Integer)recipe.get(2);
                                 boolean experienceReward = (Boolean)recipe.get(3);
                                 List<ItemStack> merchantIngredients = new ArrayList();
                                 List<Object> ingredients = (List)recipe.get(4);
                                 Iterator var27 = ingredients.iterator();

                                 while(var27.hasNext()) {
                                    Object ingredient = var27.next();
                                    List<Object> ingredientMap = (List)ingredient;
                                    ItemStack item = ItemStack.deserialize((Map)ingredientMap.get(0));
                                    List<List<Map<String, Object>>> itemMetaData = (List)ingredientMap.get(1);
                                    populatedStack = Lookup.populateItemStack(item, itemMetaData);
                                    item = (ItemStack)populatedStack[1];
                                    merchantIngredients.add(item);
                                 }

                                 MerchantRecipe merchantRecipe = new MerchantRecipe(result, uses, maxUses, experienceReward);
                                 merchantRecipe.setIngredients(merchantIngredients);
                                 merchantRecipes.add(merchantRecipe);
                              }

                              if (merchantRecipes.size() > 0) {
                                 villager.setRecipes(merchantRecipes);
                              }
                           }
                        } else if (entity instanceof Wolf) {
                           Wolf wolf = (Wolf)entity;
                           if (count == 0) {
                              setxxxxxxxxxxxxxxx = (Boolean)valuex;
                              wolf.setSitting(setxxxxxxxxxxxxxxx);
                           } else if (count == 1) {
                              setxxxxxxxxxxxx = (DyeColor)valuex;
                              wolf.setCollarColor(setxxxxxxxxxxxx);
                           }
                        } else if (entity instanceof ZombieVillager) {
                           ZombieVillager zombieVillager = (ZombieVillager)entity;
                           if (count == 0) {
                              setxxxxxxxxxxxxxxx = (Boolean)valuex;
                              zombieVillager.setBaby(setxxxxxxxxxxxxxxx);
                           } else if (count == 1) {
                              setxxxxxxxxxxx = (Profession)valuex;
                              zombieVillager.setVillagerProfession(setxxxxxxxxxxx);
                           }
                        } else if (entity instanceof Zombie) {
                           Zombie zombie = (Zombie)entity;
                           if (count == 0) {
                              setxxxxxxxxxxxxxxx = (Boolean)valuex;
                              zombie.setBaby(setxxxxxxxxxxxxxxx);
                           }
                        } else if (entity instanceof AbstractHorse) {
                           AbstractHorse abstractHorse = (AbstractHorse)entity;
                           if (count == 0 && valuex != null) {
                              setxxxxxxxxxxxxxxx = (Boolean)valuex;
                              if (entity instanceof ChestedHorse) {
                                 ChestedHorse chestedHorse = (ChestedHorse)entity;
                                 chestedHorse.setCarryingChest(setxxxxxxxxxxxxxxx);
                              }
                           } else {
                              Horse horse;
                              if (count == 1 && valuex != null) {
                                 Color setxxxxxxx = (Color)valuex;
                                 if (entity instanceof Horse) {
                                    horse = (Horse)entity;
                                    horse.setColor(setxxxxxxx);
                                 }
                              } else if (count == 2) {
                                 setxx = (Integer)valuex;
                                 abstractHorse.setDomestication(setxx);
                              } else if (count == 3) {
                                 set = (Double)valuex;
                                 abstractHorse.setJumpStrength(set);
                              } else if (count == 4) {
                                 setxx = (Integer)valuex;
                                 abstractHorse.setMaxDomestication(setxx);
                              } else if (count == 5 && valuex != null) {
                                 Style setxxxxx = (Style)valuex;
                                 horse = (Horse)entity;
                                 horse.setStyle(setxxxxx);
                              }
                           }

                           ItemStack setxxx;
                           if (entity instanceof Horse) {
                              Horse horsex = (Horse)entity;
                              if (count == 7) {
                                 if (valuex != null) {
                                    setxxx = ItemStack.deserialize((Map)valuex);
                                    horsex.getInventory().setArmor(setxxx);
                                 }
                              } else if (count == 8) {
                                 if (valuex != null) {
                                    setxxx = ItemStack.deserialize((Map)valuex);
                                    horsex.getInventory().setSaddle(setxxx);
                                 }
                              } else if (count == 9) {
                                 Color setxxxx = (Color)valuex;
                                 horsex.setColor(setxxxx);
                              } else if (count == 10) {
                                 Style setxxxxxx = (Style)valuex;
                                 horsex.setStyle(setxxxxxx);
                              }
                           } else if (entity instanceof ChestedHorse) {
                              if (count == 7) {
                                 ChestedHorse chestedHorsex = (ChestedHorse)entity;
                                 boolean setxxxxxxxx = (Boolean)valuex;
                                 chestedHorsex.setCarryingChest(setxxxxxxxx);
                              }

                              if (entity instanceof Llama) {
                                 Llama llama = (Llama)entity;
                                 if (count == 8) {
                                    if (valuex != null) {
                                       setxxx = ItemStack.deserialize((Map)valuex);
                                       llama.getInventory().setDecor(setxxx);
                                    }
                                 } else if (count == 9) {
                                    org.bukkit.entity.Llama.Color setxxxxxxxxx = (org.bukkit.entity.Llama.Color)valuex;
                                    llama.setColor(setxxxxxxxxx);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } catch (Exception var32) {
               var32.printStackTrace();
            }

         }
      });
   }

   public static void spawnHanging(final BlockState blockstate, final Material row_type, final int row_data, int delay) {
      CoreProtect.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(CoreProtect.getInstance(), new Runnable() {
         public void run() {
            try {
               Block block = blockstate.getBlock();
               int row_x = block.getX();
               int row_y = block.getY();
               int row_z = block.getZ();
               Entity[] var5 = block.getChunk().getEntities();
               int dx2 = var5.length;

               int dz1;
               for(dz1 = 0; dz1 < dx2; ++dz1) {
                  Entity e = var5[dz1];
                  if (row_type.equals(Material.ITEM_FRAME) && e instanceof ItemFrame || row_type.equals(Material.PAINTING) && e instanceof Painting) {
                     Location el = e.getLocation();
                     if (el.getBlockX() == row_x && el.getBlockY() == row_y && el.getBlockZ() == row_z) {
                        e.remove();
                        break;
                     }
                  }
               }

               int dx1 = row_x + 1;
               dx2 = row_x - 1;
               dz1 = row_z + 1;
               int dz2 = row_z - 1;
               Block c1 = block.getWorld().getBlockAt(dx1, row_y, row_z);
               Block c2 = block.getWorld().getBlockAt(dx2, row_y, row_z);
               Block c3 = block.getWorld().getBlockAt(row_x, row_y, dz1);
               Block c4 = block.getWorld().getBlockAt(row_x, row_y, dz2);
               BlockFace face_set = null;
               if (!BlockInfo.non_attachable.contains(c1.getType())) {
                  face_set = BlockFace.WEST;
                  block = c1;
               } else if (!BlockInfo.non_attachable.contains(c2.getType())) {
                  face_set = BlockFace.EAST;
                  block = c2;
               } else if (!BlockInfo.non_attachable.contains(c3.getType())) {
                  face_set = BlockFace.NORTH;
                  block = c3;
               } else if (!BlockInfo.non_attachable.contains(c4.getType())) {
                  face_set = BlockFace.SOUTH;
                  block = c4;
               }

               BlockFace face = null;
               if (!Functions.solidBlock(Functions.getType(block.getRelative(BlockFace.EAST)))) {
                  face = BlockFace.EAST;
               } else if (!Functions.solidBlock(Functions.getType(block.getRelative(BlockFace.NORTH)))) {
                  face = BlockFace.NORTH;
               } else if (!Functions.solidBlock(Functions.getType(block.getRelative(BlockFace.WEST)))) {
                  face = BlockFace.WEST;
               } else if (!Functions.solidBlock(Functions.getType(block.getRelative(BlockFace.SOUTH)))) {
                  face = BlockFace.SOUTH;
               }

               if (face_set != null && face != null) {
                  if (row_type.equals(Material.PAINTING)) {
                     String art_name = Functions.getArtName(row_data);
                     Art painting = Art.getByName(art_name.toUpperCase());
                     int height = painting.getBlockHeight();
                     int width = painting.getBlockWidth();
                     int painting_x = row_x;
                     int painting_y = row_y;
                     int painting_z = row_z;
                     if (height != 1 || width != 1) {
                        if (height > 1 && height != 3) {
                           painting_y = row_y - 1;
                        }

                        if (width > 1) {
                           if (face_set.equals(BlockFace.WEST)) {
                              painting_z = row_z - 1;
                           } else if (face_set.equals(BlockFace.SOUTH)) {
                              painting_x = row_x - 1;
                           }
                        }
                     }

                     Block spawn_block = block.getRelative(face);
                     Material current_type = spawn_block.getType();
                     int current_data = Functions.getData(spawn_block);
                     Functions.setTypeAndData(spawn_block, Material.AIR, (byte)0, true);
                     Painting hanging = null;

                     try {
                        hanging = (Painting)block.getWorld().spawn(spawn_block.getLocation(), Painting.class);
                     } catch (Exception var28) {
                     }

                     if (hanging != null) {
                        Functions.setTypeAndData(spawn_block, current_type, (byte)current_data, true);
                        hanging.teleport(block.getWorld().getBlockAt(painting_x, painting_y, painting_z).getLocation());
                        hanging.setFacingDirection(face_set, true);
                        hanging.setArt(painting, true);
                     }
                  } else if (row_type.equals(Material.ITEM_FRAME)) {
                     try {
                        Block spawn_blockx = block.getRelative(face);
                        Material current_typex = spawn_blockx.getType();
                        int current_datax = Functions.getData(spawn_blockx);
                        Functions.setTypeAndData(spawn_blockx, Material.AIR, (byte)0, true);
                        ItemFrame hangingx = null;
                        hangingx = (ItemFrame)block.getWorld().spawn(spawn_blockx.getLocation(), ItemFrame.class);
                        if (hangingx != null) {
                           Functions.setTypeAndData(spawn_blockx, current_typex, (byte)current_datax, true);
                           hangingx.teleport(block.getWorld().getBlockAt(row_x, row_y, row_z).getLocation());
                           hangingx.setFacingDirection(face_set, true);
                           Material row_data_material = Functions.getType(row_data);
                           if (row_data_material != null) {
                              ItemStack istack = new ItemStack(row_data_material, 1);
                              hangingx.setItem(istack);
                           }
                        }
                     } catch (Exception var27) {
                     }
                  }
               }
            } catch (Exception var29) {
               var29.printStackTrace();
            }

         }
      }, (long)delay);
   }

   public static boolean successfulQuery(Connection connection, String query) {
      boolean result = false;

      try {
         PreparedStatement preparedStmt = connection.prepareStatement(query);
         ResultSet resultSet = preparedStmt.executeQuery();
         if (resultSet.isBeforeFirst()) {
            result = true;
         }

         resultSet.close();
         preparedStmt.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return result;
   }

   public static String[] toStringArray(String input) {
      int commaCount = input.replaceAll("[^,]", "").length();
      if (commaCount == 8) {
         String[] data = input.split(",");
         String action_time = data[0];
         String action_player = data[1];
         String action_cords = data[2];
         String[] datacords = action_cords.split("\\.");
         String action_x = datacords[0];
         String action_y = datacords[1];
         String action_z = datacords[2];
         String action_type = data[3];
         String action_data = data[4];
         String action_action = data[5];
         String action_rb = data[6];
         String action_wid = data[7].trim();
         return new String[]{action_time, action_player, action_x, action_y, action_z, action_type, action_data, action_action, action_rb, action_wid};
      } else {
         return null;
      }
   }

   public static void updateBlock(final BlockState block) {
      CoreProtect.getInstance().getServer().getScheduler().runTask(CoreProtect.getInstance(), new Runnable() {
         public void run() {
            try {
               block.update();
            } catch (Exception var2) {
               var2.printStackTrace();
            }

         }
      });
   }

   public static void updateInventory(Player player) {
      player.updateInventory();
   }

   public static boolean checkWorldEdit() {
      boolean result = false;
      CoreProtect pl = CoreProtect.getInstance();
      Iterator var2 = pl.getServer().getWorlds().iterator();

      while(var2.hasNext()) {
         World world = (World)var2.next();
         if (checkConfig(world, "worldedit") == 1) {
            result = true;
            break;
         }
      }

      if (result) {
         Plugin plugin = pl.getServer().getPluginManager().getPlugin("WorldEdit");
         if (plugin == null || CoreProtectEditSessionEvent.isInitialized()) {
            result = false;
         }
      }

      return result;
   }
}
