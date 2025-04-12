package io.github.spicylemon2623.SimplyTyping;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;

public class SimplyTypingClient implements ClientModInitializer {
    @Override
    public void onInit() {
        Constants.LOGGER.info("Simply Zooming Initialized!");
    }

}
