package net.coreprotect.command;

import java.util.List;
import net.coreprotect.model.Config;
import org.bukkit.command.CommandSender;

public class UndoCommand {
   protected static void runCommand(CommandSender user, boolean permission, String[] args) {
      try {
         if (Config.last_rollback.get(user.getName()) != null) {
            List<Object[]> list = (List)Config.last_rollback.get(user.getName());
            int time = (Integer)((Object[])list.get(0))[0];
            args = (String[])((String[])list.get(1));
            String[] var5 = args;
            int var6 = args.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               String arg = var5[var7];
               if (arg.equals("#preview")) {
                  CancelCommand.runCommand(user, permission, args);
                  return;
               }
            }

            boolean valid = true;
            if (!args[0].equals("rollback") && !args[0].equals("rb") && !args[0].equals("ro")) {
               if (!args[0].equals("restore") && !args[0].equals("rs") && !args[0].equals("re")) {
                  valid = false;
               } else {
                  args[0] = "rollback";
               }
            } else {
               args[0] = "restore";
            }

            if (valid) {
               Config.last_rollback.remove(user.getName());
               RollbackRestoreCommand.runCommand(user, permission, args, time);
            }
         } else {
            user.sendMessage("§3CoreProtect §f- Предыдущий откат/восстановление не найден.");
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

   }
}
