package io.github.spicylemon2623.SimplyTyping;


import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PreModInitializer;

public class SimplyTypingPreinit implements PreModInitializer {

    @Override
    public void onPreInit() {
        Constants.LOGGER.info("Simply typing pre-initialised");
    }
}
