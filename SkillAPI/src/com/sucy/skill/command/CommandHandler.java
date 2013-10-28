package com.sucy.skill.command;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.util.TextSizer;
import com.sucy.skill.language.CommandNodes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * <p>Command organizer and executor imported from MCCore</p>
 */
public abstract class CommandHandler implements CommandExecutor {

    protected static final String BREAK = ChatColor.STRIKETHROUGH + "" + ChatColor.DARK_GRAY + TextSizer.createLine("", "", "-");
    /**
     * List of registered commands
     */
    public final List<CustomCommand> commands = new ArrayList();
    /**
     * Plugin reference
     */
    protected final SkillAPI plugin;

    /**
     * Constructor
     *
     * @param plugin plugin reference
     * @param title usage title
     * @param command command label
     */
    public CommandHandler(SkillAPI plugin) {
        this.plugin = plugin;
        registerCommands();
        String[] cmds = {"bind", "unbind", "use", "skills", "info", "class"};
        for (String s : cmds) {
            PluginCommand cmd = ((JavaPlugin) plugin).getCommand(s);
            if (cmd != null) {
                cmd.setExecutor(this);
            }
        }
    }

    /**
     * @return plugin reference
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Registers a new sub-command
     *
     * @param command command prefix
     * @param executor handler for the command
     */
    protected void registerCommand(CustomCommand cmd) {
        commands.add(cmd);
    }

    /**
     * Called on a command
     *
     * @param sender sender of the command
     * @param cmd command executed
     * @param label command label
     * @param args command arguments
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            for (CustomCommand c : commands) {
                if (cmd.getName().equalsIgnoreCase(c.getName()) && !c.hasLabel()) {
                    if (sender.hasPermission(c.getExecutor().getPermissionNode())) {
                        c.getExecutor().execute(this, plugin, sender, args);
                    } else {
                        sender.sendMessage(plugin.getMessage(CommandNodes.NOT_PERMITTED, true));
                    }
                }
            }
        } else {
            for (CustomCommand c : commands) {
                if (cmd.getName().equalsIgnoreCase(c.getName()) && c.hasLabel() && args[0].equalsIgnoreCase(c.getLabel())) {
                    if (sender.hasPermission(c.getExecutor().getPermissionNode())) {
                        args = trimArgs(args);
                        c.getExecutor().execute(this, plugin, sender, args);
                    } else {
                        sender.sendMessage(plugin.getMessage(CommandNodes.NOT_PERMITTED, true));
                    }
                }
            }
        }
        // Use custom command usage
        return true;
    }

    /**
     * Trims the first element off of args
     *
     * @param args initial args
     * @return trimmed args
     */
    public String[] trimArgs(String[] args) {

        // Can't trim a zero-length array
        if (args.length == 0) {
            return args;
        }

        // Make a new array that is one smaller in size
        String[] trimmed = new String[args.length - 1];

        // Copy the array over if there are elements left
        if (trimmed.length > 0) {
            System.arraycopy(args, 1, trimmed, 0, trimmed.length);
        }

        // Return the new array
        return trimmed;
    }

    /**
     * Displays the command usage - If you want custom displays, override the
     * method with the page argument -
     *
     * @param sender sender of the command
     */
    protected void displayUsage(CommandSender sender) {
        displayUsage(sender, 1);
    }

    /**
     * Displays the command usage Can be overridden for custom displays
     *
     * @param sender sender of the command
     * @param page page number
     */
    protected void displayUsage(CommandSender sender, int page) {
        if (page < 1) {
            page = 1;
        }

        // Get the key set alphabetized
        List<CustomCommand> keys = new ArrayList();
        for (CustomCommand c : commands) {
            if (canUseCommand(sender, c.getExecutor())) {
                keys.add(c);
            }
        }

        if (keys.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "   No commands available");
            return;
        }

        int maxPage = (keys.size() + 6) / 7;
        if (page > maxPage) {
            page = maxPage;
        }

        // Get command syntax and his maximum length
        TreeMap<String, CustomCommand> newKeys = new TreeMap();
        int maxSize = 0;
        int index = 0;
        for (CustomCommand c : keys) {
            String s = ChatColor.GOLD + "/" + c.getName() + ' ';
            if (c.hasLabel()) {
                s = s + c.getLabel() + ' ';
            }
            s = s + ChatColor.LIGHT_PURPLE + c.getExecutor().getArgsString(plugin);
            index++;
            if (index <= (page - 1) * 7 || index > page * 7) {
                continue;
            }
            int size = TextSizer.measureString(s);
            if (size > maxSize) {
                maxSize = size;
            }
            newKeys.put(s, c);
        }
        Set<Entry<String, CustomCommand>> commSet = newKeys.entrySet();
        maxSize += 4;

        sender.sendMessage(BREAK);
        sender.sendMessage(ChatColor.DARK_GREEN + "SkillAPI help" + (maxPage > 1 ? " (Page " + page + "/" + maxPage + ")" : ""));

        // Display usage, squaring everything up nicely
        index = 0;
        for (Entry<String, CustomCommand> key : commSet) {
            index++;
            if (index <= (page - 1) * 7 || index > page * 7) {
                continue;
            }
            sender.sendMessage(TextSizer.expand(key.getKey() + ChatColor.GRAY, maxSize, false)
                    + ChatColor.GRAY + " - " + key.getValue().getExecutor().getDescription(plugin));
        }

        sender.sendMessage(BREAK);
    }

    public void displayUsage(CommandSender sender, ICommand comm) {
        for (CustomCommand c : commands) {
            if (c.getExecutor() == comm) {
                String s = ChatColor.GOLD + "/" + c.getName() + ' ';
                if (c.hasLabel()) {
                    s = s + c.getLabel() + ' ';
                }
                s = s + ChatColor.LIGHT_PURPLE + c.getExecutor().getArgsString(plugin) + ChatColor.GRAY + " - "
                        + c.getExecutor().getDescription(plugin);
                sender.sendMessage(s);
            }
        }
    }

    /**
     * Checks whether or not a command sender can use a certain command - Can be
     * overridden for custom checks -
     *
     * @param sender sender of the command
     * @param command command to check
     * @return true if able to use it, false otherwise
     */
    protected boolean canUseCommand(CommandSender sender, ICommand command) {
        boolean correctType = true;
        if (command.getSenderType() == SenderType.PLAYER_ONLY && !(sender instanceof Player)) {
            correctType = false;
        }
        if (command.getSenderType() == SenderType.CONSOLE_ONLY && sender instanceof Player) {
            correctType = false;
        }
        return sender.hasPermission(command.getPermissionNode()) && correctType;
    }

    protected abstract void registerCommands();
}
