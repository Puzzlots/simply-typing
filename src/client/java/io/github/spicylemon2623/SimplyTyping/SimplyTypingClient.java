package io.github.spicylemon2623.SimplyTyping;

import dev.puzzleshq.puzzleloader.loader.mod.entrypoint.client.ClientModInit;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;

public abstract class SimplyTypingClient implements ClientModInit {
    public static boolean reload = true;
    public static boolean isCommand = false;
    public static boolean openWithSlash = false;
    public static ArrayList<String> suggestions = new ArrayList<>();
    public static ArrayList<String> commands = new ArrayList<>();

    @Unique
    public static void MakeSuggestions(String inputText){
        if (inputText.startsWith("/") && !inputText.equals("/")){
            isCommand = true;
            for (String each : commands) {
                if (each.startsWith(inputText.replaceFirst("/",""))) {
                    suggestions.add(each);
                }
            }
        } else{
            isCommand = false;
        }
    }

    @Unique
    public static void clearCommands(){
        commands.clear();
        commands.add("reload");
    }
}

