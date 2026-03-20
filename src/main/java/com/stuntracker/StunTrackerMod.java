package com.stuntracker;

import com.stuntracker.config.StunConfig;
import com.stuntracker.hud.ClassHudRenderer;
import com.stuntracker.hud.SkillBarRenderer;
import com.stuntracker.hud.StunHudRenderer;
import com.stuntracker.shaman.ShamanHudRenderer;
import com.stuntracker.shaman.ShamanModeTracker;
import com.stuntracker.skill.SkillHudRenderer;
import com.stuntracker.skill.SkillKeyHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.stuntracker.skill.SkillSyncer;

public class StunTrackerMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        StunConfig.register();
        StunTracker.register();
        StunHudRenderer.register();
        ClassHudRenderer.register();
        ShamanModeTracker.register();
        ShamanHudRenderer.register();
        SkillBarRenderer.register();
        SkillKeyHandler.register();
        SkillHudRenderer.register();

        // Preload and verify skill textures once the client is fully started
        ClientTickEvents.END_CLIENT_TICK.register(mc -> SkillSyncer.tick());
        ClientLifecycleEvents.CLIENT_STARTED.register(mc -> TexturePreloader.preloadAll());

        // Reset synced skill names on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> SkillSyncer.reset());
    }
}
