package com.semivanilla.discord.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.object.RoleInfo;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RoleManager extends ListenerAdapter {
    private static final File ROLE_CONFIG = new File("roles.json"),
            ROLE_MESSAGE = new File("rolesmessage");
    private static final ArrayList<RoleInfo> roles = new ArrayList<>();
    private static TextChannel channel;
    private static String messageId;

    public static void init(JDA jda) {
        jda.addEventListener(new RoleManager());
        reload();
    }

    public static void sendMessage() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Roles");
        builder.setColor(new Color(41, 43, 47));
        builder.setDescription("Click on the role to assign it to yourself!");
        List<ItemComponent> components = new ArrayList<>();
        for (RoleInfo role : roles) {
            builder.addField(role.getName(), role.getDescription(), false);
            ButtonStyle style;
            if (role.getButtonColor().equalsIgnoreCase("blue"))
                style = ButtonStyle.PRIMARY;
            else if (role.getButtonColor().equalsIgnoreCase("green"))
                style = ButtonStyle.SUCCESS;
            else if (role.getButtonColor().equalsIgnoreCase("red"))
                style = ButtonStyle.DANGER;
            else style = ButtonStyle.PRIMARY;
            components.add(Button.of(style, "role:" + role.getRoleId(), role.getButtonText()));
        }
        channel.sendMessageEmbeds(builder.build()).setActionRow(
                components.toArray(new ItemComponent[0])
        ).queue(message -> {
            messageId = message.getId();
            try {
                if (!ROLE_MESSAGE.exists())
                    ROLE_MESSAGE.createNewFile();
                PrintStream out = new PrintStream(ROLE_MESSAGE);
                out.print(message.getId());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    @SneakyThrows
    public static void reload() {
        if (ROLE_CONFIG.exists()) {
            String json = new String(Files.readAllBytes(ROLE_CONFIG.toPath()));
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (!obj.has("channel") && !obj.has("roles")) {
                System.out.println("Invalid roles.json file!");
                return;
            }
            channel = SVDiscord.getJda().getTextChannelById(obj.get("channel").getAsString());
            roles.clear();
            JsonArray array = obj.getAsJsonArray("roles");
            for (JsonElement element : array) {
                JsonObject role = element.getAsJsonObject();
                roles.add(SVDiscord.gson.fromJson(role, RoleInfo.class));
            }
        } else {
            Files.createFile(ROLE_CONFIG.toPath());
            PrintStream out = new PrintStream(ROLE_CONFIG);
            out.print("{}");
            out.close();
        }

        if (ROLE_MESSAGE.exists()) {
            messageId = new String(Files.readAllBytes(ROLE_MESSAGE.toPath()));
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        if (event.getMessage().getId().equals(messageId)) {
            String id = event.getButton().getId();
            if (id != null && id.startsWith("role:")) {
                String roleId = id.substring(5);
                RoleInfo role = roles.stream().filter(r -> r.getRoleId().equals(roleId)).findFirst().orElse(null);
                if (role != null) {
                    role.toggle(event.getMember(), event);
                }
            }
        }
    }
}
