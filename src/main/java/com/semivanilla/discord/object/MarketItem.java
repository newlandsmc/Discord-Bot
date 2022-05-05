package com.semivanilla.discord.object;

import com.semivanilla.discord.SVDiscord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MarketItem {
    private String id;
    private long expiry;
    private String channelId;

    public void delete() {
        SVDiscord.getJda().getTextChannelById(channelId).deleteMessageById(id).queue();
    }
}
