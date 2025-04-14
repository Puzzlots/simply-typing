package io.github.spicylemon2623.SimplyTyping;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;

public class SimplyTypingClient implements ClientModInitializer {
    public static boolean reload = true;
    public static boolean isCommand = false;
    public static boolean openWithSlash = false;
    public static ArrayList<String> suggestions = new ArrayList<String>();
    public static ArrayList<String> commands = new ArrayList<String>();

    @Override
    public void onInit() {
        Constants.LOGGER.info("Simply Typing Initialized!");
        Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM,false);
    }

    @Unique
    public static void MakeSuggestions(String inputText){
        if (inputText.startsWith("/")){
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

