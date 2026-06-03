package dev.redgamer6427a.admiral.common.text;

import net.kyori.adventure.text.Component;

import java.util.Map;

public class AntiSwear {

    public static boolean checkForSwear(String input){

        String[] notAllowed = {
                "fuck",
                "fick",
                "scheiss",
                "scheiß",
                "hure",
                "hass",
                "hate",
                "bitch",
                "boob",
                "nigg",
                "niga",
                "nega",
                "negg",
                "ass",
                "arse",
                "arsch",
                "fuc",
                "shit",
                "fck"

        };

        String treatedString = input;

        Map<String, String> replacements = Map.of(
                " ", "",
                "_", "",
                ".", "",
                "!", "",
                "+", "",
                "&", "",
                "1", "i",
                "3", "e",
                "0", "o"
        );

        for(Map.Entry<String, String> entry : replacements.entrySet()){
            treatedString = treatedString.replace(entry.getKey(), entry.getValue());
        }

        for(String s: notAllowed){
            if(treatedString.toLowerCase().contains(s.toLowerCase())){
                return true;
            }

        }
        return false;
    }

    public static boolean checkForSwear(Component input){
        return checkForSwear(MiniMessageUtils.serialize(MiniMessageUtils.stripAllStyles(input)));
    }


}
