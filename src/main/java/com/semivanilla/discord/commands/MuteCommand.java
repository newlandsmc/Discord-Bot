package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.ModerationManager;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;

public class MuteCommand {
    @Command(name = "mute", aliases = {"timeout", "tempmute"}, description = "Mute a user", serverOnly = true, permission = Permission.MODERATE_MEMBERS)
    public void tempmute(CommandContext ctx, @Required Member member, @Required String reason, int days, int hours, int minutes) {
        if ((days == 0 && hours == 0 && minutes == 0) || (days == -1 && hours == -1 && minutes == -1)) {
            ctx.reply("You must specify a time.");
            return;
        }
        Duration duration = Duration.ofDays(days == -1 ? 0 : days).plusHours(hours == -1 ? 0 : hours)
                .plusMinutes(minutes == -1 ? 0 : minutes);
        ModerationManager.timeout(member, reason, duration, ctx.getMember().getUser().getAsTag());
        ctx.reply("Muted " + member.getUser().getAsTag() + " for `" + reason + "` duration: " + ModerationManager.humanReadableFormat(duration));
    }
    @Command(name = "unmute",description = "Unmute a user", serverOnly = true, permission = Permission.MODERATE_MEMBERS)
    public void unmute(CommandContext ctx, @Required Member member, @Required String reason) {
        ModerationManager.unmute(member, reason, ctx.getMember().getUser().getAsTag());
        ctx.reply("Unmuted " + member.getUser().getAsTag() + " for `" + reason + "`");
    }
}
