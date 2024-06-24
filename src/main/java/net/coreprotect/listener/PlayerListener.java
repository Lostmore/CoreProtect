package net.coreprotect.listener;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.model.BlockInfo;
import net.coreprotect.model.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PlayerListener extends Queue implements Listener {
   public static ConcurrentHashMap<String, Object[]> lastInspectorEvent = new ConcurrentHashMap();

   private void onInventoryInteract(HumanEntity entity, Inventory inventory) {
      World world = entity.getWorld();
      if (Functions.checkConfig(world, "item-transactions") == 1) {
         Player player = (Player)entity;
         if (inventory != null) {
            InventoryHolder i = inventory.getHolder();
            if (i != null) {
               Material type = Material.CHEST;
               Location l = null;
               if (i instanceof BlockState) {
                  BlockState state = (BlockState)i;
                  type = state.getType();
                  if (BlockInfo.containers.contains(type)) {
                     l = state.getLocation();
                  }
               } else if (i instanceof DoubleChest) {
                  DoubleChest state = (DoubleChest)i;
                  l = state.getLocation();
               }

               if (l != null) {
                  int x = l.getBlockX();
                  int y = l.getBlockY();
                  int z = l.getBlockZ();
                  Iterator var11 = inventory.getViewers().iterator();

                  while(var11.hasNext()) {
                     HumanEntity viewer = (HumanEntity)var11.next();
                     if (!viewer.getName().equals(player.getName())) {
                        String logging_chest_id = viewer.getName().toLowerCase() + "." + x + "." + y + "." + z;
                        if (Config.old_container.get(logging_chest_id) != null) {
                           int size_old = (Config.old_container.get(logging_chest_id)).size();
                           if (Config.force_containers.get(logging_chest_id) == null) {
                              Config.force_containers.put(logging_chest_id, new ArrayList());
                           }

                           List<ItemStack[]> list = Config.force_containers.get(logging_chest_id);
                           if (list.size() < size_old) {
                              list.add(Functions.get_container_state(inventory.getContents()));
                              Config.force_containers.put(logging_chest_id, list);
                           }
                        }
                     }
                  }

                  int chest_id = 0;
                  String logging_chest_id = player.getName().toLowerCase() + "." + x + "." + y + "." + z;
                  if (Config.logging_chest.get(logging_chest_id) != null) {
                     if (Config.force_containers.get(logging_chest_id) != null) {
                        int force_size = (Config.force_containers.get(logging_chest_id)).size();
                        List<ItemStack[]> list = Config.old_container.get(logging_chest_id);
                        if (list.size() > force_size) {
                           list.set(force_size, Functions.get_container_state(inventory.getContents()));
                        } else {
                           list.add(Functions.get_container_state(inventory.getContents()));
                        }

                        Config.old_container.put(logging_chest_id, list);
                     }

                     chest_id = Config.logging_chest.get(logging_chest_id) + 1;
                  } else {
                     List<ItemStack[]> list = new ArrayList();
                     list.add(Functions.get_container_state(inventory.getContents()));
                     Config.old_container.put(logging_chest_id, list);
                  }

                  Config.logging_chest.put(logging_chest_id, chest_id);
                  Queue.queueContainerTransaction(player.getName(), l.getBlock().getState(), type, inventory, chest_id);
               }
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onInventoryClick(InventoryClickEvent event) {
      this.onInventoryInteract(event.getWhoClicked(), event.getInventory());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onInventoryDragEvent(InventoryDragEvent event) {
      this.onInventoryInteract(event.getWhoClicked(), event.getInventory());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
      final Player player = event.getPlayer();
      final ArmorStand armorStand = event.getRightClicked();
      EntityEquipment equipment = armorStand.getEquipment();
      ItemStack[] contents = equipment.getArmorContents();
      ItemStack item = event.getArmorStandItem();
      ItemStack playerItem = event.getPlayerItem();
      if (Functions.checkConfig(player.getWorld(), "item-transactions") == 1) {
         if (Config.inspecting.get(player.getName()) != null && (Boolean)Config.inspecting.get(player.getName())) {
            if (BlockInfo.containers.contains(Material.ARMOR_STAND)) {
               class BasicThread implements Runnable {
                  public void run() {
                     try {
                        if (Config.converter_running) {
                           player.sendMessage("§3CoreProtect §f- Выполняется обновление. Пожалуйста повторите позже.");
                           return;
                        }

                        if (Config.purge_running) {
                           player.sendMessage("§3CoreProtect §f- Идет чистка. Пожалуйста, повторите попытку позже");
                           return;
                        }

                        Connection connection = Database.getConnection(false);
                        if (connection != null) {
                           Statement statement = connection.createStatement();
                           Location l = armorStand.getLocation();
                           String blockdata = Lookup.chest_transactions(statement, l, player.getName(), 1, 7);
                           if (blockdata.contains("\n")) {
                              String[] var5 = blockdata.split("\n");
                              int var6 = var5.length;

                              for(int var7 = 0; var7 < var6; ++var7) {
                                 String b = var5[var7];
                                 player.sendMessage(b);
                              }
                           } else {
                              player.sendMessage(blockdata);
                           }

                           statement.close();
                           connection.close();
                        } else {
                           player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже");
                        }
                     } catch (Exception var9) {
                        var9.printStackTrace();
                     }

                  }
               }

               Runnable runnable = new BasicThread();
               Thread thread = new Thread(runnable);
               thread.start();
            }

            event.setCancelled(true);
         }

         if (!event.isCancelled()) {
            if (item != null || playerItem != null) {
               Material type = Material.ARMOR_STAND;
               Location l = armorStand.getLocation();
               int x = l.getBlockX();
               int y = l.getBlockY();
               int z = l.getBlockZ();
               int chest_id = 0;
               String logging_chest_id = player.getName().toLowerCase() + "." + x + "." + y + "." + z;
               if (Config.logging_chest.get(logging_chest_id) != null) {
                  if (Config.force_containers.get(logging_chest_id) != null) {
                     int force_size = (Config.force_containers.get(logging_chest_id)).size();
                     List<ItemStack[]> list = Config.old_container.get(logging_chest_id);
                     if (list.size() > force_size) {
                        list.set(force_size, Functions.get_container_state(contents));
                     } else {
                        list.add(Functions.get_container_state(contents));
                     }

                     Config.old_container.put(logging_chest_id, list);
                  }
                  chest_id = Config.logging_chest.get(logging_chest_id) + 1;
               } else {
                  List<ItemStack[]> list = new ArrayList();
                  list.add(Functions.get_container_state(contents));
                  Config.old_container.put(logging_chest_id, list);
               }

               Config.logging_chest.put(logging_chest_id, chest_id);
               Queue.queueContainerTransaction(player.getName(), l.getBlock().getState(), type, equipment, chest_id);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   protected void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
      String player = event.getPlayer().getName();
      Block block = event.getBlockClicked().getRelative(event.getBlockFace());
      World world = block.getWorld();
      boolean inspect = false;
      if (Config.inspecting.get(player) != null && Config.inspecting.get(player)) {
         inspect = true;
         event.setCancelled(true);
      }

      if (!event.isCancelled() && Functions.checkConfig(world, "buckets") == 1 && !inspect) {
         int wid = Functions.getWorldId(block.getWorld().getName());
         int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
         Material type = Material.WATER;
         if (event.getBucket().equals(Material.LAVA_BUCKET)) {
            type = Material.LAVA;
         }

         Config.lookup_cache.put("" + block.getX() + "." + block.getY() + "." + block.getZ() + "." + wid + "", new Object[]{unixtimestamp, player, type});
         Queue.queueBlockPlace(player, block.getState(), type, 0);
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   protected void onPlayerBucketFill(PlayerBucketFillEvent event) {
      String player = event.getPlayer().getName();
      Block block = event.getBlockClicked().getRelative(event.getBlockFace());
      World world = block.getWorld();
      if (Config.inspecting.get(player) != null && Config.inspecting.get(player)) {
         event.setCancelled(true);
      }
       if (!event.isCancelled()) {
           Functions.checkConfig(world, "buckets");
       }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChat(AsyncPlayerChatEvent event) {
      String message = event.getMessage();
      if (message != null) {
         Player player = event.getPlayer();
         if (!message.startsWith("/") && Functions.checkConfig(player.getWorld(), "player-messages") == 1) {
            int time = (int)(System.currentTimeMillis() / 1000L);
            Queue.queuePlayerChat(player, message, time);
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
      Player player = event.getPlayer();
      if (Functions.checkConfig(player.getWorld(), "player-commands") == 1) {
         int time = (int)(System.currentTimeMillis() / 1000L);
         Queue.queuePlayerCommand(player, event.getMessage(), time);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   protected void onPlayerInteract(PlayerInteractEvent event) {
      final Player player = event.getPlayer();
      World world = player.getWorld();
      boolean inspecting_or_something = false;
      List<Material> interact_blocks = Arrays.asList(Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE, Material.DISPENSER, Material.NOTE_BLOCK, Material.CHEST, Material.FURNACE, Material.BURNING_FURNACE, Material.WOODEN_DOOR, Material.LEVER, Material.STONE_BUTTON, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.TRAP_DOOR, Material.FENCE_GATE, Material.BREWING_STAND, Material.WOOD_BUTTON, Material.ANVIL, Material.TRAPPED_CHEST, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.HOPPER, Material.DROPPER, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.SILVER_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX);
      Block check_block;
      Block block_under;
      Block interact_block;
      if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
         if (Config.inspecting.get(player.getName()) != null && Config.inspecting.get(player.getName())) {
            check_block = event.getClickedBlock();
            if (check_block.getType().equals(Material.DOUBLE_PLANT) && Functions.getData(check_block) >= 8) {
               check_block = world.getBlockAt(check_block.getX(), check_block.getY() - 1, check_block.getZ());
            }

            Block finalCheck_block = check_block;
            class BasicThread implements Runnable {
               public void run() {
                  try {
                     if (Config.converter_running) {
                        player.sendMessage("§3CoreProtect §f- Обновление в процессе. Пожалуйста, повторите попытку позже");
                        return;
                     }

                     if (Config.purge_running) {
                        player.sendMessage("§3CoreProtect §f- Идет чистка. Пожалуйста, повторите попытку позже");
                        return;
                     }

                     Connection connection = Database.getConnection(false);
                     if (connection != null) {
                        Statement statement = connection.createStatement();
                        String result_data = Lookup.block_lookup(statement, finalCheck_block, player.getName(), 0, 1, 7);
                        if (result_data.contains("\n")) {
                           String[] var4 = result_data.split("\n");
                           int var5 = var4.length;

                           for(int var6 = 0; var6 < var5; ++var6) {
                              String b = var4[var6];
                              player.sendMessage(b);
                           }
                        } else if (result_data.length() > 0) {
                           player.sendMessage(result_data);
                        }

                        statement.close();
                        connection.close();
                     } else {
                        player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже");
                     }
                  } catch (Exception var8) {
                     var8.printStackTrace();
                  }

               }
            }

            Runnable runnable = new BasicThread();
            Thread thread = new Thread(runnable);
            thread.start();
            event.setCancelled(true);
         }
      } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && Config.inspecting.get(player.getName()) != null && Config.inspecting.get(player.getName())) {
         List<Material> safe_blocks = Arrays.asList(Material.WOODEN_DOOR, Material.LEVER, Material.STONE_BUTTON, Material.TRAP_DOOR, Material.FENCE_GATE, Material.SPRUCE_DOOR, Material.BIRCH_DOOR, Material.JUNGLE_DOOR, Material.ACACIA_DOOR, Material.DARK_OAK_DOOR, Material.SPRUCE_FENCE_GATE, Material.BIRCH_FENCE_GATE, Material.JUNGLE_FENCE_GATE, Material.DARK_OAK_FENCE_GATE, Material.ACACIA_FENCE_GATE);
         Block block = event.getClickedBlock();
         if (block != null) {
            final Material type = block.getType();
            if (interact_blocks.contains(type)) {
               interact_block = event.getClickedBlock();
               if (BlockInfo.containers.contains(type) && Functions.checkConfig(world, "item-transactions") == 1) {
                  Block finalInteract_block = interact_block;
                  class BasicThread implements Runnable {
                     public void run() {
                        try {
                           if (Config.converter_running) {
                              player.sendMessage("§3CoreProtect §f- Обновление в процессе. Пожалуйста, повторите попытку позже");
                              return;
                           }

                           if (Config.purge_running) {
                              player.sendMessage("§3CoreProtect §f- Идет чистка. Пожалуйста, повторите попытку позже");
                              return;
                           }

                           Connection connection = Database.getConnection(false);
                           if (connection != null) {
                              Statement statement = connection.createStatement();
                              Location l = null;
                              if (type.equals(Material.CHEST) || type.equals(Material.TRAPPED_CHEST)) {
                                 Chest chest = (Chest) finalInteract_block.getState();
                                 InventoryHolder i = chest.getInventory().getHolder();
                                 if (i instanceof DoubleChest) {
                                    DoubleChest c = (DoubleChest)i;
                                    l = c.getLocation();
                                 } else {
                                    l = chest.getLocation();
                                 }
                              }

                              if (l == null) {
                                 l = finalInteract_block.getLocation();
                              }

                              String blockdata = Lookup.chest_transactions(statement, l, player.getName(), 1, 7);
                              if (blockdata.contains("\n")) {
                                 String[] var11 = blockdata.split("\n");
                                 int var12 = var11.length;

                                 for(int var7 = 0; var7 < var12; ++var7) {
                                    String b = var11[var7];
                                    player.sendMessage(b);
                                 }
                              } else {
                                 player.sendMessage(blockdata);
                              }

                              statement.close();
                              connection.close();
                           } else {
                              player.sendMessage("§3CoreProtect §f- База данных занята. Пожалуйста, повторите попытку позже");
                           }
                        } catch (Exception var9) {
                           var9.printStackTrace();
                        }

                     }
                  }

                  Runnable runnable = new BasicThread();
                  Thread thread = new Thread(runnable);
                  thread.start();
                  event.setCancelled(true);
               } else {
                  block_under = interact_block;
                  if (type.equals(Material.WOODEN_DOOR) || type.equals(Material.SPRUCE_DOOR) || type.equals(Material.BIRCH_DOOR) || type.equals(Material.JUNGLE_DOOR) || type.equals(Material.ACACIA_DOOR) || type.equals(Material.DARK_OAK_DOOR)) {
                     int y = interact_block.getY() - 1;
                     block_under = interact_block.getWorld().getBlockAt(interact_block.getX(), y, interact_block.getZ());
                     if (block_under.getType().equals(type)) {
                        block_under = interact_block;
                     }
                  }

                  Block finalBlock_under = block_under;
                  class BasicThread implements Runnable {
                     public void run() {
                        try {
                           if (Config.converter_running) {
                              player.sendMessage("§3CoreProtect §f- Обновление в процессе. Пожалуйста, повторите попытку позже.");
                              return;
                           }

                           if (Config.purge_running) {
                              player.sendMessage("§3CoreProtect §f- Идет чистка. Пожалуйста, повторите попытку позже.");
                              return;
                           }

                           Connection connection = Database.getConnection(false);
                           if (connection != null) {
                              Statement statement = connection.createStatement();
                              String blockdata = Lookup.interaction_lookup(statement, finalBlock_under, player.getName(), 0, 1, 7);
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
                  if (!safe_blocks.contains(type)) {
                     event.setCancelled(true);
                  }
               }

               inspecting_or_something = true;
            } else {
               boolean performLookup = true;
               EquipmentSlot eventHand = event.getHand();
               String uuid = event.getPlayer().getUniqueId().toString();
               long systemTime = System.currentTimeMillis();
               if (lastInspectorEvent.get(uuid) != null) {
                  Object[] lastEvent = (Object[])lastInspectorEvent.get(uuid);
                  long lastTime = (Long)lastEvent[0];
                  EquipmentSlot lastHand = (EquipmentSlot)lastEvent[1];
                  long timeSince = systemTime - lastTime;
                  if (timeSince < 50L && !eventHand.equals(lastHand)) {
                     performLookup = false;
                  }
               }

               if (performLookup) {
                  final Block fblock = event.getClickedBlock().getRelative(event.getBlockFace());

                  class BasicThread implements Runnable {
                     public void run() {
                        try {
                           if (Config.converter_running) {
                              player.sendMessage("§3CoreProtect §f- Обновление в процессе. Пожалуйста, повторите попытку позже");
                              return;
                           }

                           if (Config.purge_running) {
                              player.sendMessage("§3CoreProtect §f- Идет чистка. Пожалуйста, повторите попытку позже.");
                              return;
                           }

                           Connection connection = Database.getConnection(false);
                           if (connection != null) {
                              Statement statement = connection.createStatement();
                              String blockdata;
                              String[] var4;
                              int var5;
                              int var6;
                              String b;
                              if (fblock.getType().equals(Material.AIR)) {
                                 blockdata = Lookup.block_lookup(statement, fblock, player.getName(), 0, 1, 7);
                                 if (blockdata.contains("\n")) {
                                    var4 = blockdata.split("\n");
                                    var5 = var4.length;

                                    for(var6 = 0; var6 < var5; ++var6) {
                                       b = var4[var6];
                                       player.sendMessage(b);
                                    }
                                 } else if (blockdata.length() > 0) {
                                    player.sendMessage(blockdata);
                                 }
                              } else {
                                 blockdata = Lookup.block_lookup(statement, fblock, player.getName(), 0, 1, 7);
                                 if (blockdata.contains("\n")) {
                                    var4 = blockdata.split("\n");
                                    var5 = var4.length;

                                    for(var6 = 0; var6 < var5; ++var6) {
                                       b = var4[var6];
                                       player.sendMessage(b);
                                    }
                                 } else if (blockdata.length() > 0) {
                                    player.sendMessage(blockdata);
                                 }
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
                  Functions.updateInventory(event.getPlayer());
                  lastInspectorEvent.put(uuid, new Object[]{systemTime, eventHand});
               }

               event.setCancelled(true);
            }
         }
      }

      check_block = event.getClickedBlock();
      if (check_block != null) {
         Material type = check_block.getType();
         if (interact_blocks.contains(type)) {
            boolean valid_click = true;
            if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
               valid_click = false;
            }

            if (!inspecting_or_something && valid_click && event.getHand().equals(EquipmentSlot.HAND) && !event.isCancelled() && Functions.checkConfig(world, "player-interactions") == 1) {
               interact_block = event.getClickedBlock();
               if (type.equals(Material.WOODEN_DOOR) || type.equals(Material.SPRUCE_DOOR) || type.equals(Material.BIRCH_DOOR) || type.equals(Material.JUNGLE_DOOR) || type.equals(Material.ACACIA_DOOR) || type.equals(Material.DARK_OAK_DOOR)) {
                  int y = interact_block.getY() - 1;
                  block_under = interact_block.getWorld().getBlockAt(interact_block.getX(), y, interact_block.getZ());
                  if (block_under.getType().equals(type)) {
                     interact_block = block_under;
                  }
               }

               Queue.queuePlayerInteraction(player.getName(), interact_block.getState());
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerInteract_Monitor(PlayerInteractEvent event) {
      Player player;
      World world;
      Material handType;
      if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
         world = event.getClickedBlock().getWorld();
         if (!event.isCancelled() && Functions.checkConfig(world, "block-break") == 1) {
            Block relative_block = event.getClickedBlock().getRelative(event.getBlockFace());
            if (relative_block.getType().equals(Material.FIRE)) {
               player = event.getPlayer();
               handType = relative_block.getType();
               int data = Functions.getData(relative_block);
               Queue.queueBlockBreak(player.getName(), relative_block.getState(), handType, data);
            }
         }
      } else {
         Block block_above;
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            world = event.getClickedBlock().getWorld();
            if (!event.isCancelled() && Functions.checkConfig(world, "block-place") == 1) {
               player = event.getPlayer();
               List<Material> entityBlockTypes = Arrays.asList(Material.ARMOR_STAND, Material.END_CRYSTAL);
               ItemStack mainHand = player.getInventory().getItemInMainHand();
               ItemStack offHand = player.getInventory().getItemInOffHand();
               if (event.getHand().equals(EquipmentSlot.HAND) && mainHand != null && entityBlockTypes.contains(mainHand.getType())) {
                  handType = mainHand.getType();
               } else {
                  if (!event.getHand().equals(EquipmentSlot.OFF_HAND) || offHand == null || !entityBlockTypes.contains(offHand.getType())) {
                     return;
                  }

                  handType = offHand.getType();
               }

               if (handType.equals(Material.END_CRYSTAL)) {
                  final Location crystalLocation = event.getClickedBlock().getLocation();
                  if (crystalLocation.getBlock().getType().equals(Material.OBSIDIAN) || crystalLocation.getBlock().getType().equals(Material.BEDROCK)) {
                     crystalLocation.setY(crystalLocation.getY() + 1.0D);
                     boolean exists = false;
                     Entity[] var10 = crystalLocation.getChunk().getEntities();
                     int var11 = var10.length;

                     for(int var12 = 0; var12 < var11; ++var12) {
                        Entity entity = var10[var12];
                        if (entity instanceof EnderCrystal && entity.getLocation().getBlockX() == crystalLocation.getBlockX() && entity.getLocation().getBlockY() == crystalLocation.getBlockY() && entity.getLocation().getBlockZ() == crystalLocation.getBlockZ()) {
                           exists = true;
                           break;
                        }
                     }

                     if (!exists) {
                        CoreProtect.getInstance().getServer().getScheduler().runTask(CoreProtect.getInstance(), new Runnable() {
                           public void run() {
                              try {
                                 boolean exists = false;
                                 int showingBottom = 0;
                                 Entity[] var3 = crystalLocation.getChunk().getEntities();
                                 int var4 = var3.length;

                                 for(int var5 = 0; var5 < var4; ++var5) {
                                    Entity entity = var3[var5];
                                    if (entity instanceof EnderCrystal && entity.getLocation().getBlockX() == crystalLocation.getBlockX() && entity.getLocation().getBlockY() == crystalLocation.getBlockY() && entity.getLocation().getBlockZ() == crystalLocation.getBlockZ()) {
                                       EnderCrystal enderCrystal = (EnderCrystal)entity;
                                       showingBottom = enderCrystal.isShowingBottom() ? 1 : 0;
                                       exists = true;
                                       break;
                                    }
                                 }

                                 if (exists) {
                                    PlayerListener.queueBlockPlace(player.getName(), crystalLocation.getBlock().getState(), crystalLocation.getBlock(), crystalLocation.getBlock().getState(), Material.END_CRYSTAL, showingBottom, 1);
                                 }
                              } catch (Exception var8) {
                                 var8.printStackTrace();
                              }

                           }
                        });
                     }
                  }
               } else {
                  Block relative_block = event.getClickedBlock().getRelative(event.getBlockFace());
                  Location location_1 = relative_block.getLocation();
                  Location location_2 = event.getClickedBlock().getLocation();
                  String key_1 = world.getName() + "-" + location_1.getBlockX() + "-" + location_1.getBlockY() + "-" + location_1.getBlockZ();
                  String key_2 = world.getName() + "-" + location_2.getBlockX() + "-" + location_2.getBlockY() + "-" + location_2.getBlockZ();
                  Object[] keys = new Object[]{key_1, key_2, handType};
                  Config.entity_block_mapper.put(player.getUniqueId(), keys);
               }
            }
         } else if (event.getAction().equals(Action.PHYSICAL)) {
            Block block = event.getClickedBlock();
            if (block == null) {
               return;
            }

            if (!block.getType().equals(Material.SOIL)) {
               return;
            }

            world = block.getWorld();
            if (!event.isCancelled() && Functions.checkConfig(world, "block-break") == 1) {
               player = event.getPlayer();
               block_above = world.getBlockAt(block.getX(), block.getY() + 1, block.getZ());
               Material type = block_above.getType();
               if (!type.equals(Material.AIR)) {
                  int data = Functions.getData(block_above);
                  Queue.queueBlockBreak(player.getName(), block_above.getState(), type, data);
               }

               Queue.queueBlockPlace(player.getName(), (Block)block, block.getState(), Material.DIRT, Functions.getData(block));
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      if (!(event instanceof PlayerArmorStandManipulateEvent)) {
         Player player = event.getPlayer();
         Entity entity = event.getRightClicked();
         World world = entity.getWorld();
         if (entity instanceof ItemFrame && !event.isCancelled() && Functions.checkConfig(world, "block-place") == 1) {
            ItemFrame frame = (ItemFrame)entity;
            if (frame.getItem().getType().equals(Material.AIR) && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
               Material t = Material.ITEM_FRAME;
               int hand = Functions.block_id(player.getInventory().getItemInMainHand().getType());
               int d = 0;
               if (frame.getItem() != null) {
                  d = Functions.block_id(frame.getItem().getType());
               }

               String playername = player.getName();
               Block block = frame.getLocation().getBlock();
               Queue.queueBlockBreak(playername, block.getState(), t, d);
               Queue.queueBlockPlace(playername, block.getState(), t, hand);
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      int time = (int)(System.currentTimeMillis() / 1000L);
      Queue.queuePlayerLogin(player, time, Functions.checkConfig(player.getWorld(), "player-sessions"), Functions.checkConfig(player.getWorld(), "username-changes"));
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      if (Functions.checkConfig(player.getWorld(), "player-sessions") == 1) {
         int time = (int)(System.currentTimeMillis() / 1000L);
         Queue.queuePlayerQuit(player, time);
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onSignChange(SignChangeEvent event) {
      String player = event.getPlayer().getName();
      Block block = event.getBlock();
      String line1 = event.getLine(0);
      String line2 = event.getLine(1);
      String line3 = event.getLine(2);
      String line4 = event.getLine(3);
      if (!event.isCancelled() && Functions.checkConfig(block.getWorld(), "sign-text") == 1) {
         Queue.queueSignText(player, block.getState(), line1, line2, line3, line4, 0);
      }

   }
}
