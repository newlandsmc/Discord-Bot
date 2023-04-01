package com.semivanilla.discord.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.object.MarketItem;
import com.semivanilla.discord.object.TimerEvent;
import net.badbird5907.lightning.annotation.EventHandler;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class MarketManager {
    private static final File MARKET_DATA_FILE = new File("market-data.json");
    private static boolean enabled = false;

    private static Pattern pattern;

    private static Duration duration;

    private static boolean deleteIfNoPhoto;

    private static List<String> channels = new ArrayList<>();

    private static CopyOnWriteArrayList<MarketItem> items = new CopyOnWriteArrayList<>();

    private static long lastSave = -1;

    public static void init() {
        File file = new File("market.json");
        if (!file.exists()) {
            System.out.println("market.json does not exist. Disabling module");
            enabled = false;
        }
        try {
            JsonObject jsonObject = JsonParser.parseString(new String(Files.readAllBytes(file.toPath()))).getAsJsonObject();
            for (JsonElement jsonElement : jsonObject.get("channels").getAsJsonArray()) {
                channels.add(jsonElement.getAsString());
            }
            duration = Duration.parse(jsonObject.get("delete-timer").getAsString());
            deleteIfNoPhoto = jsonObject.get("delete-if-no-photo").getAsBoolean();
            pattern = Pattern.compile(jsonObject.get("pattern").getAsString().trim());
            enabled = true;
            lastSave = System.currentTimeMillis();
        } catch (IOException e) {
            enabled = false;
            throw new RuntimeException(e);
        }

        if (!enabled)
            return;

        if (MARKET_DATA_FILE.exists()) {
            try {
                String contents = new String(Files.readAllBytes(MARKET_DATA_FILE.toPath()));
                TypeToken<CopyOnWriteArrayList<MarketItem>> token = new TypeToken<>() {
                };
                items = SVDiscord.gson.fromJson(contents, token.getType());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveMarketData() {
        if (!enabled)
            return;
        if (!MARKET_DATA_FILE.exists()) {
            try {
                MARKET_DATA_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Files.write(MARKET_DATA_FILE.toPath(), SVDiscord.gson.toJson(items).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public static void onTick(TimerEvent event) {
        if (!enabled) return;
        if (System.currentTimeMillis() - lastSave > 60000) {
            saveMarketData();
            lastSave = System.currentTimeMillis();
        }
        for (MarketItem item : items) {
            if (item.getExpiry() < System.currentTimeMillis()) {
                System.out.println("Item expired, deleted: " + item);
                item.delete();
                items.remove(item);
            }
        }
    }

    //not a event from lightning/event bus, called from listener
    public static boolean onMessage(MessageReceivedEvent event) {
        if (!enabled) return false;
        if (!channels.contains(event.getChannel().getId())) return false;
        if (pattern.matcher(event.getMessage().getContentRaw()).find()) {
            event.getMessage().delete().queue();
            return true;
        }
        if (deleteIfNoPhoto && event.getMessage().getAttachments().isEmpty()) {
            System.out.println("No photo found");
            event.getMessage().delete().queue();
            return true;
        }
        MarketItem item = new MarketItem(event.getMessage().getId(), System.currentTimeMillis() + duration.toMillis(), event.getChannel().getId());
        items.add(item);
        saveMarketData();

        return false;
    }

    public static void onDelete(MessageDeleteEvent event){
        if (!enabled) return;
        if (items.removeIf(item -> item.getId().equals(event.getMessageId())))
            saveMarketData();
    }
}
