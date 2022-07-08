package com.semivanilla.discord.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.object.TicketConfig;
import lombok.Getter;
import net.badbird5907.jdacommand.util.object.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TicketManager {
    private static final File CONFIG_FILE = new File("ticket_config.json"), TICKETS_FILE = new File("tickets"), TICKETS_MESSAGE_FILE = new File("tickets_message");
    @Getter
    private static final List<TicketConfig> configs = new ArrayList<>();
    public static int tickets = 0;
    @Getter
    private static String supportChannel, supportCategory, guildId;
    @Getter
    private static String ticketsMessage = null;

    public static void init() {
        if (!TICKETS_FILE.exists()) {
            try {
                TICKETS_FILE.createNewFile();
                PrintStream ps = new PrintStream(TICKETS_FILE);
                ps.print("0");
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                tickets = Integer.parseInt(Files.readAllLines(TICKETS_FILE.toPath()).get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (TICKETS_MESSAGE_FILE.exists()) {
            try {
                ticketsMessage = new String(Files.readAllBytes(TICKETS_MESSAGE_FILE.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            String json = new String(Files.readAllBytes(CONFIG_FILE.toPath()));
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            supportChannel = jsonObject.get("support-channel").getAsString();
            supportCategory = jsonObject.get("support-category").getAsString();
            guildId = jsonObject.get("guild-id").getAsString();
            JsonArray jsonArray = jsonObject.get("tickettypes").getAsJsonArray();
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject1 = element.getAsJsonObject();
                TicketConfig config = SVDiscord.gson.fromJson(jsonObject1, TicketConfig.class);
                configs.add(config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void disable() {
        save();
    }

    public static void save() {
        try {
            PrintStream ps = new PrintStream(TICKETS_FILE);
            ps.print(tickets);
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage() {
        TextChannel channel = SVDiscord.getJda().getGuildById(guildId).getTextChannelById(supportChannel);
        Pair<EmbedBuilder, SelectMenu.Builder> pair = getEmbed();
        channel.sendMessageEmbeds(pair.getValue0().build()).setActionRow(
                pair.getValue1().build()
        ).queue(m -> setTicketsMessage(m.getId()));
    }

    public static void update() {
        TextChannel channel = SVDiscord.getJda().getGuildById(guildId).getTextChannelById(supportChannel);
        Pair<EmbedBuilder, SelectMenu.Builder> pair = getEmbed();
        channel.editMessageEmbedsById(ticketsMessage, pair.getValue0().build())
                .setActionRow(
                        pair.getValue1().build()
                ).queue(m -> {
                    setTicketsMessage(m.getId());
                });
    }

    public static Pair<EmbedBuilder, SelectMenu.Builder> getEmbed() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("Select an option below to open a ticket!\n")
                .addField("\u2753 Help", "Request support.", false)
                .addField("\uD83D\uDC1B Bug", "Report an issue.\n" +
                        "Please check <#953014350132678706> first!", false)
                .addField("\uD83D\uDCA1 Suggestion", "Share an idea.\n" +
                        "Please check <#953014425969889350> first!\n", false)
                .addField("Please, provide as much detail in your ticket as possible!",
                        "This helps us help you faster.", false).setColor(new Color(41, 43, 47))
                .setTitle("Support");
        SelectMenu.Builder b = SelectMenu.create("ticket:create");
        for (TicketConfig config : configs) {
            Emoji emoji;
            if (config.getEmoji() != null)
                emoji = Emoji.fromUnicode(config.getEmoji());
            else if (config.getEmojiID() != null)
                emoji = Emoji.fromEmote(Objects.requireNonNull(SVDiscord.getJda().getGuildById(guildId).getEmoteById(config.getEmojiID())));
            else emoji = null;
            b.addOption(config.getName(), "ticket:open:" + config.getId(), config.getDescription(), null);
        }
        return new Pair<>(builder, b);
    }

    public static void onSelectMenu(SelectMenuInteractionEvent event) {
        String[] actions = event.getComponentId().split(":");
        System.out.println("[select] " + Arrays.asList(actions));
        if (actions[1].equals("create")) {
            String[] selection = event.getSelectedOptions().get(0).getValue().split(":");
            if (selection[0].equals("ticket") && selection[1].equals("open")) {
                String id = selection[2];
                System.out.println("[select] " + id);
                getConfigById(id).open(event.getMember());
                event.reply("Ticket opened!").setEphemeral(true).queue();
                update();
            }
        }
    }

    public static TicketConfig getConfigById(String id) {
        return configs.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static void setTicketsMessage(String ticketsMessage) {
        TicketManager.ticketsMessage = ticketsMessage;
        if (!TICKETS_MESSAGE_FILE.exists()) {
            try {
                TICKETS_MESSAGE_FILE.createNewFile();
                PrintStream ps = new PrintStream(TICKETS_MESSAGE_FILE);
                ps.print(ticketsMessage);
                ps.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
