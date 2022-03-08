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
    public void tempmute(CommandContext ctx, @Required Member member, @Required String reason, int days, int hours) {
        Duration duration = Duration.ofDays(days).plusHours(hours);
        ModerationManager.timeout(member, reason, duration, ctx.getMember().getUser().getAsTag());
        ctx.reply("Muted " + member.getUser().getAsTag() + " for `" + reason + "` duration: " + duration.toString());
    }
}
