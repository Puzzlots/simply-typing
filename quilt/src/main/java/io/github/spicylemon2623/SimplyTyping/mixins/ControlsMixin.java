package io.github.spicylemon2623.SimplyTyping.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import finalforeach.cosmicreach.settings.Controls;
import io.github.spicylemon2623.SimplyTyping.SimplyTyping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Controls.class)
public class ControlsMixin {

    @Inject(method = "chatOpened", at = @At("RETURN"), cancellable = true)
    private static void chatOpened(CallbackInfoReturnable<Boolean> cir) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SLASH)) {
            SimplyTyping.openWithSlash = true;
            cir.setReturnValue(true);
        }
    }
}
