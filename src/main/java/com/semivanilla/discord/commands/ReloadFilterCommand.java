package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.RegexFilterManager;
import net.badbird5907.jdacommand.CommandResult;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;

public class ReloadFilterCommand {
    @Command(name = "reloadfilter", description = "Reload regex filter", serverOnly = true, permission = Permission.ADMINISTRATOR)
    public CommandResult reloadFilter(CommandContext ctx) {
        ctx.reply("Reloading filter...");
        long start = System.currentTimeMillis();
        RegexFilterManager.reload();
        long end = System.currentTimeMillis();
        ctx.reply("Filter reloaded in " + (end - start) + "ms");
        return CommandResult.SUCCESS;
    }
}
