package com.semivanilla.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.semivanilla.discord.commands.*;
import com.semivanilla.discord.listener.MessageListener;
import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.manager.RegexFilterManager;
import com.semivanilla.discord.util.EnvConfig;
import lombok.Getter;
import lombok.Setter;
import net.badbird5907.jdacommand.JDACommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class SVDiscord {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    @Setter
    private static boolean enabled = false;

    @Getter
    private static JDA jda;

    public static void main(String[] args) {
        String token = new EnvConfig().getConfigs().get("token");
        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(new MessageListener())
                    .build();
            jda.awaitReady();
            JDACommand command = new JDACommand(jda);
            command.setReturnCallBack((guild) -> guild.getIdLong() == 950472276887867495L);
            command.registerCommand(new TestCommand());
            command.registerCommand(new ReloadFilterCommand());
            command.registerCommand(new BanCommand());
            command.registerCommand(new KickCommand());
            command.registerCommand(new MuteCommand());
            command.registerCommand(new ClearCommand());
            RegexFilterManager.reload();
            ModerationManager.init();
            System.out.println("Bot is now running.");
            enabled = true;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> setEnabled(false)));
            //command.registerCommandsInPackage("com.semivanilla.discord.commands");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
