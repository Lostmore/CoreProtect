package net.coreprotect.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import net.coreprotect.database.Database;
import net.coreprotect.model.Config;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RenameCommand extends Config {
   protected static void runCommand(final CommandSender player, boolean permission, String[] args) {
      int resultc = args.length;
      if (Config.converter_running) {
         player.sendMessage("§3CoreProtect §f- Upgrade in progress. Please try again later.");
      } else if (Config.purge_running) {
         player.sendMessage("§3CoreProtect §f- Выполняется очистка. Пожалуйста, повторите попытку позже.");
      } else {
         if (permission) {
            if (player instanceof Player) {
               player.sendMessage("§3CoreProtect §f- Эта команда должна использоваться через консоль.");
               return;
            }

            if (resultc <= 2) {
               player.sendMessage("§3CoreProtect §f- Используйте \"/co rename <args>\".");
               return;
            }

            String rename_command = args[1].toLowerCase();
            if (!rename_command.equals("world")) {
               player.sendMessage("§3CoreProtect §f- Используйте \"/co rename <args>\".");
               return;
            }

            if (resultc > 3) {
               final String old_world = args[2];
               final String new_world = args[3];

               class BasicThread implements Runnable {
                  public void run() {
                     try {
                        Connection connection = Database.getConnection(false);
                        if (connection == null) {
                           player.sendMessage("§3CoreProtect §f- База данных занята, попробуйте позже.");
                           return;
                        }

                        int wid = -1;
                        PreparedStatement preparedStmt = connection.prepareStatement("SELECT rowid FROM " + Config.prefix + "world WHERE world LIKE ?");
                        preparedStmt.setString(1, old_world);

                        ResultSet rs;
                        for(rs = preparedStmt.executeQuery(); rs.next(); wid = rs.getInt("rowid")) {
                        }

                        rs.close();
                        if (wid == -1) {
                           player.sendMessage("§3CoreProtect §f- Мир \"" + old_world + "\" не найден.");
                           connection.close();
                           return;
                        }

                        preparedStmt = connection.prepareStatement("UPDATE " + Config.prefix + "world SET world = ? WHERE rowid='" + wid + "'");
                        preparedStmt.setString(1, new_world);
                        preparedStmt.executeUpdate();
                        Statement statement = connection.createStatement();
                        statement.close();
                        connection.close();
                        player.sendMessage("§3CoreProtect §f- World \"" + old_world + "\" renamed to \"" + new_world + "\".");
                     } catch (Exception var6) {
                        var6.printStackTrace();
                     }

                  }
               }

               Runnable runnable = new BasicThread();
               Thread thread = new Thread(runnable);
               thread.start();
            } else {
               player.sendMessage("§3CoreProtect §f- Используйте \"/co rename world <old> <new>\".");
            }
         } else {
            player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это.");
         }

      }
   }
}
