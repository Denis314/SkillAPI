package com.sucy.skill.command;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.command.admin.CmdLevel;
import com.sucy.skill.command.admin.CmdReload;
import com.sucy.skill.command.basic.*;
import com.sucy.skill.language.CommandNodes;

/**
 * Handler for class commands
 */
public class ClassCommander extends CommandHandler {

    /**
     * Constructor
     *
     * @param plugin plugin reference
     */
    public ClassCommander(SkillAPI plugin) {
        super(plugin);
    }

    /**
     * Registers the commands
     */
    @Override
    protected void registerCommands() {
        SkillAPI api = (SkillAPI) plugin;

        // Basic commands
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.BIND, false), new CmdBind()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.UNBIND, false), new CmdUnbind()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.CAST, false), new CmdCast()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.INFO, false), new CmdInfo()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.SKILLS, false), new CmdSkills()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.CLASS, false), new CmdClass()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.CLASS, false),
                api.getMessage(CommandNodes.NAME + CommandNodes.PROFESS, false), new CmdProfess()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.CLASS, false),
                api.getMessage(CommandNodes.NAME + CommandNodes.RESET, false), new CmdReset()));

        // Admin commands
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.LEVEL, false), new CmdLevel()));
        registerCommand(new CustomCommand(api.getMessage(CommandNodes.NAME + CommandNodes.CLASS, false),
                api.getMessage(CommandNodes.NAME + CommandNodes.RELOAD, false), new CmdReload()));
    }
}
