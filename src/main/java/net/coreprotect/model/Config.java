package net.coreprotect.model;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.CoreProtect;
import net.coreprotect.Functions;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.patch.Patch;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Config extends Queue {
    public static String driver = "com.mysql.jdbc.Driver";
    public static String sqlite = "plugins/CoreProtect/database.db";
    public static String host = "127.0.0.1";
    public static int port = 3306;
    public static String database = "database";
    public static String username = "root";
    public static String password = "";
    public static String prefix = "co_";
    public static boolean server_running = false;
    public static boolean converter_running = false;
    public static boolean purge_running = false;
    public static int world_id = 0;
    public static int material_id = 0;
    public static int entity_id = 0;
    public static int art_id = 0;
    public static Map<String, Integer> worlds = Collections.synchronizedMap(new HashMap());
    public static Map<Integer, String> worlds_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> materials = Collections.synchronizedMap(new HashMap());
    public static Map<Integer, String> materials_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> entities = Collections.synchronizedMap(new HashMap());
    public static Map<Integer, String> entities_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> art = Collections.synchronizedMap(new HashMap());
    public static Map<Integer, String> art_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> config = Collections.synchronizedMap(new HashMap());
    public static Map<String, int[]> rollback_hash = Collections.synchronizedMap(new HashMap());
    public static Map<String, Boolean> inspecting = Collections.synchronizedMap(new HashMap());
    public static Map<String, Object[]> lookup_cache = Collections.synchronizedMap(new HashMap());
    public static Map<String, Object[]> break_cache = Collections.synchronizedMap(new HashMap());
    public static Map<String, Object[]> piston_cache = Collections.synchronizedMap(new HashMap());
    public static Map<String, Object[]> entity_cache = Collections.synchronizedMap(new HashMap());
    public static Map<String, Boolean> blacklist = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> logging_chest = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<ItemStack[]>> old_container = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<ItemStack[]>> force_containers = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> lookup_type = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> lookup_page = Collections.synchronizedMap(new HashMap());
    public static Map<String, String> lookup_command = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<Object>> lookup_blist = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<Object>> lookup_elist = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<String>> lookup_e_userlist = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<String>> lookup_ulist = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<Integer>> lookup_alist = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer[]> lookup_radius = Collections.synchronizedMap(new HashMap());
    public static Map<String, String> lookup_time = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> lookup_rows = Collections.synchronizedMap(new HashMap());
    public static Map<String, String> uuid_cache = Collections.synchronizedMap(new HashMap());
    public static Map<String, String> uuid_cache_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, Integer> player_id_cache = Collections.synchronizedMap(new HashMap());
    public static Map<Integer, String> player_id_cache_reversed = Collections.synchronizedMap(new HashMap());
    public static Map<String, List<Object[]>> last_rollback = Collections.synchronizedMap(new HashMap());
    public static Map<String, Boolean> active_rollbacks = Collections.synchronizedMap(new HashMap());
    public static Map<UUID, Object[]> entity_block_mapper = Collections.synchronizedMap(new HashMap());
    public static ConcurrentHashMap<String, String> language = new ConcurrentHashMap();
    public static List<String> databaseTables = new ArrayList();

    private static void checkPlayers(Connection connection) {
        player_id_cache.clear();
        Iterator var1 = CoreProtect.getInstance().getServer().getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player player = (Player)var1.next();
            if (player_id_cache.get(player.getName().toLowerCase()) == null) {
                Database.loadUserID(connection, player.getName(), player.getUniqueId().toString());
            }
        }

    }

    private static void loadBlacklist() {
        try {
            Config.blacklist.clear();
            String blacklist = "plugins/CoreProtect/blacklist.txt";
            boolean exists = (new File(blacklist)).exists();
            if (exists) {
                RandomAccessFile blfile = new RandomAccessFile(blacklist, "rw");
                long blc = blfile.length();
                if (blc > 0L) {
                    while(blfile.getFilePointer() < blfile.length()) {
                        String blacklist_user = blfile.readLine().replaceAll(" ", "").toLowerCase();
                        if (blacklist_user.length() > 0) {
                            Config.blacklist.put(blacklist_user, true);
                        }
                    }
                }

                blfile.close();
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private static void loadConfig() {
        try {
            String confighead = "#CoreProtect Config\n";
            config.clear();
            File config_file = new File("plugins/CoreProtect/config.yml");
            boolean exists = config_file.exists();
            if (!exists) {
                config_file.createNewFile();
            }

            File dir = new File("plugins/CoreProtect");
            String[] children = dir.list();
            if (children != null) {
                String[] var41 = children;
                int var42 = children.length;

                for(int var43 = 0; var43 < var42; ++var43) {
                    String element = var41[var43];
                    String filename = element;
                    if (!element.startsWith(".") && element.endsWith(".yml")) {
                        try {
                            String key = filename.replaceAll(".yml", "-");
                            if (key.equals("config-")) {
                                key = "";
                            }

                            RandomAccessFile configfile = new RandomAccessFile("plugins/CoreProtect/" + filename, "rw");
                            long config_length = configfile.length();
                            if (config_length > 0L) {
                                while(configfile.getFilePointer() < configfile.length()) {
                                    String line = configfile.readLine();
                                    if (line.contains(":") && !line.startsWith("#")) {
                                        line = line.replaceFirst(":", "§ ");
                                        String[] i2 = line.split("§");
                                        String option = i2[0].trim().toLowerCase();
                                        String setting;
                                        if (key.length() == 0) {
                                            if (option.equals("verbose")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put(key + "verbose", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("verbose", 0);
                                                }
                                            }

                                            if (option.equals("use-mysql")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put("use-mysql", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("use-mysql", 0);
                                                }
                                            }

                                            if (option.equals("table-prefix")) {
                                                prefix = i2[1].trim();
                                            }

                                            if (option.equals("mysql-host")) {
                                                host = i2[1].trim();
                                            }

                                            if (option.equals("mysql-port")) {
                                                setting = i2[1].trim();
                                                setting = setting.replaceAll("[^0-9]", "");
                                                if (setting.length() == 0) {
                                                    setting = "0";
                                                }

                                                port = Integer.parseInt(setting);
                                            }

                                            if (option.equals("mysql-database")) {
                                                database = i2[1].trim();
                                            }

                                            if (option.equals("mysql-username")) {
                                                username = i2[1].trim();
                                            }

                                            if (option.equals("mysql-password")) {
                                                password = i2[1].trim();
                                            }

                                            if (option.equals("check-updates")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put("check-updates", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("check-updates", 0);
                                                }
                                            }

                                            if (option.equals("api-enabled")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put("api-enabled", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("api-enabled", 0);
                                                }
                                            }

                                            if (option.equals("default-radius")) {
                                                setting = i2[1].trim();
                                                setting = setting.replaceAll("[^0-9]", "");
                                                if (setting.length() == 0) {
                                                    setting = "0";
                                                }

                                                config.put("default-radius", Integer.parseInt(setting));
                                            }

                                            if (option.equals("max-radius")) {
                                                setting = i2[1].trim();
                                                setting = setting.replaceAll("[^0-9]", "");
                                                if (setting.length() == 0) {
                                                    setting = "0";
                                                }

                                                config.put("max-radius", Integer.parseInt(setting));
                                            }

                                            if (option.equals("rollback-items")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put("rollback-items", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("rollback-items", 0);
                                                }
                                            }

                                            if (option.equals("rollback-entities")) {
                                                setting = i2[1].trim().toLowerCase();
                                                if (setting.startsWith("t")) {
                                                    config.put("rollback-entities", 1);
                                                } else if (setting.startsWith("f")) {
                                                    config.put("rollback-entities", 0);
                                                }
                                            }
                                        }

                                        if (option.equals("skip-generic-data")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "skip-generic-data", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "skip-generic-data", 0);
                                            }
                                        }

                                        if (option.equals("block-place")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "block-place", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "block-place", 0);
                                            }
                                        }

                                        if (option.equals("block-break")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "block-break", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "block-break", 0);
                                            }
                                        }

                                        if (option.equals("natural-break")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "natural-break", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "natural-break", 0);
                                            }
                                        }

                                        if (option.equals("block-movement")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "block-movement", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "block-movement", 0);
                                            }
                                        }

                                        if (option.equals("pistons")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "pistons", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "pistons", 0);
                                            }
                                        }

                                        if (option.equals("block-burn")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "block-burn", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "block-burn", 0);
                                            }
                                        }

                                        if (option.equals("block-ignite")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "block-ignite", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "block-ignite", 0);
                                            }
                                        }

                                        if (option.equals("explosions")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "explosions", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "explosions", 0);
                                            }
                                        }

                                        if (option.equals("entity-change")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "entity-change", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "entity-change", 0);
                                            }
                                        }

                                        if (option.equals("entity-kills")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "entity-kills", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "entity-kills", 0);
                                            }
                                        }

                                        if (option.equals("sign-text")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "sign-text", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "sign-text", 0);
                                            }
                                        }

                                        if (option.equals("buckets")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "buckets", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "buckets", 0);
                                            }
                                        }

                                        if (option.equals("leaf-decay")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "leaf-decay", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "leaf-decay", 0);
                                            }
                                        }

                                        if (option.equals("tree-growth")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "tree-growth", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "tree-growth", 0);
                                            }
                                        }

                                        if (option.equals("mushroom-growth")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "mushroom-growth", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "mushroom-growth", 0);
                                            }
                                        }

                                        if (option.equals("vine-growth")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "vine-growth", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "vine-growth", 0);
                                            }
                                        }

                                        if (option.equals("portals")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "portals", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "portals", 0);
                                            }
                                        }

                                        if (option.equals("water-flow")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "water-flow", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "water-flow", 0);
                                            }
                                        }

                                        if (option.equals("lava-flow")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "lava-flow", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "lava-flow", 0);
                                            }
                                        }

                                        if (option.equals("liquid-tracking")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "liquid-tracking", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "liquid-tracking", 0);
                                            }
                                        }

                                        if (option.equals("item-transactions")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "item-transactions", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "item-transactions", 0);
                                            }
                                        }

                                        if (option.equals("player-interactions")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "player-interactions", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "player-interactions", 0);
                                            }
                                        }

                                        if (option.equals("player-messages")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "player-messages", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "player-messages", 0);
                                            }
                                        }

                                        if (option.equals("player-commands")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "player-commands", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "player-commands", 0);
                                            }
                                        }

                                        if (option.equals("player-sessions")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "player-sessions", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "player-sessions", 0);
                                            }
                                        }

                                        if (option.equals("username-changes")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "username-changes", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "username-changes", 0);
                                            }
                                        }

                                        if (option.equals("worldedit")) {
                                            setting = i2[1].trim().toLowerCase();
                                            if (setting.startsWith("t")) {
                                                config.put(key + "worldedit", 1);
                                            } else if (setting.startsWith("f")) {
                                                config.put(key + "worldedit", 0);
                                            }
                                        }
                                    }
                                }
                            }

                            if (key.length() == 0) {
                                if (config_length < 1L) {
                                    configfile.write(confighead.getBytes());
                                }

                                if (config.get("verbose") == null) {
                                    config.put("verbose", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("use-mysql") == null) {
                                    config.put("use-mysql", 0);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("check-updates") == null) {
                                    config.put("check-updates", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("api-enabled") == null) {
                                    config.put("api-enabled", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("default-radius") == null) {
                                    config.put("default-radius", 10);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("max-radius") == null) {
                                    config.put("max-radius", 100);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("rollback-items") == null) {
                                    config.put("rollback-items", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("rollback-entities") == null) {
                                    config.put("rollback-entities", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("skip-generic-data") == null) {
                                    config.put("skip-generic-data", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("block-place") == null) {
                                    config.put("block-place", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("block-break") == null) {
                                    config.put("block-break", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("natural-break") == null) {
                                    config.put("natural-break", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("block-movement") == null) {
                                    config.put("block-movement", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("pistons") == null) {
                                    config.put("pistons", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("block-burn") == null) {
                                    config.put("block-burn", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("block-ignite") == null) {
                                    config.put("block-ignite", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("explosions") == null) {
                                    config.put("explosions", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("entity-change") == null) {
                                    config.put("entity-change", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("entity-kills") == null) {
                                    config.put("entity-kills", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("sign-text") == null) {
                                    config.put("sign-text", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("buckets") == null) {
                                    config.put("buckets", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("leaf-decay") == null) {
                                    config.put("leaf-decay", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("tree-growth") == null) {
                                    config.put("tree-growth", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("mushroom-growth") == null) {
                                    config.put("mushroom-growth", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("vine-growth") == null) {
                                    config.put("vine-growth", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("portals") == null) {
                                    config.put("portals", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("water-flow") == null) {
                                    config.put("water-flow", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("lava-flow") == null) {
                                    config.put("lava-flow", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("liquid-tracking") == null) {
                                    config.put("liquid-tracking", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("item-transactions") == null) {
                                    config.put("item-transactions", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("player-interactions") == null) {
                                    config.put("player-interactions", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("player-messages") == null) {
                                    config.put("player-messages", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("player-commands") == null) {
                                    config.put("player-commands", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("player-sessions") == null) {
                                    config.put("player-sessions", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("username-changes") == null) {
                                    config.put("username-changes", 1);
                                    configfile.seek(configfile.length());
                                }

                                if (config.get("worldedit") == null) {
                                    config.put("worldedit", 1);
                                    configfile.seek(configfile.length());
                                }
                            }

                            configfile.close();
                        } catch (Exception var18) {
                            var18.printStackTrace();
                        }
                    }
                }
            }

            if ((Integer)config.get("use-mysql") == 0) {
                prefix = "co_";
            }

            loadBlacklist();
        } catch (Exception var19) {
            var19.printStackTrace();
        }

    }

    public static void loadDatabase() {
        if ((Integer)config.get("use-mysql") == 0) {
            try {
                File tempFile = File.createTempFile("CoreProtect_" + System.currentTimeMillis(), ".tmp");
                tempFile.setExecutable(true);
                if (!tempFile.canExecute()) {
                    File tempFolder = new File("cache");
                    boolean exists = tempFolder.exists();
                    if (!exists) {
                        tempFolder.mkdir();
                    }

                    System.setProperty("java.io.tmpdir", "cache");
                }

                tempFile.delete();
            } catch (Exception var3) {
                var3.printStackTrace();
            }
        }

        Functions.createDatabaseTables(prefix, false);
    }

    private static void loadTypes(Statement statement) {
        try {
            materials.clear();
            materials_reversed.clear();
            material_id = 0;
            String query = "SELECT id,material FROM " + prefix + "material_map";
            ResultSet rs = statement.executeQuery(query);

            int id;
            String entity;
            while(rs.next()) {
                id = rs.getInt("id");
                entity = rs.getString("material");
                materials.put(entity, id);
                materials_reversed.put(id, entity);
                if (id > material_id) {
                    material_id = id;
                }
            }

            rs.close();
            art.clear();
            art_reversed.clear();
            art_id = 0;
            query = "SELECT id,art FROM " + prefix + "art_map";
            rs = statement.executeQuery(query);

            while(rs.next()) {
                id = rs.getInt("id");
                entity = rs.getString("art");
                art.put(entity, id);
                art_reversed.put(id, entity);
                if (id > art_id) {
                    art_id = id;
                }
            }

            rs.close();
            entities.clear();
            entities_reversed.clear();
            entity_id = 0;
            query = "SELECT id,entity FROM " + prefix + "entity_map";
            rs = statement.executeQuery(query);

            while(rs.next()) {
                id = rs.getInt("id");
                entity = rs.getString("entity");
                entities.put(entity, id);
                entities_reversed.put(id, entity);
                if (id > entity_id) {
                    entity_id = id;
                }
            }

            rs.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        BlockInfo.loadData();
    }

    private static void loadWorlds(Statement statement) {
        try {
            Config.worlds.clear();
            worlds_reversed.clear();
            world_id = 0;
            String query = "SELECT id,world FROM " + prefix + "world";
            ResultSet rs = statement.executeQuery(query);

            while(rs.next()) {
                int id = rs.getInt("id");
                String world = rs.getString("world");
                Config.worlds.put(world, id);
                worlds_reversed.put(id, world);
                if (id > world_id) {
                    world_id = id;
                }
            }

            List<World> worlds = CoreProtect.getInstance().getServer().getWorlds();
            Iterator var10 = worlds.iterator();

            while(var10.hasNext()) {
                World world = (World)var10.next();
                String worldname = world.getName();
                if (Config.worlds.get(worldname) == null) {
                    int id = world_id + 1;
                    Config.worlds.put(worldname, id);
                    worlds_reversed.put(id, worldname);
                    world_id = id;
                    Queue.queueWorldInsert(id, worldname);
                }
            }
        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }

    public static boolean performInitialization() {
        try {
            loadConfig();
            CoreProtect.getInstance().loadConfig();
            loadDatabase();
            Connection connection = Database.getConnection(true);
            Statement statement = connection.createStatement();
            checkPlayers(connection);
            loadWorlds(statement);
            loadTypes(statement);
            if (Functions.checkWorldEdit()) {
                Functions.loadWorldEdit();
            }

            server_running = true;
            boolean validVersion = Patch.versionCheck(statement);
            statement.close();
            connection.close();
            return validVersion;
        } catch (Exception var3) {
            var3.printStackTrace();
            return false;
        }
    }
}