package net.coreprotect.command;

import net.coreprotect.model.Config;
import org.bukkit.command.CommandSender;

public class InspectCommand {
   protected static void runCommand(CommandSender player, boolean permission, String[] args) {
      if (!permission) {
         player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
         return;
      }

      if (args.length < 2) {
         player.sendMessage("§3CoreProtect §f- Используйте /co inspect [on/off]");
         return;
      }

      String action = args[1].toLowerCase();
      boolean inspecting = Config.inspecting.getOrDefault(player.getName(), false);

      switch (action) {
         case "on":
            if (inspecting) {
               player.sendMessage("§3CoreProtect §f- Слежка уже включена.");
            } else {
               player.sendMessage("§3CoreProtect §f- Слежка включена.");
               Config.inspecting.put(player.getName(), true);
            }
            break;
         case "off":
            if (!inspecting) {
               player.sendMessage("§3CoreProtect §f- Слежка уже отключена.");
            } else {
               player.sendMessage("§3CoreProtect §f- Слежка отключена.");
               Config.inspecting.put(player.getName(), false);
            }
            break;
         default:
            player.sendMessage("§3CoreProtect §f- Неверный формат команды. Используйте /co inspect [on/off]");
      }
   }
}
