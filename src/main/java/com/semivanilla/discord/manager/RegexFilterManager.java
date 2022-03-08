package com.semivanilla.discord.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.semivanilla.discord.SVDiscord;
import com.semivanilla.discord.object.RegexFilter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RegexFilterManager {
    private static final File REGEX_FILE = new File("regex.json");
    private static final List<RegexFilter> filters = new ArrayList<>();

    @SneakyThrows
    public static void reload() {
        if (!REGEX_FILE.exists()) {
            try {
                REGEX_FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        filters.clear();
        String json = new String(Files.readAllBytes(REGEX_FILE.toPath()));
        ArrayList<RegexFilter> regexFilters = new ArrayList<>();
        JsonArray jo = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement je : jo) {
            regexFilters.add(SVDiscord.gson.fromJson(je, RegexFilter.class));
        }
        filters.addAll(regexFilters);
    }

    public static void process(Message message, Member member) {
        for (RegexFilter filter : filters) {
            filter.process(message, member);
        }
    }
}
