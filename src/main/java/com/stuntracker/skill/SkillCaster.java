package com.stuntracker.skill;

import com.stuntracker.mixin.InventoryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class SkillCaster {

    private static boolean skillModeActive = false;
    private static int slotBeforeSkillMode = 0;

    private SkillCaster() {}

    public static boolean isSkillModeActive() { return skillModeActive; }

    /**
     * Enters skill mode (manual toggle key).
     * Only sends the offhand packet to signal the server — slot switching
     * happens separately in castSkill().
     */
    public static void enterSkillMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) return;
        if (skillModeActive) return;

        slotBeforeSkillMode = ((InventoryAccessor) mc.player.getInventory()).getSelected();
        skillModeActive = true;
    }

    /**
     * Exits skill mode silently. no offhand packet.
     * Restores the player's previous hotbar slot.
     */
    public static void exitSkillMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) return;
        if (!skillModeActive) return;

        skillModeActive = false;
        ((InventoryAccessor) mc.player.getInventory()).setSelected(slotBeforeSkillMode);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(slotBeforeSkillMode));
    }

    /**
     * Casts a skill: switch to the skill slot, send the offhand packet (cast trigger),
     * then restore the previous slot.
     *
     * The server reads which hotbar slot the client switches *to* as the skill index.
     * If the player is sitting on the slot that would be the target, that counts as
     * "no switch" and the server never fires the skill. To avoid this, we skip the
     * player's current slot: any skillIndex >= currentSlot gets bumped up by 1.
     *
     * Examplol player on slot 0:
     *   skillIndex 0 → slot 1  (skip 0)
     *   skillIndex 1 → slot 2
     *   skillIndex 5 → slot 6
     *
     * Exampll player on slot 3:
     *   skillIndex 0 → slot 0  (before 3, no bump)
     *   skillIndex 1 → slot 1
     *   skillIndex 2 → slot 2
     *   skillIndex 3 → slot 4  (skip 3)
     *   skillIndex 5 → slot 6
     */
    public static void castSkill(int skillIndex) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.player == null) return;

        int savedSlot = ((InventoryAccessor) mc.player.getInventory()).getSelected();

        // Skip the current hotbar slot so the server always sees a real slot change.
        int slot = (skillIndex >= savedSlot) ? skillIndex + 1 : skillIndex;
        slot = Math.max(0, Math.min(8, slot));

        // Server expects: offhand packet → slot switch → offhand packet
        mc.getConnection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
            BlockPos.ZERO, Direction.DOWN));

        ((InventoryAccessor) mc.player.getInventory()).setSelected(slot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));

        mc.getConnection().send(new ServerboundPlayerActionPacket(
            ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
            BlockPos.ZERO, Direction.DOWN));

        // Restore previous slot
        ((InventoryAccessor) mc.player.getInventory()).setSelected(savedSlot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(savedSlot));

        // Notify HUD: use skillIndex (not the offset slot) so the highlight always
        // matches the skill that was actually cast. +1 because HUD slot 0 = passive.
        SkillHudRenderer.notifyCast(skillIndex + 1);
    }

    /** Toggle for the manual skill mode key — enters or exits skill mode. */
    public static void toggleSkillMode() {
        if (skillModeActive) {
            exitSkillMode();
        } else {
            enterSkillMode();
        }
    }
}
