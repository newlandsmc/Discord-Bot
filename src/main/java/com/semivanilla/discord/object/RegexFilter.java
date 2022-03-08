package com.semivanilla.discord.object;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
public class RegexFilter {
    private String pattern;
    private List<ModerationAction> actions;
    private transient Pattern regexPattern;

    public RegexFilter(String pattern, List<ModerationAction> actions) {
        this.pattern = pattern;
        this.actions = actions;
        this.regexPattern = Pattern.compile(pattern);
    }

    public void process(Message message, Member member) {
        if (getRegexPattern().matcher(message.getContentDisplay()).find()) {
            actions.forEach(action -> action.run(message, member));
        }
    }

    public Pattern getRegexPattern() {
        if (regexPattern == null) {
            this.regexPattern = Pattern.compile(pattern);
        }
        return regexPattern;
    }
}
