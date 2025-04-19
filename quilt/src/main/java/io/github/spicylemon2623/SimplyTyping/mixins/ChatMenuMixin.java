package io.github.spicylemon2623.SimplyTyping.mixins;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import finalforeach.cosmicreach.gamestates.ChatMenu;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.ui.FontRenderer;
import finalforeach.cosmicreach.ui.HorizontalAnchor;
import finalforeach.cosmicreach.ui.VerticalAnchor;
import io.github.spicylemon2623.SimplyTyping.Constants;
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
    int simplyTyping$selected = 0;

    @Unique
    int simplyTyping$suggestionSize = 0;

    @Inject(method = "keyTyped",at = @At("TAIL"))
    public void keyTyped(char character, CallbackInfoReturnable<Boolean> cir){
        suggestions.clear();
        MakeSuggestions(inputText);
        if (!(character == '\t')){
            simplyTyping$selected = 0;
        }
    }

    @Inject(method = "keyTyped",at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    public void keyDelete(char character, CallbackInfoReturnable<Boolean> cir){
        suggestions.clear();
        MakeSuggestions(inputText);
        if (!(character == '\t')){
            simplyTyping$selected = 0;
        }
    }

    @Inject(method = "updateRepeatMessageIdx", at = @At("HEAD"), cancellable = true)
    public void updateRepeatMessageIdxHead(int offset, CallbackInfo ci){
        simplyTyping$selected += offset;
        simplyTyping$selected = Math.max(0, Math.min(simplyTyping$selected, suggestions.size() - 1));
        if (simplyTyping$selected != 0) {
            ci.cancel();
        }
    }

    @Inject(method = "updateRepeatMessageIdx", at = @At("TAIL"))
    public void updateRepeatMessageIdxTail(int offset, CallbackInfo ci){
        suggestions.clear();
        MakeSuggestions(inputText);
    }

    @Unique
    public boolean simplyTyping$isSelected(int suggestionsIdx){
        return simplyTyping$selected == suggestionsIdx;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/g2d/SpriteBatch;end()V", shift = At.Shift.BEFORE))
    private void render(CallbackInfo ci) {
        if (SimplyTypingClient.isCommand) {
            Iterator<String> iterator = SimplyTypingClient.suggestions.iterator();

            simplyTyping$suggestionSize = suggestions.size();

            float sugY = 0;
            float selY = 0;
            float selX = 0;
            float sugX;

            Vector2 preSelectedVec = new Vector2();
            Vector2 suggestionsVec = new Vector2();
            Vector2 suggestionVec = new Vector2();

            FontRenderer.getTextDimensions(this.uiViewport, "> " + inputText, preSelectedVec);

            String preSelectedTextToRender = " ";

            int suggestionsIdx = 0;
            String selectedTextToRender = "";

            boolean tabPressed = Gdx.input.isKeyJustPressed(61);
            boolean hasOneExactMatch = SimplyTypingClient.suggestions.size() == 1 &&
                    SimplyTypingClient.suggestions.contains(inputText.substring(1));

            if ((tabPressed || hasOneExactMatch) && !SimplyTypingClient.suggestions.isEmpty()) {
                if (simplyTyping$selected >= 0 && simplyTyping$selected < SimplyTypingClient.suggestions.size()) {
                    inputText = "/" + SimplyTypingClient.suggestions.get(simplyTyping$selected);
                    desiredCharIdx = inputText.length();
                    SimplyTypingClient.suggestions.clear();
                }
            }

            while (iterator.hasNext()) {
                if (!inputText.isEmpty()) {

                    String suggestionTextToRender = iterator.next();

                    if (suggestionsIdx == simplyTyping$selected) {
                        preSelectedTextToRender = suggestionTextToRender;
                    }

                    FontRenderer.getTextDimensions(this.uiViewport, suggestionTextToRender, suggestionVec);

                    try {
                        selectedTextToRender = preSelectedTextToRender.substring(inputText.length() - 1);
                    } catch (Exception e) {
                        Constants.LOGGER.error(String.valueOf(e));
                    }

                    selX = preSelectedVec.x;


                    //get position for suggestions
                    String suggestionsPos = "/" + (inputText.trim().contains(" ") ? inputText.trim().substring(0, inputText.trim().lastIndexOf(' ') + 1) : "/");
                    FontRenderer.getTextDimensions(this.uiViewport, suggestionsPos, suggestionsVec);
                    sugX = suggestionsVec.x;


                    if (suggestionsIdx == 0) {  //if it's the first run
                        sugY -= preSelectedVec.y;

                        /* Botch job because p's are weird */
                        if (inputText.contains("p")) {
                            selY -= 1f;
                        }
                        if (selectedTextToRender.contains("p")) {
                            selY += 1.5f;
                        }
                    }

                    batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);  //set colour to white
                    if (simplyTyping$isSelected(suggestionsIdx)) {
                        batch.setColor(Color.YELLOW);
                    }

                    // draw suggestions
                    if (!Objects.equals(inputText, "/")) {
                        batch.setColor(batch.getColor().add(-0.75f, -0.75f, -0.75f, 0)); //make text darker
                        FontRenderer.drawText(batch, this.uiViewport, suggestionTextToRender, 10f + sugX, minY - 10.0F + sugY, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw darker text
                        batch.setColor(batch.getColor().add(0.75f, 0.75f, 0.75f, 0));  //set colour back to normal
                        FontRenderer.drawText(batch, this.uiViewport, suggestionTextToRender, 8.0F + sugX, minY - 12.0F + sugY, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw normal text on-top
                    }

                    suggestionsIdx++;
                    sugY -= suggestionVec.y + 2f;
                }

                if (!Objects.equals(inputText, "/")) {
                    //draw selected
                    batch.setColor(Color.SLATE);
                    batch.setColor(batch.getColor().add(-0.25f, -0.25f, -0.25f, 0)); //make text darker
                    FontRenderer.drawText(batch, this.uiViewport, selectedTextToRender, 10f + selX, minY - 10.0F + selY, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw darker text
                    batch.setColor(batch.getColor().add(0.25f, 0.25f, 0.25f, 0));  //set colour back to normal
                    FontRenderer.drawText(batch, this.uiViewport, selectedTextToRender, 8.0F + selX, minY - 12.0F + selY, HorizontalAnchor.LEFT_ALIGNED, VerticalAnchor.BOTTOM_ALIGNED); //draw normal text on-top
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

    @Inject(method = "keyDown(I)Z", at = @At("HEAD"))
    private void beforeSwitchKeyCode(int keycode, CallbackInfoReturnable<Boolean> cir) {
        if ((keycode == 19 || keycode == 20) && !Objects.equals(inputText, "/")) {
            keycode = 0;
        }
    }
}
