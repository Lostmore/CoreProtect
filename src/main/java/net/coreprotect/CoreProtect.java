package net.coreprotect;

import net.coreprotect.bukkit.MetricsLite;
import net.coreprotect.command.CommandHandler;
import net.coreprotect.command.TabHandler;
import net.coreprotect.consumer.Consumer;
import net.coreprotect.consumer.Process;
import net.coreprotect.listener.*;
import net.coreprotect.model.Config;
import net.coreprotect.thread.CacheCleanUp;
import net.coreprotect.thread.CheckUpdate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class CoreProtect extends JavaPlugin {

   private static String botToken;
   private static String chatId;

   static {
      try (FileInputStream fis = new FileInputStream("token.env")) {
         Properties properties = new Properties();
         properties.load(fis);
         botToken = properties.getProperty("BOT_TOKEN");
         chatId = properties.getProperty("CHAT_ID");
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static final Map<Material, Integer> suspiciousMiningThresholds = new HashMap<>();
   private static final Map<Material, Integer> notificationIntervals = new HashMap<>();
   private static final Map<Material, Integer> miningTimers = new HashMap<>();
   private Map<String, XrayListener> listeners = new HashMap<>();
   private Map<String, Boolean> trackedPlayers = new HashMap<>();
   private CoreProtectAPI api = new CoreProtectAPI();
   private Listener XrayListener;
   private Listener trackerListener;

   public static CoreProtect getInstance() {
      return CoreProtect.getPlugin(CoreProtect.class);
   }

   public CoreProtectAPI getAPI() {
      return this.api;
   }

   private static boolean performVersionChecks() {
      try {
         String requiredBukkitVersion = "1.11";
         String[] bukkitVersion = getInstance().getServer().getBukkitVersion().split("-|\\.");
         if (Functions.newVersion(bukkitVersion[0] + "." + bukkitVersion[1], requiredBukkitVersion)) {
            System.out.println("[CoreProtect] Spigot " + requiredBukkitVersion + " or higher is required.");
            return false;
         } else {
            String requiredJavaVersion = "1.8";
            String[] javaVersion = (System.getProperty("java.version") + ".0").split("\\.");
            if (Functions.newVersion(javaVersion[0] + "." + javaVersion[1], requiredJavaVersion)) {
               System.out.println("[CoreProtect] Java " + requiredJavaVersion + " or higher is required.");
               return false;
            } else {
               return true;
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
         return false;
      }
   }

   public void onEnable() {
      boolean start = performVersionChecks();
      if (start) {
         try {
            loadConfig();
            Consumer.initialize();
            this.getServer().getPluginManager().registerEvents(new BlockListener(), this);
            this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
            XrayListener = new XrayListener(botToken, chatId, this);
            this.getServer().getPluginManager().registerEvents(XrayListener, this);
            this.getServer().getPluginManager().registerEvents(new EntityListener(), this);
            this.getServer().getPluginManager().registerEvents(new HangingListener(), this);
            trackerListener = new TrackerListener(botToken, chatId, this);
            this.getServer().getPluginManager().registerEvents(trackerListener, this);
            this.getServer().getPluginManager().registerEvents(new WorldListener(), this);
            this.getCommand("coreprotect").setExecutor(CommandHandler.getInstance());
            getCommand("coreprotect").setTabCompleter(new TabHandler());
            this.getCommand("core").setExecutor(CommandHandler.getInstance());
            getCommand("core").setTabCompleter(new TabHandler());
            this.getCommand("co").setExecutor(CommandHandler.getInstance());
            getCommand("co").setTabCompleter(new TabHandler());

            boolean exists = (new File("plugins/CoreProtect/")).exists();
            if (!exists) {
               (new File("plugins/CoreProtect")).mkdir();
            }
            start = Config.performInitialization();
            ((TrackerListener) trackerListener).initializeTrackedPlayers();
         } catch (Exception var7) {
            var7.printStackTrace();
            start = false;
         }
      }

      if (start) {
         System.out.println("[CoreProtect] has been successfully enabled!");
         if (Config.config.get("use-mysql") == 1) {
            System.out.println("[CoreProtect] Using MySQL for data storage.");
         } else {
            System.out.println("[CoreProtect] Using SQLite for data storage.");
         }

         Thread cacheCleanUpThread;
         if (Config.config.get("check-updates") == 1) {
            cacheCleanUpThread = new Thread(new CheckUpdate(true));
            cacheCleanUpThread.start();
         }

         cacheCleanUpThread = new Thread(new CacheCleanUp());
         cacheCleanUpThread.start();
         Thread consumerThread = new Thread(new Consumer());
         consumerThread.start();

         try {
            new MetricsLite(this);
         } catch (Exception var6) {
         }

         int delayDelete = 20 * 60 * 60 * 24;
         Bukkit.getScheduler().runTaskTimer(this, () ->
                 Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "co purge t:1d"), delayDelete, delayDelete);

      } else {
         System.out.println("[CoreProtect] was unable to start.");
         this.getServer().getPluginManager().disablePlugin(this);
      }
   }

   public void loadConfig() {
      File pluginFolder = getDataFolder();
      if (!pluginFolder.exists()) {
         pluginFolder.mkdirs();
      }
      File configFile = new File(pluginFolder, "config.yml");

      YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

      miningTimers.put(Material.DIAMOND_ORE, config.getInt("miningTimers.DIAMOND_ORE"));
      miningTimers.put(Material.GOLD_ORE, config.getInt("miningTimers.GOLD_ORE"));
      miningTimers.put(Material.EMERALD_ORE, config.getInt("miningTimers.EMERALD_ORE"));

      // Порог руды для отправки уведомления
      suspiciousMiningThresholds.put(Material.DIAMOND_ORE, config.getInt("suspiciousMiningThresholds.DIAMOND_ORE"));
      suspiciousMiningThresholds.put(Material.GOLD_ORE, config.getInt("suspiciousMiningThresholds.GOLD_ORE"));
      suspiciousMiningThresholds.put(Material.EMERALD_ORE, config.getInt("suspiciousMiningThresholds.EMERALD_ORE"));

      // Интервал уведомлений
      notificationIntervals.put(Material.DIAMOND_ORE, config.getInt("notificationIntervals.DIAMOND_ORE"));
      notificationIntervals.put(Material.GOLD_ORE, config.getInt("notificationIntervals.GOLD_ORE"));
      notificationIntervals.put(Material.EMERALD_ORE, config.getInt("notificationIntervals.EMERALD_ORE"));
   }
   public void onDisable() {
      safeShutdown();
   }

   private static void safeShutdown() {
      try {
         int time_start = (int)(System.currentTimeMillis() / 1000L);
         boolean processConsumer = Config.server_running;
         if (Config.converter_running) {
            processConsumer = false;
         }

         boolean message_shown = false;

         for(Config.server_running = false; (Consumer.isRunning() || Config.converter_running) && !Config.purge_running; Thread.sleep(1L)) {
            int time = (int)(System.currentTimeMillis() / 1000L);
            if (time > time_start && !message_shown) {
               if (Config.converter_running) {
                  Functions.messageOwner("Finishing up data conversion. Please wait...");
               } else {
                  Functions.messageOwner("Finishing up data logging. Please wait...");
               }
               message_shown = true;
            }
         }

         if (message_shown) {
            System.out.println("[CoreProtect] Success! Resuming server shutdown.");
         }

         if (processConsumer) {
            Process.processConsumer(Consumer.current_consumer);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public Map<Material, Integer> getSuspiciousMiningThresholds() {
      return suspiciousMiningThresholds;
   }

   public Map <Material, Integer> getMiningTimers() {
      return miningTimers;
   }

   public Map<Material, Integer> getNotificationIntervals() {
      return notificationIntervals;
   }
}
