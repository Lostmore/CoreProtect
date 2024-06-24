package net.coreprotect.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.Logger;
import net.coreprotect.model.BlockInfo;
import net.coreprotect.model.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderDragonPart;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

public class EntityListener extends Queue implements Listener {
   @EventHandler
   public void onCreatureSpawn(CreatureSpawnEvent event) {
      if (!event.isCancelled()) {
         if (event.getEntityType().equals(EntityType.ARMOR_STAND)) {
            World world = event.getEntity().getWorld();
            Location location = event.getEntity().getLocation();
            String key = world.getName() + "-" + location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ();
            Iterator it = Config.entity_block_mapper.entrySet().iterator();

            while(true) {
               UUID uuid;
               Object[] data;
               do {
                  if (!it.hasNext()) {
                     return;
                  }

                  Entry<UUID, Object[]> pair = (Entry)it.next();
                  uuid = pair.getKey();
                  data = pair.getValue();
               } while(!data[0].equals(key) && !data[1].equals(key));

               if (Functions.getEntityMaterial(event.getEntityType()).equals(data[2])) {
                  Player player = CoreProtect.getInstance().getServer().getPlayer(uuid);
                  Queue.queueBlockPlace(player.getName(), location.getBlock().getState(), location.getBlock(), location.getBlock().getState(), (Material)data[2], (int)event.getEntity().getLocation().getYaw(), 1);
                  it.remove();
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onEntityBlockForm(EntityBlockFormEvent event) {
      World world = event.getBlock().getWorld();
      if (!event.isCancelled() && Functions.checkConfig(world, "entity-change") == 1) {
         Entity entity = event.getEntity();
         Block block = event.getBlock();
         BlockState newState = event.getNewState();
         String e = "";
         if (entity instanceof Snowman) {
            e = "#snowman";
         }

         if (e.length() > 0) {
            Queue.queueBlockPlace(e, block.getState(), newState.getType(), Functions.getData(newState));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onEntityChangeBlock(EntityChangeBlockEvent event) {
      World world = event.getBlock().getWorld();
      if (!event.isCancelled() && Functions.checkConfig(world, "entity-change") == 1) {
         Entity entity = event.getEntity();
         Block block = event.getBlock();
         Material newtype = event.getTo();
         Material type = event.getBlock().getType();
         int data = Functions.getData(event.getBlock());
         String e = "";
         if (entity instanceof Enderman) {
            e = "#enderman";
         } else if (entity instanceof EnderDragon) {
            e = "#enderdragon";
         } else if (entity instanceof Wither) {
            e = "#wither";
         } else if (entity instanceof Silverfish && newtype.equals(Material.AIR)) {
            e = "#silverfish";
         }

         if (e.length() > 0) {
            if (newtype.equals(Material.AIR)) {
               Queue.queueBlockBreak(e, block.getState(), type, data);
            } else {
               Queue.queueBlockPlace(e, block);
            }
         }
      }

   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
      Entity damager = event.getDamager();
      if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof ArmorStand || event.getEntity() instanceof EnderCrystal) {
         boolean inspecting = false;
         String user = "#entity";
         if (damager != null) {
            Entity entity = event.getEntity();
            Block block = entity.getLocation().getBlock();
            if (damager instanceof Player) {
               Player player = (Player)damager;
               user = player.getName();
               if (Config.inspecting.get(player.getName()) != null && Config.inspecting.get(player.getName())) {
                  HangingListener.inspectItemFrame(block, player);
                  event.setCancelled(true);
                  inspecting = true;
               }
            } else if (damager instanceof Arrow) {
               Arrow arrow = (Arrow)damager;
               ProjectileSource source = arrow.getShooter();
               if (source instanceof Player) {
                  Player player = (Player)source;
                  user = player.getName();
               }
            } else if (damager instanceof TNTPrimed) {
               user = "#tnt";
            } else if (damager instanceof Minecart) {
               String name = damager.getType().name();
               if (name.contains("TNT")) {
                  user = "#tnt";
               }
            } else if (damager instanceof Creeper) {
               user = "#creeper";
            } else if (!(damager instanceof EnderDragon) && !(damager instanceof EnderDragonPart)) {
               if (!(damager instanceof Wither) && !(damager instanceof WitherSkull)) {
                  if (damager.getType() != null) {
                     user = "#" + damager.getType().name().toLowerCase();
                  }
               } else {
                  user = "#wither";
               }
            } else {
               user = "#enderdragon";
            }

            if (!event.isCancelled() && Functions.checkConfig(entity.getWorld(), "block-break") == 1 && !inspecting) {
               if (entity instanceof ItemFrame) {
                  ItemFrame frame = (ItemFrame)event.getEntity();
                  int data = 0;
                  if (frame.getItem() != null) {
                     data = Functions.block_id(frame.getItem().getType());
                  }

                  Queue.queueBlockBreak(user, block.getState(), Material.ITEM_FRAME, data);
                  Queue.queueBlockPlace(user, block.getState(), Material.ITEM_FRAME, 0);
               } else if (entity instanceof ArmorStand) {
                  Database.containerBreakCheck(user, Material.ARMOR_STAND, entity, block.getLocation());
                  Queue.queueBlockBreak(user, block.getState(), Material.ARMOR_STAND, (int)entity.getLocation().getYaw());
               } else if (entity instanceof EnderCrystal) {
                  EnderCrystal crystal = (EnderCrystal)event.getEntity();
                  Queue.queueBlockBreak(user, block.getState(), Material.END_CRYSTAL, crystal.isShowingBottom() ? 1 : 0);
               }
            }
         }
      }

   }

   @EventHandler
   public void onEntityDeath(EntityDeathEvent event) {
      LivingEntity entity = event.getEntity();
      if (entity != null) {
         if (Functions.checkConfig(entity.getWorld(), "entity-kills") == 1) {
            EntityDamageEvent damage = entity.getLastDamageCause();
            if (damage != null) {
               String e = "";
               boolean skip = true;
               if (Functions.checkConfig(entity.getWorld(), "skip-generic-data") == 0 || !(entity instanceof Zombie) && !(entity instanceof Skeleton)) {
                  skip = false;
               }

               if (damage instanceof EntityDamageByEntityEvent) {
                  EntityDamageByEntityEvent attack = (EntityDamageByEntityEvent)damage;
                  Entity attacker = attack.getDamager();
                  if (attacker instanceof Player) {
                     Player player = (Player)attacker;
                     e = player.getName();
                  } else {
                     ProjectileSource shooter;
                     Player player;
                     String name;
                     EntityType entityType;
                     if (attacker instanceof Arrow) {
                        Arrow arrow = (Arrow)attacker;
                        shooter = arrow.getShooter();
                        if (shooter instanceof Player) {
                           player = (Player)shooter;
                           e = player.getName();
                        } else if (shooter instanceof LivingEntity) {
                           entityType = ((LivingEntity)shooter).getType();
                           if (entityType != null) {
                              name = entityType.name().toLowerCase();
                              e = "#" + name;
                           }
                        }
                     } else if (attacker instanceof ThrownPotion) {
                        ThrownPotion potion = (ThrownPotion)attacker;
                        shooter = potion.getShooter();
                        if (shooter instanceof Player) {
                           player = (Player)shooter;
                           e = player.getName();
                        } else if (shooter instanceof LivingEntity) {
                           entityType = ((LivingEntity)shooter).getType();
                           if (entityType != null) {
                              name = entityType.name().toLowerCase();
                              e = "#" + name;
                           }
                        }
                     } else if (attacker.getType().name() != null) {
                        e = "#" + attacker.getType().name().toLowerCase();
                     }
                  }
               } else {
                  DamageCause cause = damage.getCause();
                  if (cause.equals(DamageCause.FIRE)) {
                     e = "#fire";
                  } else if (cause.equals(DamageCause.FIRE_TICK)) {
                     if (!skip) {
                        e = "#fire";
                     }
                  } else if (cause.equals(DamageCause.LAVA)) {
                     e = "#lava";
                  } else if (cause.equals(DamageCause.BLOCK_EXPLOSION)) {
                     e = "#explosion";
                  }
               }

               EntityType entity_type = entity.getType();
               if (e.length() == 0 && !skip) {
                  if (!(entity instanceof Player) && entity_type.name() != null) {
                     e = "#" + entity_type.name().toLowerCase();
                  } else if (entity instanceof Player) {
                     e = entity.getName();
                  }
               }

               if (e.startsWith("#wither")) {
                  e = "#wither";
               }

               if (e.startsWith("#enderdragon")) {
                  e = "#enderdragon";
               }

               if (e.startsWith("#primedtnt") || e.startsWith("#tnt")) {
                  e = "#tnt";
               }

               if (e.startsWith("#lightning")) {
                  e = "#lightning";
               }

               if (e.length() > 0) {
                  List<Object> data = new ArrayList();
                  List<Object> age = new ArrayList();
                  List<Object> tame = new ArrayList();
                  List<Object> attributes = new ArrayList();
                  List<Object> info = new ArrayList();
                  if (entity instanceof Ageable) {
                     Ageable ageable = (Ageable)entity;
                     age.add(ageable.getAge());
                     age.add(ageable.getAgeLock());
                     age.add(ageable.isAdult());
                     age.add(ageable.canBreed());
                     age.add(null);
                  }

                  if (entity instanceof Tameable) {
                     Tameable tameable = (Tameable)entity;
                     tame.add(tameable.isTamed());
                     if (tameable.isTamed() && tameable.getOwner() != null) {
                        tame.add(tameable.getOwner().getName());
                     }
                  }

                  ArrayList itemMap;
                  if (entity instanceof Attributable) {
                     Attributable attributable = entity;
                     Attribute[] var14 = Attribute.values();
                     int var15 = var14.length;

                     for(int var16 = 0; var16 < var15; ++var16) {
                        Attribute attribute = var14[var16];
                        AttributeInstance attributeInstance = attributable.getAttribute(attribute);
                        if (attributeInstance != null) {
                           itemMap = new ArrayList();
                           List<Object> attributeModifiers = new ArrayList();
                           itemMap.add(attributeInstance.getAttribute());
                           itemMap.add(attributeInstance.getBaseValue());
                           Iterator var21 = attributeInstance.getModifiers().iterator();

                           while(var21.hasNext()) {
                              AttributeModifier modifier = (AttributeModifier)var21.next();
                              attributeModifiers.add(modifier.serialize());
                           }

                           itemMap.add(attributeModifiers);
                           attributes.add(itemMap);
                        }
                     }
                  }

                  if (entity instanceof Creeper) {
                     Creeper creeper = (Creeper)entity;
                     info.add(creeper.isPowered());
                  } else if (entity instanceof Enderman) {
                     Enderman enderman = (Enderman)entity;
                     info.add(enderman.getCarriedMaterial().toItemStack().serialize());
                  } else if (entity instanceof IronGolem) {
                     IronGolem irongolem = (IronGolem)entity;
                     info.add(irongolem.isPlayerCreated());
                  } else if (entity instanceof Ocelot) {
                     Ocelot ocelot = (Ocelot)entity;
                     info.add(ocelot.getCatType());
                     info.add(ocelot.isSitting());
                  } else if (entity instanceof Pig) {
                     Pig pig = (Pig)entity;
                     info.add(pig.hasSaddle());
                  } else if (entity instanceof Sheep) {
                     Sheep sheep = (Sheep)entity;
                     info.add(sheep.isSheared());
                     info.add(sheep.getColor());
                  } else if (entity instanceof Skeleton) {
                     info.add(null);
                  } else if (entity instanceof Slime) {
                     Slime slime = (Slime)entity;
                     info.add(slime.getSize());
                  } else if (!(entity instanceof Villager)) {
                     if (entity instanceof Wolf) {
                        Wolf wolf = (Wolf)entity;
                        info.add(wolf.isSitting());
                        info.add(wolf.getCollarColor());
                     } else if (entity instanceof ZombieVillager) {
                        ZombieVillager zombieVillager = (ZombieVillager)entity;
                        info.add(zombieVillager.isBaby());
                        info.add(zombieVillager.getVillagerProfession());
                     } else if (entity instanceof Zombie) {
                        Zombie zombie = (Zombie)entity;
                        info.add(zombie.isBaby());
                        info.add(null);
                        info.add(null);
                     } else if (entity instanceof AbstractHorse) {
                        AbstractHorse abstractHorse = (AbstractHorse)entity;
                        info.add(null);
                        info.add(null);
                        info.add(abstractHorse.getDomestication());
                        info.add(abstractHorse.getJumpStrength());
                        info.add(abstractHorse.getMaxDomestication());
                        info.add(null);
                        info.add(null);
                        ItemStack decor;
                        if (entity instanceof Horse) {
                           Horse horse = (Horse)entity;
                           ItemStack armor = horse.getInventory().getArmor();
                           if (armor != null) {
                              info.add(armor.serialize());
                           } else {
                              info.add(null);
                           }

                           decor = horse.getInventory().getSaddle();
                           if (decor != null) {
                              info.add(decor.serialize());
                           } else {
                              info.add(null);
                           }

                           info.add(horse.getColor());
                           info.add(horse.getStyle());
                        } else if (entity instanceof ChestedHorse) {
                           ChestedHorse chestedHorse = (ChestedHorse)entity;
                           info.add(chestedHorse.isCarryingChest());
                           if (entity instanceof Llama) {
                              Llama llama = (Llama)entity;
                              decor = llama.getInventory().getDecor();
                              if (decor != null) {
                                 info.add(decor.serialize());
                              } else {
                                 info.add(null);
                              }

                              info.add(llama.getColor());
                           }
                        }
                     }
                  } else {
                     Villager villager = (Villager)entity;
                     info.add(villager.getProfession());
                     info.add(villager.getRiches());
                     List<Object> recipes = new ArrayList();
                     Iterator var43 = villager.getRecipes().iterator();

                     while(true) {
                        if (!var43.hasNext()) {
                           info.add(recipes);
                           break;
                        }

                        MerchantRecipe merchantRecipe = (MerchantRecipe)var43.next();
                        List<Object> recipe = new ArrayList();
                        List<Object> ingredients = new ArrayList();
                        itemMap = new ArrayList();
                        ItemStack item = merchantRecipe.getResult().clone();
                        List<List<Map<String, Object>>> metadata = Logger.getItemMeta(item, item.getType(), 0);
                        item.setItemMeta(null);
                        itemMap.add(item.serialize());
                        itemMap.add(metadata);
                        recipe.add(itemMap);
                        recipe.add(merchantRecipe.getUses());
                        recipe.add(merchantRecipe.getMaxUses());
                        recipe.add(merchantRecipe.hasExperienceReward());
                        Iterator var60 = merchantRecipe.getIngredients().iterator();

                        while(var60.hasNext()) {
                           ItemStack ingredient = (ItemStack)var60.next();
                           itemMap = new ArrayList();
                           item = ingredient.clone();
                           metadata = Logger.getItemMeta(item, item.getType(), 0);
                           item.setItemMeta(null);
                           itemMap.add(item.serialize());
                           itemMap.add(metadata);
                           ingredients.add(itemMap);
                        }

                        recipe.add(ingredients);
                        recipes.add(recipe);
                     }
                  }
                  data.add(age);
                  data.add(tame);
                  data.add(info);
                  data.add(entity.isCustomNameVisible());
                  data.add(entity.getCustomName());
                  data.add(attributes);
                  if (!(entity instanceof Player)) {
                     Queue.queueEntityKill(e, entity.getLocation(), data, entity_type);
                  } else {
                     Queue.queuePlayerKill(e, entity.getLocation(), entity.getName());
                  }
               }
            }
         }

      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onEntityExplode(EntityExplodeEvent event) {
      Entity entity = event.getEntity();
      World world = event.getLocation().getWorld();
      String user = "#explosion";
      if (entity instanceof TNTPrimed) {
         user = "#tnt";
      } else if (entity instanceof Minecart) {
         String name = entity.getType().name();
         if (name.contains("TNT")) {
            user = "#tnt";
         }
      } else if (entity instanceof Creeper) {
         user = "#creeper";
      } else if (!(entity instanceof EnderDragon) && !(entity instanceof EnderDragonPart)) {
         if (!(entity instanceof Wither) && !(entity instanceof WitherSkull)) {
            if (entity instanceof EnderCrystal) {
               user = "#endercrystal";
            }
         } else {
            user = "#wither";
         }
      } else {
         user = "#enderdragon";
      }

      boolean log = false;
      if (Functions.checkConfig(world, "explosions") == 1) {
         log = true;
      }

      if ((user.equals("#enderdragon") || user.equals("#wither")) && Functions.checkConfig(world, "entity-change") == 0) {
         log = false;
      }

      if (!event.isCancelled() && log) {
         List<Block> b = event.blockList();
         List<Block> nb = new ArrayList();
         Iterator var8;
         Block block;
         if (Functions.checkConfig(world, "natural-break") == 1) {
            var8 = b.iterator();

            while(var8.hasNext()) {
               block = (Block)var8.next();
               int x = block.getX();
               int y = block.getY();
               int z = block.getZ();
               Location l1 = new Location(world, (x + 1), y, z);
               Location l2 = new Location(world, (x - 1), y, z);
               Location l3 = new Location(world, x, y, (z + 1));
               Location l4 = new Location(world, x, y, (z - 1));
               Location l5 = new Location(world, x, (y + 1), z);
               int l = 1;

               for(byte m = 6; l < m; ++l) {
                  Location lc = l1;
                  if (l == 2) {
                     lc = l2;
                  }

                  if (l == 3) {
                     lc = l3;
                  }

                  if (l == 4) {
                     lc = l4;
                  }

                  if (l == 5) {
                     lc = l5;
                  }

                  Block block_t = world.getBlockAt(lc);
                  Material t = block_t.getType();
                  if (BlockInfo.track_any.contains(t) || BlockInfo.track_top.contains(t) || BlockInfo.track_side.contains(t)) {
                     Block bl = world.getBlockAt(lc);
                     nb.add(bl);
                  }
               }
            }
         }

         var8 = b.iterator();

         while(var8.hasNext()) {
            block = (Block)var8.next();
            if (!nb.contains(block)) {
               nb.add(block);
            }
         }

         var8 = nb.iterator();

         while(var8.hasNext()) {
            block = (Block)var8.next();
            Material blockType = block.getType();
            BlockState blockState = block.getState();
            if ((blockType.equals(Material.SIGN_POST) || blockType.equals(Material.WALL_SIGN)) && Functions.checkConfig(world, "sign-text") == 1) {
               try {
                  Sign sign = (Sign)blockState;
                  String line1 = sign.getLine(0);
                  String line2 = sign.getLine(1);
                  String line3 = sign.getLine(2);
                  String line4 = sign.getLine(3);
                  Queue.queueSignText(user, blockState, line1, line2, line3, line4, 5);
               } catch (Exception var24) {
                  var24.printStackTrace();
               }
            }

            Database.containerBreakCheck(user, blockType, block, block.getLocation());
            Queue.queueBlockBreak(user, blockState, blockType, Functions.getData(block));
         }
      }

   }
}
