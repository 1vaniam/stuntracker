package com.stuntracker;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public final class ClassDetector {

    public enum PlayerClass {
        NONE,
        AWAKENED_ARCHER,
        AWAKENED_MAGE,
        AWAKENED_WARRIOR,
        AWAKENED_SHAMAN,
        AWAKENED_SUMMONER,
        AWAKENED_ASSASSIN;

        public String displayName() {
            return switch (this) {
                case AWAKENED_ARCHER   -> "Awakened Archer";
                case AWAKENED_MAGE     -> "Awakened Mage";
                case AWAKENED_WARRIOR  -> "Awakened Warrior";
                case AWAKENED_SHAMAN   -> "Awakened Shaman";
                case AWAKENED_SUMMONER -> "Awakened Summoner";
                case AWAKENED_ASSASSIN -> "Awakened Assassin";
                case NONE              -> "None";
            };
        }
    }

    private static PlayerClass currentClass = PlayerClass.NONE;

    private ClassDetector() {}

    public static PlayerClass getCurrentClass() { return currentClass; }

    public static void onItemClicked(ItemStack stack) {
        if (stack.isEmpty()) return;

        // Check display name first
        String name = stack.getHoverName().getString().toLowerCase();
        PlayerClass detected = detectFromString(name);

        // If not found in name, check lore lines
        if (detected == PlayerClass.NONE) {
            ItemLore lore = stack.get(DataComponents.LORE);
            if (lore != null) {
                for (Component line : lore.lines()) {
                    String loreLine = line.getString().toLowerCase();
                    detected = detectFromString(loreLine);
                    if (detected != PlayerClass.NONE) break;
                }
            }
        }

        if (detected != PlayerClass.NONE && detected != currentClass) {
            currentClass = detected;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(
                    Component.literal("§6[SkillKeybind] §fClass detected: §a" + detected.displayName()),
                    false
                );
            }
        }
    }

    private static PlayerClass detectFromString(String text) {
        if (text.contains("awakened archer"))   return PlayerClass.AWAKENED_ARCHER;
        if (text.contains("awakened mage"))     return PlayerClass.AWAKENED_MAGE;
        if (text.contains("awakened warrior"))  return PlayerClass.AWAKENED_WARRIOR;
        if (text.contains("awakened shaman"))   return PlayerClass.AWAKENED_SHAMAN;
        if (text.contains("awakened summoner")) return PlayerClass.AWAKENED_SUMMONER;
        if (text.contains("awakened assassin")) return PlayerClass.AWAKENED_ASSASSIN;
        return PlayerClass.NONE;
    }
}
