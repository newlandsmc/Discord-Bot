package com.semivanilla.discord.commands;

import net.badbird5907.jdacommand.CommandResult;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.annotation.Sender;
import net.badbird5907.jdacommand.context.CommandContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

public class TestCommand {
    @Command(name = "test",description = "Test Command")
    public CommandResult test(@Sender Member member, MessageChannel channel, CommandContext ctx, boolean test) {
        ctx.reply("Hello World! b: " + test);
        return CommandResult.SUCCESS;
    }
}
