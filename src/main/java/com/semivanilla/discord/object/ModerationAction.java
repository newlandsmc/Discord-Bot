package com.semivanilla.discord.object;

import com.semivanilla.discord.manager.ModerationManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.time.Duration;

@Getter
@Setter
public class ModerationAction {
    private String action;
    private String reason;
    private String duration;

    public Duration getDuration() {
        return Duration.parse(duration);
    }

    public void run(Message message, Member member) {
        String a = action.toLowerCase();
        try {
            if (a.equals("delete")) {
                ModerationManager.delete(message);
            } else if (a.equals("kick")) {
                ModerationManager.kick(member, reason, "");
            } else if (a.equals("ban")) {
                ModerationManager.ban(member, reason, getDuration(), "", false);
            } else if (a.equals("mute") || a.equals("timeout")) {
                if (duration == null || duration.isEmpty()) {
                    throw new IllegalArgumentException("Timeout duration cannot be empty");
                }
                ModerationManager.timeout(member, reason, getDuration(), "");
            }
        } catch (Exception e) {
            System.err.println("Could not perform moderation action " + a + ": " + e.getMessage());
            System.err.println(e.getMessage());
        }
    }
}
