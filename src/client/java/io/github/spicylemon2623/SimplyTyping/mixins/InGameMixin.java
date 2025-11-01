package io.github.spicylemon2623.SimplyTyping.mixins;

import finalforeach.cosmicreach.ClientSingletons;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.chat.ChatMessage;
import finalforeach.cosmicreach.gamestates.*;
import finalforeach.cosmicreach.networking.client.ChatSender;
import io.github.spicylemon2623.SimplyTyping.SimplyTypingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(InGame.class)
public abstract class InGameMixin {

    @Inject(method = "onSwitchTo",at = @At("TAIL"))
    public void onSwitchTo(CallbackInfo ci) {
        if (SimplyTypingClient.reload){
            SimplyTypingClient.clearCommands();
            Chat chat = Chat.MAIN_CLIENT_CHAT;
            String inputText = "/?";
            ChatSender.sendMessageOrCommand(chat, ClientSingletons.ACCOUNT, inputText);

            ChatMessage message = chat.getLastMessage(0);
            chat.clear();
            String text = message.messageText();
            String sender = message.getSenderName();

            if (sender == null) {
                Pattern pattern = Pattern.compile("/(\\w+)");
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    SimplyTypingClient.commands.add(matcher.group(1));
                }
            }
        }
    }
//    public void onSwitchTo(CallbackInfo ci) {
//        if (SimplyTypingClient.reload){
//            SimplyTypingClient.clearCommands();
////            CommandManager.initCommands();
////
////            CommandNode<ServerCommandSource> root = CommandManager.DISPATCHER.getRoot();
////
////
////            for (CommandNode<ServerCommandSource> child : root.getChildren()) {
////                SimplyTypingClient.commands.add(child.getName());
////
////                String base = child.getName();
////                String text = ("Loaded: "+base);
////                Constants.LOGGER.info(text);
////            }
//
//        }
//    }

    @Inject(method = "switchAwayTo",at = @At("TAIL"))
    public void switchAwayTo(GameState gameState, CallbackInfo ci) {
        if ((gameState instanceof PauseMenu) || (gameState instanceof LoadingGame) || (gameState instanceof YouDiedMenu) || (gameState instanceof ChatMenu)){
            SimplyTypingClient.reload = false;
        } else {
            SimplyTypingClient.reload = true;
            SimplyTypingClient.clearCommands();
        }
    }
}
