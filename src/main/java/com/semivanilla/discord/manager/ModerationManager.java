package com.semivanilla.discord.manager;

import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.util.EnvConfig;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ModerationManager {
    @Getter
    private static MessageChannel auditLogChannel;
    private static final Map<Long, Long> bans = new HashMap<>();

    public static void init() {
        auditLogChannel = SVDiscord.getJda().getTextChannelById(EnvConfig.getConfigs().get("audit-log-channel"));
        new Thread("Unban Thread") {
            @Override
            public void run() {
                while (SVDiscord.isEnabled()) {
                    try {
                        Thread.sleep(1000);
                        bans.forEach(
                                (key, value) -> {
                                    if (value < System.currentTimeMillis()) {
                                        SVDiscord.getJda().getGuildById(key).unban(key + "").queue();
                                    }
                                }
                        );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public static void ban(Member member, String reason, String mod, boolean delMessages) {
        sendBannedMessage(member, reason, "Permanent", mod);
        member.ban(delMessages ? 7 : 0, reason).queue();
    }

    public static void ban(Member member, String reason, Duration duration, String mod, boolean delMessages) {
        sendBannedMessage(member, reason, duration.toString(), mod);
        bans.put(member.getIdLong(), System.currentTimeMillis() + duration.toMillis());
        member.ban(delMessages ? 7 : 0, reason).queue();
    }

    public static void kick(Member member, String reason, String mod) {
        sendKickMessage(member, reason, mod);
        member.kick(reason).queue();
    }

    public static void delete(Message message) {
        message.delete().queue();
        try {
            auditLog("Message Deleted", message.getMember(), "Inappropriate", "", "");
        } catch (Exception e) {
            error(e, message.getMember(), "delete");
            e.printStackTrace();
        }
    }

    public static void timeout(Member member, String reason, Duration duration, String mod) {
        try {
            member.timeoutFor(duration).queue();
            sendTimeoutMessage(member, reason, duration.toString(), mod);
        } catch (Exception e) {
            error(e, member, "timeout");
            e.printStackTrace();
        }
    }

    public static void sendTimeoutMessage(Member member, String reason, String duration, String mod) {
        auditLog("Timeout", member, reason, mod, duration);
        member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle("Timeout")
                .setDescription("You have been timed out from SemiVanilla-MC For:\n```" + reason + "```")
                .addField("Duration", duration, false)
                .build()).queue());
    }

    public static void sendBannedMessage(Member member, String reason, String duration, String mod) {
        auditLog("Ban", member, reason, mod, duration);
        member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle("Banned")
                .setDescription("You have been banned from SemiVanilla-MC For:\n```" + reason + "```")
                .addField("Duration", duration, false)
                .build()).queue());
    }

    public static void sendKickMessage(Member member, String reason, String mod) {
        auditLog("Kick", member, reason, mod, null);
        member.getUser().openPrivateChannel().queue(channel -> channel.sendMessageEmbeds(new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle("Kicked")
                .setDescription("You have been kicked from SemiVanilla-MC For:\n```" + reason + "```")
                .build()).queue());
    }

    public static void error(Exception e, Member member, String action) {
        ModerationManager.getAuditLogChannel().sendMessageEmbeds(new EmbedBuilder().setTitle("Error").setColor(Color.RED).setDescription("Could not perform moderation action `" + action + "`: " + e.getMessage())
                .addField("User", member.getAsMention() + "(" + member.getUser().getAsTag() + ")", false).build()).queue();
    }

    public static void auditLog(String action, Member member, String reason, String moderator, String duration) {
        EmbedBuilder embed = new EmbedBuilder().setColor(Color.ORANGE)
                .setTitle(action)
                .addField("Member", member.getAsMention() + "(" + member.getUser().getAsTag() + ")", false)
                .addField("Reason", reason, false)
                .setThumbnail(member.getUser().getAvatarUrl());
        if (duration != null && !duration.isEmpty()) {
            embed.addField("Duration", duration, false);
        }
        if (moderator != null && !moderator.isEmpty()) {
            embed.addField("Moderator", moderator, false);
        }
        auditLogChannel.sendMessageEmbeds(embed.build()).queue();
    }
}
