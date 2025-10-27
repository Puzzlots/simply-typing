package io.github.spicylemon2623.SimplyTyping.mixins;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.mojang.brigadier.tree.CommandNode;
import finalforeach.cosmicreach.gamestates.*;
import io.github.spicylemon2623.SimplyTyping.Constants;
import io.github.spicylemon2623.SimplyTyping.SimplyTypingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public abstract class InGameMixin {

    @Inject(method = "onSwitchTo",at = @At("TAIL"))
    public void onSwitchTo(CallbackInfo ci) {
        if (SimplyTypingClient.reload){
            SimplyTypingClient.clearCommands();
            CommandManager.initCommands();

            CommandNode<ServerCommandSource> root = CommandManager.DISPATCHER.getRoot();


            for (CommandNode<ServerCommandSource> child : root.getChildren()) {
                SimplyTypingClient.commands.add(child.getName());

                String base = child.getName();
                String text = ("Loaded: "+base);
                Constants.LOGGER.info(text);
            }

        }
    }

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
