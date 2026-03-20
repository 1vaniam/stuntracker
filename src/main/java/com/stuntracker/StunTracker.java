package com.stuntracker;

import com.stuntracker.config.StunConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

public final class StunTracker {

    private static int cooldownDebounceCounter = 0;

    private StunTracker() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(StunTracker::tick);
    }

    private static void tick(Minecraft mc) {
        StunConfig cfg = StunConfig.get();
        if (!cfg.enabled) return;
        if (mc.player == null || mc.level == null) return;

        // Stun tracking is only relevant for Awakened Assassin
        if (ClassDetector.getCurrentClass() != ClassDetector.PlayerClass.AWAKENED_ASSASSIN
                && ClassDetector.getCurrentClass() != ClassDetector.PlayerClass.NONE) {
            // Another class is detected and thun reset stun state silently
            if (StunState.get() != StunState.Status.UNKNOWN) {
                StunState.set(StunState.Status.UNKNOWN);
            }
            return;
        }

        if (!StunTrackerDirtyFlag.consumeDirty()
                && StunState.get() != StunState.Status.UNKNOWN
                && cooldownDebounceCounter == 0) return;

        boolean effectsReady = checkEffects(mc.player);

        if (effectsReady) {
            cooldownDebounceCounter = 0;
            if (StunState.set(StunState.Status.READY)) onBecameReady(mc, cfg);
        } else {
            if (StunState.isReady()) {
                cooldownDebounceCounter++;
                if (cooldownDebounceCounter >= cfg.cooldownDebounce) {
                    cooldownDebounceCounter = 0;
                    if (StunState.set(StunState.Status.COOLDOWN)) onBecameCooldown(mc, cfg);
                }
            }
        }
    }

    private static boolean checkEffects(LocalPlayer player) {
        Collection<MobEffectInstance> active = player.getActiveEffects();
        return hasEffectById(active, "minecraft:speed", 1)
            && hasEffectById(active, "minecraft:invisibility", 1);
    }

    private static boolean hasEffectById(Collection<MobEffectInstance> effects, String id, int minAmplifier) {
        for (MobEffectInstance instance : effects) {
            Holder<MobEffect> holder = instance.getEffect();
            if (holder.getRegisteredName().equals(id) && instance.getAmplifier() >= minAmplifier)
                return true;
        }
        return false;
    }

    private static void onBecameReady(Minecraft mc, StunConfig cfg) {
        if (cfg.showSubtitle) {
            mc.gui.setTitle(Component.empty());
            mc.gui.setSubtitle(Component.literal("§aStun Ready"));
            mc.gui.setTimes(0, 40, 10);
        }
        if (cfg.playReadySound && mc.player != null)
            mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 2.0f);
    }

    private static void onBecameCooldown(Minecraft mc, StunConfig cfg) {
        if (cfg.showSubtitle) {
            mc.gui.setTitle(Component.empty());
            mc.gui.setSubtitle(Component.literal("§cStun Cooldown"));
            mc.gui.setTimes(0, 40, 10);
        }
        if (cfg.playCooldownSound && mc.player != null)
            mc.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0f, 0.5f);
    }
}
