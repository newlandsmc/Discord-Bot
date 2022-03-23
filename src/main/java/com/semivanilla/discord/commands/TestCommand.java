package com.semivanilla.discord.commands;

import com.semivanilla.discord.SVDiscord;
import net.badbird5907.jdacommand.CommandResult;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Sender;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

public class TestCommand {
    @Command(name = "test", description = "Test Command 1", botOwnerOnly = true)
    public CommandResult test(@Sender Member member, MessageChannel channel, CommandContext ctx, boolean test) {
        System.out.println("Test Command 0");
        ctx.reply("Working...");
        SVDiscord.getJda().retrieveCommands().queue(commands -> {
            commands.forEach(command -> command.delete().queue(c -> {
                System.out.println("Deleted command: " + command);
            }));
            ctx.reply("done");
        });
        return CommandResult.SUCCESS;
    }
}
