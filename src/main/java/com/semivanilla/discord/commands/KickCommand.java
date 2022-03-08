package com.semivanilla.discord.commands;

import com.semivanilla.discord.manager.ModerationManager;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

public class KickCommand {
    @Command(name = "kick", description = "Kick a user from the server", serverOnly = true, permission = Permission.KICK_MEMBERS)
    public void kick(CommandContext ctx, @Required Member member, @Required String reason) {
        ModerationManager.kick(member, reason, ctx.getMember().getUser().getAsTag());
        ctx.reply("Kicked " + member.getUser().getAsTag() + " for `" + reason + "`");
    }
}
