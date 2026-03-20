package com.stuntracker.mixin;

import com.stuntracker.config.StunConfig;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses specific server messages that are triggered by the mod's
 * slot-switching packets (e.g. "No spells on hotbar. Use /cast bind...").
 */
@Mixin(ChatComponent.class)
public class ChatFilterMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void stuntracker$filterMessage(Component message, CallbackInfo ci) {
        if (!StunConfig.get().filterSpamMessages) return;

        String plain = message.getString().toLowerCase();

        // Suppress known slot switchin spam messages
        if (plain.contains("no spells on hotbar") ||
            plain.contains("use /cast bind")) {
            ci.cancel(); // Block the message from appearing in chat
        }
    }
}
