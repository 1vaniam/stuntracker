package com.stuntracker.mixin;

import com.stuntracker.StunTrackerDirtyFlag;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleUpdateMobEffect", at = @At("TAIL"))
    private void stuntracker$onEffectAdded(ClientboundUpdateMobEffectPacket packet, CallbackInfo ci) {
        StunTrackerDirtyFlag.markDirty();
    }

    @Inject(method = "handleRemoveMobEffect", at = @At("TAIL"))
    private void stuntracker$onEffectRemoved(ClientboundRemoveMobEffectPacket packet, CallbackInfo ci) {
        StunTrackerDirtyFlag.markDirty();
    }
}
