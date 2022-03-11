package com.semivanilla.discord.util;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class Transcript {
    private final TextChannel channel;

    private StringBuilder sb = new StringBuilder();

    public CompletableFuture<ByteArrayInputStream> start() {
        CompletableFuture<ByteArrayInputStream> future = new CompletableFuture<>();
        channel.getIterableHistory().takeAsync(1000).thenAcceptAsync(messages -> {
            //reverse order
            for (int i = messages.size() - 1; i >= 0; i--) {
                appendMessage(messages.get(i));
            }
            //turn sb into a inputstream
            future.complete(new ByteArrayInputStream(sb.toString().getBytes()));
        });
        return future;
    }

    public void appendMessage(Message message) {
        sb.append("\n")
                .append(message.getAuthor().getAsTag() + ": ")
                .append(message.getContentDisplay());
    }
}
