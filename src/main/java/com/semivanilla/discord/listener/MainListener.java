package com.semivanilla.discord.listener;

import com.semivanilla.discord.manager.ModerationManager;
import com.semivanilla.discord.manager.RegexFilterManager;
import com.semivanilla.discord.manager.RoleManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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
        if (event.getMessage().getContentDisplay().startsWith(">")) {
            String cmd = event.getMessage().getContentDisplay().substring(1);
            if (cmd.equalsIgnoreCase("roles") && event.getMessage().getMember().hasPermission(Permission.MANAGE_ROLES))
                RoleManager.sendMessage();
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
        }
    }
}
