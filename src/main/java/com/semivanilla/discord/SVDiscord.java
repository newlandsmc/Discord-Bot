package com.semivanilla.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.semivanilla.discord.commands.*;
import com.semivanilla.discord.listener.MainListener;
import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.manager.RegexFilterManager;
import com.semivanilla.discord.manager.RoleManager;
import com.semivanilla.discord.manager.TicketManager;
import com.semivanilla.discord.util.EnvConfig;
import lombok.Getter;
import lombok.Setter;
import net.badbird5907.jdacommand.JDACommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Date;
import java.util.Scanner;

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
                    .addEventListeners(new MainListener())
                    .build();
            jda.awaitReady();
            JDACommand command = new JDACommand(jda, new Long[]{456951144166457345L, 881939327817486386L}); //FIXME only serverOnly commands work
            //command.setReturnCallBack((guild) -> guild.getIdLong() == 950472276887867495L);
            command.registerCommand(new TestCommand());
            command.registerCommand(new ReloadCommand());
            command.registerCommand(new BanCommand());
            command.registerCommand(new KickCommand());
            command.registerCommand(new MuteCommand());
            command.registerCommand(new PruneCommand());

            enabled = true;

            RegexFilterManager.reload();
            ModerationManager.init();
            RoleManager.init(jda);
            TicketManager.init();
            System.out.println("SVMC Bot Successfully connected to " + jda.getSelfUser().getAsTag() + " (" + jda.getSelfUser().getIdLong() + ") " + new Date());
            System.out.println("Registering commands with discord, this may take a while...");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Saving Data...");
                ModerationManager.disable();
                TicketManager.disable();

                setEnabled(false);
            }));
            new Thread("Console Thread") { // to gracefully shutdown if using intellij
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.equalsIgnoreCase("exit")) {
                            System.exit(0);
                        }
                    }
                }
            }.start();
            //command.registerCommandsInPackage("com.semivanilla.discord.commands");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
