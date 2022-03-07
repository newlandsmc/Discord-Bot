package com.semivanilla.discord;

import com.semivanilla.discord.commands.TestCommand;
import net.badbird5907.jdacommand.CommandListener;
import net.badbird5907.jdacommand.JDACommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class SVDiscord {
    public static void main(String[] args) {
        String token = "";
        try {
            JDA jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES,GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(new ListenerAdapter() {
                        @Override
                        public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                            super.onSlashCommandInteraction(event);
                            System.out.println("Cmd: " + event);
                        }

                        @Override
                        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                            super.onMessageReceived(event);
                            System.out.println("Msg: " + event);
                        }
                    }).build();
            jda.awaitReady();
            JDACommand command = new JDACommand(jda);
            command.registerCommand(new TestCommand());
            System.out.println("Bot is now running.");
            command.printAllRegisteredCommands();
            //command.registerCommandsInPackage("com.semivanilla.discord.commands");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
