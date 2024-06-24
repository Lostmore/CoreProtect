package net.coreprotect.command;

import org.bukkit.command.CommandSender;

public class HelpCommand {
   protected static void runCommand(CommandSender player, boolean permission, String[] args) {
      int resultc = args.length;
      if (permission) {
         if (resultc > 1) {
            String helpcommand_original = args[1];
            String helpcommand = args[1].toLowerCase();
            helpcommand = helpcommand.replaceAll("[^a-zA-Z]", "");
            player.sendMessage("§f----- §3CoreProtect Help §f-----");
            if (helpcommand.equals("help")) {
               player.sendMessage("§3/co help §f- Отображает список всех команд.");
            } else if (!helpcommand.equals("inspect") && !helpcommand.equals("inspector") && !helpcommand.equals("i")) {
               if (!helpcommand.equals("rollback") && !helpcommand.equals("rollbacks") && !helpcommand.equals("rb") && !helpcommand.equals("ro")) {
                  if (!helpcommand.equals("restore") && !helpcommand.equals("restores") && !helpcommand.equals("re") && !helpcommand.equals("rs")) {
                     if (!helpcommand.equals("lookup") && !helpcommand.equals("lookups") && !helpcommand.equals("l")) {
                        if (!helpcommand.equals("params") && !helpcommand.equals("param") && !helpcommand.equals("parameters") && !helpcommand.equals("parameter")) {
                           if (!helpcommand.equals("purge") && !helpcommand.equals("purges")) {
                              if (helpcommand.equals("version")) {
                                 player.sendMessage("§3/co version §f- Показывает версию CoreProtect, которую вы используете.");
                              } else if (!helpcommand.equals("u") && !helpcommand.equals("user") && !helpcommand.equals("users") && !helpcommand.equals("uuser") && !helpcommand.equals("uusers")) {
                                 if (!helpcommand.equals("t") && !helpcommand.equals("time") && !helpcommand.equals("ttime")) {
                                    if (!helpcommand.equals("r") && !helpcommand.equals("radius") && !helpcommand.equals("rradius")) {
                                       if (!helpcommand.equals("a") && !helpcommand.equals("action") && !helpcommand.equals("actions") && !helpcommand.equals("aaction")) {
                                          if (!helpcommand.equals("b") && !helpcommand.equals("block") && !helpcommand.equals("blocks") && !helpcommand.equals("bblock") && !helpcommand.equals("bblocks")) {
                                             if (!helpcommand.equals("e") && !helpcommand.equals("exclude") && !helpcommand.equals("eexclude")) {
                                                player.sendMessage("§fИнформация для команды \"§3/co help " + helpcommand_original + "§f\" не найдена.");
                                             } else {
                                                player.sendMessage("§3/co lookup e:<exclude> §f- Исключение блоков/игроков");
                                                player.sendMessage("§7§oПараметры: [e:stone], [e:Lostmore], e:[stone,Lostmore]");
                                                player.sendMessage("§7§oНаименования блоков: http://minecraft.gamepedia.com/Blocks");
                                             }
                                          } else {
                                             player.sendMessage("§3/co lookup b:<blocks> §f- Ограничьте поиск определенными блоками.");
                                             player.sendMessage("§7§oExamples: [b:stone], [b:stone,wood,bedrock]");
                                             player.sendMessage("§7§oНаименования блоков: http://minecraft.gamepedia.com/Blocks");
                                          }
                                       } else {
                                          player.sendMessage("§3/co lookup a:<action> §f- Ограничьте поиск определенным действием.");
                                          player.sendMessage("§7§oПараметры: [a:block], [a:+block], [a:-block] [a:click], [a:container], [a:kill], [a:chat], [a:command], [a:session], [a:username]");
                                       }
                                    } else {
                                       player.sendMessage("§3/co lookup r:<radius> §f- Укажите область радиуса.");
                                       player.sendMessage("§7§oПараметры: [r:10] (Работает в пределе 10 блоков от вас)");
                                    }
                                 } else {
                                    player.sendMessage("§3/co lookup t:<time> §f- Укажите количество времени для поиска.");
                                    player.sendMessage("§7§oПараметры: [t:2w,5d,7h,2m,10s], [t:5d2h], [t:2.50h]");
                                 }
                              } else {
                                 player.sendMessage("§3/co lookup u:<users> §f- Укажите игроков для поиска.");
                                 player.sendMessage("§7§oПараметры: [u:Lostmore], [u:Lostmore,#enderman]");
                              }
                           } else {
                              player.sendMessage("§3/co purge t:<time> §f- Удалите данные за указанное время.");
                              player.sendMessage("§7§oFor example, \"/co purge t:30d\" удалит все данные больше одного месяца и сохранит данные за последние 30 дней.");
                           }
                        } else {
                           player.sendMessage("§3/co lookup §7<params> §f- Выполните поиск.");
                           player.sendMessage("§3| §7u:<users> §f- Укажите игроков для поиска.");
                           player.sendMessage("§3| §7t:<time> §f- Укажите время для поиска.");
                           player.sendMessage("§3| §7r:<radius> §f- Укажите радиус для поиска..");
                           player.sendMessage("§3| §7a:<action> §f- Ограничьте поиск определенным действием.");
                           player.sendMessage("§3| §7b:<blocks> §f- Ограничьте поиск определенными типами блоков.");
                           player.sendMessage("§3| §7e:<exclude> §f- Исключить тип блоков/игроков.");
                           player.sendMessage("§7§oСмотреть \"/co help <param>\" для подробной информации.");
                        }
                     } else {
                        player.sendMessage("§3/co lookup <params>");
                        player.sendMessage("§3/co l <params> §f- Сокращение команды.");
                        player.sendMessage("§3/co lookup <page> §f- Используйте после проверки блока для просмотра журналов.");
                        player.sendMessage("§7§oСмотреть \"/co help params\" для подробной информации.");
                     }
                  } else {
                     player.sendMessage("§3/co restore §7<params> §f- Выполнить восстановление.");
                     player.sendMessage("§3| §7u:<users> §f- Укажите игроков для восстановления..");
                     player.sendMessage("§3| §7t:<time> §f- Укажите время для восстановления.");
                     player.sendMessage("§3| §7r:<radius> §f- Укажите радиус для восстановления.");
                     player.sendMessage("§3| §7a:<action> §f- Ограничьте восстановление определенным действием.");
                     player.sendMessage("§3| §7b:<blocks> §f- Ограничьте восстановление типом блоков.");
                     player.sendMessage("§3| §7e:<exclude> §f- Исключение блоков/игроков.");
                     player.sendMessage("§7§oСмотреть \"/co help <param>\" для подробной информации.");
                  }
               } else {
                  player.sendMessage("§3/co rollback §7<params> §f- Выполнить откат.");
                  player.sendMessage("§3| §7u:<users> §f- Укажите игроков для отката.");
                  player.sendMessage("§3| §7t:<time> §f- Укажите время для отката.");
                  player.sendMessage("§3| §7r:<radius> §f- Укажите радиус для отката.");
                  player.sendMessage("§3| §7a:<action> §f- Ограничьте откат определенным действием.");
                  player.sendMessage("§3| §7b:<blocks> §f- Ограничьте откат типом блоков.");
                  player.sendMessage("§3| §7e:<exclude> §f- Исключение блоков/игроков.");
                  player.sendMessage("§7§oСмотреть \"/co help <param>\" для подробной информации.");
               }
            } else {
               player.sendMessage("§3При включенной слежке вы можете выполнить следующее:");
               player.sendMessage("* Щелкните левой кнопкой мыши по блоку, чтобы увидеть, кто разместил этот блок.");
               player.sendMessage("* Щелкните правой кнопкой мыши по блоку, чтобы увидеть, какой соседний блок был удален.");
               player.sendMessage("* Поставьте блок, чтобы увидеть, какой блок был удален в этом месте.");
               player.sendMessage("* Поместите в блок жидность, чтобы увидеть кто его поместил.");
               player.sendMessage("* Щелкните правой кнопкой мыши на двери, сундуке и т.д., чтобы увидеть, кто в последний раз пользовался ими.");
               player.sendMessage("§7§oПримечание: Вы можете использовать \"/co i\" для быстрого использования.");
            }
         } else {
            player.sendMessage("§f----- §3CoreProtect Help §f-----");
            player.sendMessage("§3/co help §7<command> §f- Отобразить дополнительную информацию для этой команды.");
            player.sendMessage("§3/co §7inspect §f- Включает или выключает слежку блоков..");
            player.sendMessage("§3/co §7rollback §3<params> §f- Back-up данных.");
            player.sendMessage("§3/co §7restore §3<params> §f- Восстановление данных.");
            player.sendMessage("§3/co §7lookup §3<params> §f- Расширенный поиск.");
            player.sendMessage("§3/co §7purge §3<params> §f- Очистить базу данных");
            player.sendMessage("§3/co §7reload §f- Перезагрузка конфигурации.");
            player.sendMessage("§3/co §7version §f- Выводит текущую версию плагина.");
         }
      } else {
         player.sendMessage("§3CoreProtect §f- У вас нет разрешения на это!");
      }
   }
}
