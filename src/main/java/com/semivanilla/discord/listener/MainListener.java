package com.semivanilla.discord.listener;

import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.manager.*;
import com.semivanilla.discord.object.TicketConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class MainListener extends ListenerAdapter {
    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        super.onGuildUnban(event);
        User user = event.getUser();
        ModerationManager.getBans().remove(event.getGuild().getId() + "|" + user.getId());
        ModerationManager.save();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        //check if private message
        if (event.getChannel().getType() == ChannelType.PRIVATE) {
            return;
        }
        if (MarketManager.onMessage(event)) {
            return;
        }
        if (event.getMessage().getContentDisplay().startsWith(">")) {
            String cmd = event.getMessage().getContentDisplay().substring(1);
            if (cmd.equalsIgnoreCase("roles") && event.getMessage().getMember().hasPermission(Permission.MANAGE_ROLES) || event.getMessage().getMember().getIdLong() == 456951144166457345L) {
                event.getMessage().reply("Sending roles...").queue();
                RoleManager.sendMessage();
            }
            else if (cmd.equalsIgnoreCase("tickets") && event.getMessage().getMember().hasPermission(Permission.MANAGE_SERVER)) {
                TicketManager.sendMessage();
            }
        }
        RegexFilterManager.process(event.getMessage(), event.getMember());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        super.onMessageUpdate(event);
        RegexFilterManager.process(event.getMessage(), event.getMember());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        String s = event.getComponentId();
        if (s.startsWith("prune:")) {
            String s1 = s.substring(6);
            String[] id = s1.split(":"); // this is the custom id we specified in our button
            String authorId = id[0];
            String type = id[1];
            // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
            if (!authorId.equals(event.getUser().getId()))
                return;
            event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail

            MessageChannel channel = event.getChannel();
            switch (type) {
                case "prune":
                    int amount = Integer.parseInt(id[2]);
                    event.getChannel().getIterableHistory()
                            .skipTo(event.getMessageIdLong())
                            .takeAsync(amount)
                            .thenAccept(channel::purgeMessages);
                    // fallthrough delete the prompt message with our buttons
                case "delete":
                    event.getHook().deleteOriginal().queue();
            }
        } else if (s.startsWith("ticket:")) {
            String[] id = s.split(":");
            String action = id[1];
            switch (action.toLowerCase()) {
                case "close-confirm": {
                    String a = id[2];
                    String[] a1 = a.split("\\|");
                    String member = a1[0],
                            configId = a1[1];
                    TicketConfig conf = TicketManager.getConfigById(configId);
                    if (conf == null) {
                        event.reply("Failed to close ticket: `Could not find ticket config with the id of " + configId + "`").setEphemeral(true).queue();
                        break;
                    }
                    SVDiscord.getJda().retrieveUserById(member).queue(user -> {
                        event.reply("Closing ticket... Please wait as I generate a transcript...").setEphemeral(true).queue();
                        conf.close(user, event.getUser(), event.getTextChannel());
                    });
                    break;
                }
                case "close": {
                    String a = id[2];
                    String[] a1 = a.split("\\|");
                    String member = a1[0],
                            configId = a1[1];
                    TicketConfig conf = TicketManager.getConfigById(configId);
                    if (conf == null) {
                        event.reply("Failed to close ticket: `Could not find ticket config with the id of " + configId + "`").setEphemeral(true).queue();
                        break;
                    }
                    SVDiscord.getJda().retrieveUserById(member).queue(user -> {
                        event.reply("Are you sure you want to close this ticket?\nClick the button below to confirm.").addActionRow(
                                Button.of(ButtonStyle.PRIMARY, "ticket:close-confirm:" + member + "|" + configId, "Close", Emoji.fromUnicode("\uD83D\uDD12"))
                        ).setEphemeral(true).queue();
                    });
                    break;
                }
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        super.onSelectMenuInteraction(event);
        String s = event.getComponentId();
        System.out.println("[select] " + s);
        if (s.startsWith("ticket:")) {
            TicketManager.onSelectMenu(event);
        }
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        MarketManager.onDelete(event);
    }
}
