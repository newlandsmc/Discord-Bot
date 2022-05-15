package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.MarketManager;
import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.manager.RegexFilterManager;
import com.semivanilla.discord.manager.RoleManager;
import net.badbird5907.jdacommand.CommandResult;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;

public class ReloadCommand {
    @Command(name = "reload", description = "Reload Bot", permission = Permission.ADMINISTRATOR)
    public CommandResult reloadFilter(CommandContext ctx) {
        ctx.reply("Reloading...");
        long start = System.currentTimeMillis();
        RegexFilterManager.reload();
        ModerationManager.save();
        RoleManager.reload();
        long end = System.currentTimeMillis();
        ctx.setOriginal("Reloaded in " + (end - start) + "ms");
        return CommandResult.SUCCESS;
    }
}
