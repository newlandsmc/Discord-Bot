package com.semivanilla.discord.commands;

import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Required;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;

public class PruneCommand {
    @Command(name = "prune", aliases = "purge", permission = Permission.MANAGE_SERVER, description = "Prune messages")
    public void prune(CommandContext ctx, @Required int amount) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.RED)
                .setTitle("Purge")
                .setDescription("Are you sure if you want to purge " + amount + " messages?")
                .setFooter("This action cannot be undone.", null);
        String userId = ctx.getMember().getId();
        ctx.getEvent().getHook().editOriginalEmbeds(eb.build())
                .setActionRow(
                        Button.secondary("prune:" + userId + ":delete", "No"),
                        Button.danger("prune:" + userId + ":prune:" + amount, "Yes"))
                .queue();
    }
}
