package net.coreprotect.command;

import java.util.List;
import net.coreprotect.model.Config;
import org.bukkit.command.CommandSender;

public class CancelCommand {
   protected static void runCommand(CommandSender user, boolean permission, String[] args) {
      try {
         if (Config.last_rollback.get(user.getName()) != null) {
            List<Object[]> list = (List)Config.last_rollback.get(user.getName());
            int time = (Integer)((Object[])list.get(0))[0];
            args = (String[])((String[])list.get(1));
            boolean valid = false;

            for(int i = 0; i < args.length; ++i) {
               if (args[i].equals("#preview")) {
                  valid = true;
                  args[i] = args[i].replaceAll("#preview", "#preview_cancel");
               }
            }

            if (!valid) {
               user.sendMessage("§3CoreProtect §f- Ожидающий откат/восстановление не найден.");
            } else {
               Config.last_rollback.remove(user.getName());
               RollbackRestoreCommand.runCommand(user, permission, args, time);
            }
         } else {
            user.sendMessage("§3CoreProtect §f- Ожидающий откат/восстановление не найден.");
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

   }
}
