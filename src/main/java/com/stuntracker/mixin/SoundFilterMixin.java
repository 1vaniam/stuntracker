package com.stuntracker.mixin;

import com.stuntracker.config.StunConfig;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels specific ambient/nuisance sounds before they are queued.
 * SoundEngine.play() returns a value in 1.21.x so we use CallbackInfoReturnable.
 */
@Mixin(SoundEngine.class)
public class SoundFilterMixin {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void stuntracker$filterSounds(SoundInstance sound, CallbackInfoReturnable<?> ci) {
        StunConfig cfg = StunConfig.get();

        // Read 'location' field via reflection from AbstractSoundInstance
        String path;
        try {
            java.lang.reflect.Field f = sound.getClass().getSuperclass().getDeclaredField("location");
            f.setAccessible(true);
            Object loc = f.get(sound);
            if (loc == null) return;
            path = loc.toString(); // "minecraft:block.end_portal_frame.fill"
        } catch (Exception e) {
            return;
        }

        // Ender eye inserted into end portal frame
        if (cfg.filterEndPortalSound && path.equals("minecraft:block.end_portal_frame.fill")) {
            ci.cancel();
            return;
        }

        // Fire / lava extinguish
        if (cfg.filterExtinguishSound
                && (path.equals("minecraft:block.fire.extinguish")
                    || path.equals("minecraft:block.lava.extinguish"))) {
            ci.cancel();
        }
    }
}
