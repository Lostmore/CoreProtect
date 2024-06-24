package net.coreprotect.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TabHandler implements TabCompleter {

    private static final String[] COMMANDS = {"help", "inspect", "rollback", "restore", "lookup", "purge", "reload", "version"};
    private static final String[] HELP = {"inspect", "rollback", "restore", "lookup", "purge", "params", "users", "time", "radius", "action", "exclude"};
    private static final String[] PARAMS = {"user:", "time:", "radius:", "action:", "exclude:", "#container"};
    private static final String[] ACTIONS = {"block", "+block", "-block", "click", "container", "+container", "-container", "kill", "chat", "command", "session", "+session", "-session", "username"};
    private static final String[] NUMBERS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final String[] TIMES = {"w", "d", "h", "m", "s"};
    private static ArrayList<String> materials = null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || args.length == 0) {
            return null;
        }

        if (cmd.getName().equalsIgnoreCase("co")) {
            if (args.length == 1) {
                return StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), new ArrayList<>());
            } else if ((args.length == 2 || args.length == 3) && (args[0].equalsIgnoreCase("rollback") || args[0].equalsIgnoreCase("restore") || args[0].equalsIgnoreCase("lookup"))) {
                if (args[args.length - 1].startsWith("action:") || args[args.length - 1].startsWith("exclude:")) {
                    return Arrays.asList(ACTIONS);
                } else {
                    return null;
                }
            } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("help")) {
                return StringUtil.copyPartialMatches(args[args.length - 1], Arrays.asList(HELP), new ArrayList<>());
            } else if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("purge")) {
                if (args[args.length - 1].startsWith("time:")) {
                    return StringUtil.copyPartialMatches(args[args.length - 1].substring(5), Arrays.asList(NUMBERS), new ArrayList<>());
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
        return null;
    }
}