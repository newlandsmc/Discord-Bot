package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.ModerationManager;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;

public class BanCommand {
    @Command(name = "ban", description = "Bans a user from the server.", serverOnly = true, permission = Permission.BAN_MEMBERS)
    public void ban(CommandContext ctx, @Required Member member, @Required String reason, boolean delMessages) {
        ModerationManager.ban(member, reason, ctx.getMember().getUser().getAsTag(), delMessages);
        ctx.reply("Banned " + member.getUser().getAsTag() + " for `" + reason + "`");
    }

    @Command(name = "tempban", description = "Temporarily bans a user from the server.", serverOnly = true, permission = Permission.BAN_MEMBERS)
    public void tempban(CommandContext ctx, @Required Member member, @Required String reason, boolean delMessages, int days, int hours) {
        Duration duration = Duration.ofDays(days).plusHours(hours);
        ModerationManager.ban(member, reason, duration, ctx.getMember().getUser().getAsTag(), delMessages);
        ctx.reply("Banned " + member.getUser().getAsTag() + " for `" + reason + "` duration: " + duration);
    }
}
