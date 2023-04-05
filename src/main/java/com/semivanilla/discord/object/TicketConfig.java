package com.semivanilla.discord.object;

import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.manager.TicketManager;
import com.semivanilla.discord.util.Transcript;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

@Getter
public class TicketConfig {
    private String id, name, description, message, emoji, emojiID;

    @Getter
    @Setter
    public static class EmbedConfig {
        private String title, description, color, footer, footerIcon;
        private FieldConfig[] fields;
    }

    @Getter
    @Setter
    public static class FieldConfig {
        private String name, value;
        private boolean inline;
        private boolean applications;
    }

    public TicketManager.TicketOpenResult open(Member member, Consumer<String> channelIdConsumer) {
        String category = TicketManager.getSupportCategory();
        Guild guild = SVDiscord.getJda().getGuildById(TicketManager.getGuildId());
        if (category != null && guild != null) {
            Category cat = guild.getCategoryById(category);
            if (cat == null) {
                return TicketManager.TicketOpenResult.ERROR;
            }
            cat.createTextChannel(this.name + "-" + ++TicketManager.tickets/*member.getUser().getName()*/).queue(channel -> {
                channelIdConsumer.accept(channel.getId());
                channel.getManager().setTopic(member.getId()).queue();
                channel.getManager().putMemberPermissionOverride(member.getIdLong(), Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), new ArrayList<>()).queue(a -> {
                    channel.sendMessage(this.message.replace("%user%", member.getAsMention())).setActionRow(
                            Button.of(ButtonStyle.PRIMARY, "ticket:close:" + member.getId() + "|" + id, "Close", Emoji.fromUnicode("\uD83D\uDD12"))
                    ).queue();
                });
            });
            TicketManager.save();
            return TicketManager.TicketOpenResult.SUCCESS;
        }
        return TicketManager.TicketOpenResult.ERROR;
    }

    public void close(User user, User closer, TextChannel channel) {
        new Transcript(channel).start().thenAcceptAsync(inputStream -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                byte[] buffer = new byte[512];
                int len;
                while ((len = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                buffer = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
            ModerationManager.getAuditLogChannel().sendMessageEmbeds(new EmbedBuilder().setColor(Color.YELLOW)
                            .setTitle("Ticket Closed")
                            .addField("User", user.getAsMention() + " (" + user.getAsTag() + ")", false)
                            .addField("Closed By", closer.getAsMention() + " (" + closer.getAsTag() + ")", false)
                            .addField("Type", name, false)
                            .setTimestamp(Instant.now())
                            .build())
                    .addFile(is1, "transcript.txt")
                    .queue();
            user.openPrivateChannel().queue(pm -> pm.sendMessageEmbeds(new EmbedBuilder().setTitle("Ticket Closed")
                                    .setDescription("Your " + name + " ticket was closed.").addField("Closed By", closer.getAsMention() + " (" + closer.getAsTag() + ")", false)
                                    .setTimestamp(Instant.now()).build())
                            .addFile(is2, "transcript.txt").queue(), e -> {
                    }
            );
            channel.delete().queue();
        });

    }
}
