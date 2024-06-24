package net.coreprotect.command;

import net.coreprotect.model.Config;
import net.coreprotect.thread.CheckUpdate;
import net.coreprotect.CoreProtect;;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadCommand {
   public static void runCommand(CommandSender sender, boolean permission, String[] args) {
      if (permission) {
         Plugin plugin = CoreProtect.getInstance();
         if (plugin.isEnabled()) {
            sender.sendMessage("§3CoreProtect §f- Полная перезагрузка плагина начата...");
            plugin.getServer().getScheduler().cancelTasks(plugin);

            plugin.onDisable();
            plugin.onEnable();
            Config.performInitialization();

            if (Config.config.get("check-updates") == 1) {
               Thread checkUpdateThread = new Thread(new CheckUpdate(false));
               checkUpdateThread.start();
            }

            sender.sendMessage("§3CoreProtect §f- Плагин полностью перезагружен.");
         } else {
            sender.sendMessage("§3CoreProtect §f- Плагин отключен и не может быть перезагружен.");
         }
      } else {
         sender.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
      }
   }
}

