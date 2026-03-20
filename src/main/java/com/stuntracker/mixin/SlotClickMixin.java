package com.stuntracker.mixin;

import com.stuntracker.ClassDetector;
import com.stuntracker.config.StunConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class SlotClickMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"))
    private void stuntracker$onSlotClicked(Slot slot, int slotId, int mouseButton,
                                           ClickType clickType, CallbackInfo ci) {
        if (slot == null || !slot.hasItem()) return;

        ItemStack item = slot.getItem();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Debug output — only shown when Debug Mode is ON in config
        if (StunConfig.get().debugMode) {
            String name = item.getHoverName().getString();
            mc.player.displayClientMessage(
                Component.literal("§7[Debug] Name: \"" + name + "\""), false);

            ItemLore lore = item.get(DataComponents.LORE);
            if (lore != null) {
                for (int i = 0; i < lore.lines().size(); i++) {
                    String line = lore.lines().get(i).getString();
                    mc.player.displayClientMessage(
                        Component.literal("§7[Debug] Lore[" + i + "]: \"" + line + "\""), false);
                }
            } else {
                mc.player.displayClientMessage(
                    Component.literal("§7[Debug] No lore found"), false);
            }
        }

        ClassDetector.onItemClicked(item);
    }
}
