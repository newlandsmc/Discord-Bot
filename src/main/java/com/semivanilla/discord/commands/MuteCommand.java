package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.util.DateUtils;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.util.Date;

public class MuteCommand {
    @Command(name = "mute", aliases = {"timeout", "tempmute"}, description = "Mute a user", permission = Permission.MODERATE_MEMBERS)
    public void tempmute(CommandContext ctx, @Required Member member, @Required String reason, @Required String duration) {
        long t = DateUtils.parseTime(duration);
        if (t == -1) {
            ctx.reply("Invalid time duration \"`" + duration + "`\"!\nExpected format `XdYhZm` eg: `1d`");
            return;
        }
        Duration durationD = Duration.ofMillis(t);
        ModerationManager.timeout(member, reason, durationD, ctx.getMember().getUser().getAsTag());
        ctx.reply("Muted " + member.getUser().getAsTag() + " for `" + reason + "` duration: " + ModerationManager.humanReadableFormat(durationD) + " Unbanned on: " + new Date(System.currentTimeMillis() + t));
    }

    @Command(name = "unmute", description = "Unmute a user", permission = Permission.MODERATE_MEMBERS)
    public void unmute(CommandContext ctx, @Required Member member, @Required String reason) {
        ModerationManager.unmute(member, reason, ctx.getMember().getUser().getAsTag());
        ctx.reply("Unmuted " + member.getUser().getAsTag() + " for `" + reason + "`");
    }
}
