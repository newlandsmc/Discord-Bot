package com.semivanilla.discord.listener;

import com.semivanilla.discord.manager.RegexFilterManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        RegexFilterManager.process(event.getMessage(), event.getMember());
    }
}
