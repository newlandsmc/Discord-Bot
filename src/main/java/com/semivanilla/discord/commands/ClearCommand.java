package com.semivanilla.discord.commands;

import com.semivanilla.discord.SVDiscord;
import net.badbird5907.jdacommand.annotation.Command;
import net.badbird5907.jdacommand.context.CommandContext;

public class ClearCommand {
    @Command(name = "clear", description = "Clear commands")
    public void clear(CommandContext ctx) {
        SVDiscord.getJda().retrieveCommands().queue(commands -> {
            commands.forEach(command -> {
                command.delete().queue();
            });
            ctx.reply("done");
        });
    }
}
