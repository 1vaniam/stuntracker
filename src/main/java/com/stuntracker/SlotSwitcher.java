package com.stuntracker;

import com.stuntracker.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;

public final class SlotSwitcher {

    private SlotSwitcher() {}

    public static void switchTo(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.getConnection() == null) return;

        LocalPlayer player = mc.player;
        int target = Math.max(0, Math.min(8, slot));

        InventoryAccessor inv = (InventoryAccessor) player.getInventory();
        if (inv.getSelected() == target) return;

        inv.setSelected(target);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(target));
    }
}
