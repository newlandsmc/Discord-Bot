package com.semivanilla.discord.manager;

import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.util.EnvConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModerationManager {
    private static final File FILE = new File("bans.json");
    @Getter
    private static MessageChannel auditLogChannel;
    @Getter
    private static HashMap<String, String> bans = new HashMap<>();

    @SneakyThrows
    public static void init() {
        if (FILE.exists()) {
            String json = new String(Files.readAllBytes(FILE.toPath()));
            bans = SVDiscord.gson.fromJson(json, HashMap.class);
        }
        auditLogChannel = SVDiscord.getJda().getTextChannelById(EnvConfig.getConfigs().get("audit-log-channel"));

        new Thread("Unban Thread") {
            @Override
            public void run() {
                while (SVDiscord.isEnabled()) {
                    try {
                        Thread.sleep(1000);
                        Iterator<Map.Entry<String, String>> iterator = bans.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, String> entry = iterator.next();
                            String key = entry.getKey();
                            String value = entry.getValue();
                            long time = Long.parseLong(value);
                            if (time < System.currentTimeMillis()) {
                                String[] a = key.split("\\|");
                                String guildId = a[0];
                                String userId = a[1];
                                System.out.println("Unbanning " + guildId);
                                //bans.remove(key);
                                iterator.remove();
                                SVDiscord.getJda().getGuildById(guildId).unban(userId).queue((c) -> {
                                    System.out.println("Successfully unbanned " + userId);
                                    SVDiscord.getJda().retrieveUserById(userId).queue(user -> {
                                        String username;
                                        if (user == null) {
                                            username = "<@" + userId + ">";
                                        } else username = user.getAsTag();
                                        auditLog("Unban", null, "Ban Expired\n\n**User**\n" + username, "", "");
                                    }, e -> error(e, userId, "Unban"));
                                }, e -> {
                                    System.err.println("Failed to unban " + userId + "\n" + e.getMessage());
                                    error(e, "<@" + userId + "> (" + userId + ")", "Unban");
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @SneakyThrows
    public static void save() {
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PrintStream ps = new PrintStream(FILE);
        ps.print(SVDiscord.gson.toJson(bans));
        ps.close();
    }

    public static void disable() {
        save();
    }

    public static void ban(Member member, String reason, String mod, boolean delMessages) {
        sendBannedMessage(member, reason, "Permanent", mod);
        member.ban(delMessages ? 7 : 0, reason).queue();
    }

    public static void ban(Member member, String reason, Duration duration, String mod, boolean delMessages) {
        sendBannedMessage(member, reason, humanReadableFormat(duration), mod);
        bans.put(member.getGuild().getId() + "|" + member.getId(), (System.currentTimeMillis() + duration.toMillis()) + "");
        member.ban(delMessages ? 7 : 0, reason).queue();
        save();
    }

    public static void unban(User user, Guild guild, String reason, String mod) {
        bans.remove(guild.getId() + "|" + user.getId());
        guild.unban(user).queue();
        save();
        auditLog("Unban", user, reason, mod, "");
    }

    public static void kick(Member member, String reason, String mod) {
        sendKickMessage(member, reason, mod);
        member.kick(reason).queue();
    }

    public static void delete(Message message) {
        message.delete().queue();
        try {
            auditLog("Message Deleted", message.getAuthor(), "Prohibited Language\n**Message**\n||" + message.getContentDisplay() + "||\n**Channel**\n" + message.getChannel().getAsMention(), "", "");
        } catch (Exception e) {
            error(e, message.getMember(), "delete");
            e.printStackTrace();
        }
    }

    public static void unmute(Member member, String reason, String mod) {
        member.getGuild().removeTimeout(member).queue();
        auditLog("Unmute", member.getUser(), reason, mod, "");
    }

    public static void timeout(Member member, String reason, Duration duration, String mod) {
        try {
            member.timeoutFor(duration).queue();
            sendTimeoutMessage(member, reason, humanReadableFormat(duration), mod);
        } catch (Exception e) {
            error(e, member, "timeout");
            e.printStackTrace();
        }
    }

    public static void sendTimeoutMessage(Member member, String reason, String duration, String mod) {
        auditLog("Timeout", member.getUser(), reason, mod, duration);
        try {
            member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                    .setTitle("Timeout")
                    .setDescription("You have been timed out from SemiVanilla-MC For:\n```" + reason + "```")
                    .addField("Duration", duration, false)
                    .build()).queue());
        } catch (Exception e) {

        }
    }

    public static void sendBannedMessage(Member member, String reason, String duration, String mod) {
        auditLog("Ban", member.getUser(), reason, mod, duration);
        try {
            member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                    .setTitle("Banned")
                    .setDescription("You have been banned from SemiVanilla-MC For:\n```" + reason + "```")
                    .addField("Duration", duration, false)
                    .build()).queue((c) -> {
            }, e -> {
            }));
        } catch (Exception e) {

        }
    }

    public static void sendKickMessage(Member member, String reason, String mod) {
        auditLog("Kick", member.getUser(), reason, mod, null);
        try {
            member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                    .setTitle("Kicked")
                    .setDescription("You have been kicked from SemiVanilla-MC For:\n```" + reason + "```")
                    .build()).queue((c) -> {
            }, e -> {
            }));
        } catch (Exception e) {

        }
    }

    public static void error(Throwable e, Member member, String action) {
        ModerationManager.getAuditLogChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Error").setColor(Color.RED).setTimestamp(Instant.now()).setDescription("Could not perform moderation action `" + action + "`: " + e.getMessage())
                .addField("User", member.getAsMention() + "(" + member.getUser().getAsTag() + ")", false).build()).queue(c -> {
        });
    }

    public static void error(Throwable e, String member, String action) {
        ModerationManager.getAuditLogChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Error").setColor(Color.RED).setTimestamp(Instant.now()).setDescription("Could not perform moderation action `" + action + "`: " + e.getMessage())
                .addField("User", member, false).build()).queue(c -> {
        });
    }

    public static void auditLog(String action, User user, String reason, String moderator, String duration) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(action);
        if ("Ban".equals(action)) {
            embed.setColor(Color.RED);
        } else {
            embed.setColor(Color.ORANGE);
        }
        if (user != null)
            embed.addField("Member", user.getAsMention() + " (" + user.getAsTag() + ")", false)
                    .setThumbnail(user.getAvatarUrl());
        if (reason != null && !reason.isEmpty())
            embed.addField("Reason", reason, false);
        if (duration != null && !duration.isEmpty()) {
            embed.addField("Duration", duration, false);
        }
        if (moderator != null && !moderator.isEmpty()) {
            embed.addField("Moderator", moderator, false);
        }
        embed.setTimestamp(Instant.now());
        auditLogChannel.sendMessageEmbeds(embed.build()).queue();
    }

    public static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
