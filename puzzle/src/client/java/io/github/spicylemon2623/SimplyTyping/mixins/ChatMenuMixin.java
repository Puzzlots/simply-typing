package io.github.spicylemon2623.SimplyTyping.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import io.github.spicylemon2623.SimplyTyping.SimplyTypingClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Objects;

import static finalforeach.cosmicreach.gamestates.ChatMenu.minY;
import static io.github.spicylemon2623.SimplyTyping.SimplyTypingClient.MakeSuggestions;
import static io.github.spicylemon2623.SimplyTyping.SimplyTypingClient.suggestions;
import static io.github.spicylemon2623.SimplyTyping.SimplyTypingClient.isCommand;

@Mixin(ChatMenu.class)
public class ChatMenuMixin extends GameState {

    @Shadow
    String inputText;

    @Shadow
    int desiredCharIdx;

    @Unique
    int selected = 0;

    @Inject(method = "keyTyped",at = @At("TAIL"))
    public void keyTyped(char character, CallbackInfoReturnable<Boolean> cir){
        suggestions.clear();
        MakeSuggestions(inputText);
        if (!(character == '\t')){
            selected = 0;
        }
    }

    @Inject(method = "keyTyped",at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    public void keyDelete(char character, CallbackInfoReturnable<Boolean> cir){
        suggestions.clear();
        MakeSuggestions(inputText);
        if (!(character == '\t')){
            selected = 0;
        }
    }

    @Inject(method = "updateRepeatMessageIdx", at = @At("HEAD"), cancellable = true)
    public void updateRepeatMessageIdxHead(int offset, CallbackInfo ci){
        if (offset == -1){ // down
            if (selected >= 0){
                selected -= 1;
                ci.cancel();
            } else {
                selected -= 1;
            }
        }

        if (offset == 1){ // up
            if (selected > 0){
                selected += 1;
                ci.cancel();
            } else {
                selected += 1;
            }
        }
    }

    @Inject(method = "updateRepeatMessageIdx", at = @At("TAIL"))
    public void updateRepeatMessageIdxTail(int offset, CallbackInfo ci){
        suggestions.clear();
        MakeSuggestions(inputText);
    }

    @Unique
    public boolean isSelected(int suggestionsIdx){
        return selected == suggestionsIdx;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/g2d/SpriteBatch;end()V", shift = At.Shift.BEFORE))
    private void render(CallbackInfo ci) {
        if (isCommand){
            Iterator<String> iterator = suggestions.iterator();

            long msec = System.currentTimeMillis();


            float y = 0;
            float x = 0;

            Vector2 tmpVec = new Vector2();
            Vector2 suggestionsVec = new Vector2();
            Vector2 suggestionVec = new Vector2();

            FontRenderer.getTextDimensions(this.uiViewport, "> "+inputText, tmpVec);
            FontRenderer.getTextDimensions(this.uiViewport, "> /", suggestionsVec);


            int suggestionsIdx = 0;
            while (iterator.hasNext()) {

                String textToRender = iterator.next();
                FontRenderer.getTextDimensions(this.uiViewport, textToRender, suggestionVec);
                String modifiedTextToRender = textToRender;

                if (suggestionsIdx == 0 && inputText.length() - 1 > -1) {  //if it's the first suggestion
                    modifiedTextToRender = textToRender.substring(inputText.length() - 1);
                    x = tmpVec.x;
                }

                if (suggestionsIdx != 0) {  //if it's not the first suggestion ignore the x offset
                    x = suggestionsVec.x;
                }

                if (Objects.equals(inputText, "/") && suggestionsIdx == 0) {  //if the user hasn't started typing the command don't show the first suggestion in the text bar
                    y -= suggestionVec.y;
                }

                /* Botch job because p's are weird */
                if (inputText.contains("p")) {
                    y -= 1f;
                }

                if (modifiedTextToRender.contains("p")) {
                    y += 1.5f;
                }

                batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);  //colour set
                if (isSelected(suggestionsIdx)){
                    batch.setColor(Color.YELLOW);
                }


                Chat.MAIN_CLIENT_CHAT.clear();
                batch.setColor(batch.getColor().add(-0.75f,-0.75f,-0.75f,0)); //make text darker
                FontRenderer.drawText(batch, this.uiViewport, modifiedTextToRender, 10f + x, minY - 10.0F + y, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw darker text

                batch.setColor(batch.getColor().add(0.75f,0.75f,0.75f,0));  //set colour back to normal
                FontRenderer.drawText(batch, this.uiViewport, modifiedTextToRender, 8.0F + x, minY - 12.0F + y, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw normal text on-top

                suggestionsIdx ++;
                y -= suggestionVec.y +2f;
            }

            if (Gdx.input.isKeyJustPressed(61)) {
                if (!suggestions.isEmpty() && selected >= 0 && selected < suggestions.size()) {
                    inputText = "/";
                    inputText = inputText.concat(suggestions.get(selected) + " ");
                    desiredCharIdx = inputText.length();
                    suggestions.clear();
                }
            }
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lfinalforeach/cosmicreach/gamestates/ChatMenu;switchToGameState(Lfinalforeach/cosmicreach/gamestates/GameState;)V", shift = At.Shift.AFTER))
    private void renderSwitchToGameState(CallbackInfo ci) {
        suggestions.clear();
        isCommand = false;
    }

    @Override
    public void onSwitchTo() {
        if (SimplyTypingClient.openWithSlash){
            SimplyTypingClient.openWithSlash = false;
            inputText = "/";
            desiredCharIdx += 1;
            suggestions.clear();
            MakeSuggestions(inputText);
        }
    }
}
