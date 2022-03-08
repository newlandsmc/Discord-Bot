package com.semivanilla.discord.util;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnvConfig {
    @Getter
    private static final Map<String, String> configs = new ConcurrentHashMap<>();

    public EnvConfig() {
        for (String s : readFileLines(".env")) {
            if (s.startsWith("#") || s.equalsIgnoreCase("") || s.equalsIgnoreCase("\n")) {
                continue;
            }
            String[] sarray = s.split("=");
            if (sarray.length == 1)
                continue;
            StringBuilder after = new StringBuilder();
            /*
            for (String s1 : sarray) {
                if (after.length() == 0)
                    after.append(s1);
                else after.append("=").append(s1);
            }
            configs.put(sarray[0], after.toString());
             */
            configs.put(sarray[0].trim(), sarray[1].trim());
        }
        //if (!ConfigManager.azureComputerVisionToken.equalsIgnoreCase("empty"))
        //OctoCord.setComputerVisionClient(ComputerVisionManager.authenticate(ConfigManager.azureComputerVisionToken));
    }

    private static String[] readFileLines(String file) {
        List<String> str = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                str.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toArray(new String[0]);
    }

    public static int getInt(String key) {
        return Integer.parseInt(getConfigs().get(key));
    }
}
