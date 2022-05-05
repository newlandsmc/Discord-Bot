package com.semivanilla.discord.commands;

import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.util.DateUtils;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

import java.time.Duration;
import java.util.Date;

public class BanCommand {
    @Command(name = "ban", description = "Bans a user from the server.", permission = Permission.BAN_MEMBERS)
    public void ban(CommandContext ctx, @Required Member member, @Required String reason, boolean delMessages, boolean silent) {
        ModerationManager.ban(member, reason, ctx.getMember().getUser().getAsTag(), delMessages);
        ctx.reply("Banned " + member.getUser().getAsTag() + " for `" + reason + "`");
    }

    @Command(name = "tempban", description = "Temporarily bans a user from the server.", permission = Permission.BAN_MEMBERS)
    public void tempban(CommandContext ctx, @Required Member member, @Required String reason, @Required String duration, boolean delMessages) {
        long t = DateUtils.parseTime(duration);
        if (t == -1) {
            ctx.reply("Invalid time duration \"`" + duration + "`\"!\nExpected format `XdYhZm` eg: `1d`");
            return;
        }
        Duration durationD = Duration.ofMillis(t);
        ModerationManager.ban(member, reason, durationD, ctx.getMember().getUser().getAsTag(), delMessages);
        ctx.reply("Banned " + member.getUser().getAsTag() + " for `" + reason + "` duration: " + ModerationManager.humanReadableFormat(durationD) + " Unbanned on: " + new Date(System.currentTimeMillis() + t));
    }

    @Command(name = "unban", description = "Unbans a user from the server.", permission = Permission.BAN_MEMBERS)
    public void unban(CommandContext ctx, @Required String user, @Required String reason) {
        RestAction<User> target = SVDiscord.getJda().retrieveUserById(user);
        target.queue(u -> {
            if (u == null) {
                ctx.reply("Could not find user `" + user + "`");
                return;
            }
            ModerationManager.unban(u, ctx.getMember().getGuild(), reason, ctx.getMember().getUser().getAsTag());
            ctx.reply("Unbanned " + u.getAsTag() + " for `" + reason + "`");
        }, e -> {
            ctx.reply("Could not find user `" + user + "` " + e.getMessage());
        });
    }
}
