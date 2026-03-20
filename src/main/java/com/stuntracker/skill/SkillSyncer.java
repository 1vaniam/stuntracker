package com.stuntracker.skill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class SkillSyncer {

    private static final Logger LOGGER = LoggerFactory.getLogger("SkillKeybind/Syncer");

    public static final int[] SKILL_SLOT_INDICES = { 7, 8, 17, 26, 35, 44, 53 };

    private static final String[] skillNames = new String[]{"","","","","","",""};
    private static boolean synced = false;
    private static int pendingReadTicks = -1;

    // Icon map 
    private static final Map<String, String> ICON_MAP;
    private static final String PLACEHOLDER_PATH = "textures/gui/skill/placeholder.png";
    private static final Map<Integer, Identifier> ICON_CACHE = new HashMap<>();

    static {
        ICON_MAP = new HashMap<String, String>();
        // Assassin
        ICON_MAP.put("deadly calm",   "textures/gui/skill/assassin/deadly_calm.png");
        ICON_MAP.put("lethal combo",  "textures/gui/skill/assassin/lethal_combo.png");
        ICON_MAP.put("ravaging dash", "textures/gui/skill/assassin/ravaging_dash.png");
        ICON_MAP.put("death bloom",   "textures/gui/skill/assassin/death_bloom.png");
        ICON_MAP.put("shadowquake",   "textures/gui/skill/assassin/shadowquake.png");
        ICON_MAP.put("crimson arc",   "textures/gui/skill/assassin/crimson_arc.png");
        ICON_MAP.put("last dance",    "textures/gui/skill/assassin/last_dance.png");
        // Shaman
        ICON_MAP.put("soul link",          "textures/gui/skill/shaman/passive.png");
        ICON_MAP.put("stance switch",      "textures/gui/skill/shaman/skill1.png");
        ICON_MAP.put("primal combo",       "textures/gui/skill/shaman/skill2.png");
        ICON_MAP.put("ritual totem",       "textures/gui/skill/shaman/skill3.png");
        ICON_MAP.put("echo step",          "textures/gui/skill/shaman/skill4.png");
        ICON_MAP.put("earthen embrace",    "textures/gui/skill/shaman/skill5.png");
        ICON_MAP.put("ancestral hands",    "textures/gui/skill/shaman/skill6.png");
        // Warrior
        ICON_MAP.put("bloodbound barrier",   "textures/gui/skill/warrior/passive.png");
        ICON_MAP.put("brutal combo",         "textures/gui/skill/warrior/skill1.png");
        ICON_MAP.put("berserker's leap",    "textures/gui/skill/warrior/skill2.png");
        ICON_MAP.put("relentless whirlwind", "textures/gui/skill/warrior/skill3.png");
        ICON_MAP.put("vicious strike",       "textures/gui/skill/warrior/skill4.png");
        ICON_MAP.put("bulwark instinct",     "textures/gui/skill/warrior/skill5.png");
        ICON_MAP.put("strike of fury",       "textures/gui/skill/warrior/skill6.png");
        // Mage
        ICON_MAP.put("mana barrier",     "textures/gui/skill/mage/passive.png");
        ICON_MAP.put("sorcery combo",    "textures/gui/skill/mage/skill1.png");
        ICON_MAP.put("teleport strike",  "textures/gui/skill/mage/skill2.png");
        ICON_MAP.put("blazing barrage",  "textures/gui/skill/mage/skill3.png");
        ICON_MAP.put("cryo prison",      "textures/gui/skill/mage/skill4.png");
        ICON_MAP.put("hailpiercer",      "textures/gui/skill/mage/skill5.png");
        ICON_MAP.put("meteor of doom",   "textures/gui/skill/mage/skill6.png");
        // Summoner
        ICON_MAP.put("spirit wolf",         "textures/gui/skill/summoner/passive.png");
        ICON_MAP.put("soul combo",          "textures/gui/skill/summoner/skill1.png");
        ICON_MAP.put("blade wheel",         "textures/gui/skill/summoner/skill2.png");
        ICON_MAP.put("summon minion",       "textures/gui/skill/summoner/skill3.png");
        ICON_MAP.put("summoner's command", "textures/gui/skill/summoner/skill4.png");
        ICON_MAP.put("soul spear",          "textures/gui/skill/summoner/skill5.png");
        ICON_MAP.put("summon dragon",       "textures/gui/skill/summoner/skill6.png");
    }

    private SkillSyncer() {}

    // Sync logic

    public static void openAndSync() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return;
        ICON_CACHE.clear(); // Clear stale icons so re-sync picks up new slot order
        mc.player.connection.sendCommand("skills");
        pendingReadTicks = 10;
    }

    public static void tick() {
        if (pendingReadTicks < 0) return;
        pendingReadTicks--;
        if (pendingReadTicks > 0) return;
        pendingReadTicks = -1;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AbstractContainerScreen<?> screen) {
            readSlots(screen, mc);
        } else {
            LOGGER.warn("[SkillKeybind] Sync failed — /skills GUI did not open.");
        }
    }

    private static void readSlots(AbstractContainerScreen<?> screen, Minecraft mc) {
        var container = screen.getMenu();
        int total = container.slots.size();
        int found = 0;

        for (int i = 0; i < 7; i++) {
            int slotIdx = SKILL_SLOT_INDICES[i];
            skillNames[i] = "";
            if (slotIdx >= total) continue;

            ItemStack stack = container.slots.get(slotIdx).getItem();
            if (!stack.isEmpty()) {
                String displayName = stack.getHoverName().getString();
                LOGGER.info("[SkillKeybind] Slot {} (container {}): '{}'", i, slotIdx, displayName);

                // Always try lore first — the actual skill name is stored there
                String loreSkill = readSkillFromLore(stack);
                if (loreSkill != null && !loreSkill.isEmpty()) {
                    skillNames[i] = loreSkill;
                    found++;
                } else if (!displayName.toLowerCase().startsWith("skill slot")
                        && !displayName.equalsIgnoreCase("empty")) {
                    // Fallback: use display name if it's not a placeholder
                    skillNames[i] = displayName;
                    found++;
                }
                // else: empty/unequipped slot, leave as ""
            }
        }

        synced = found > 0;
        if (synced) {
            mc.setScreen(null);
            StringBuilder sb = new StringBuilder("[SkillKeybind] Synced " + found + "/7 slots: ");
            for (int i = 0; i < 7; i++) {
                if (!skillNames[i].isEmpty()) sb.append("[").append(i).append(":").append(skillNames[i]).append("] ");
            }
            LOGGER.info(sb.toString());
        } else {
            LOGGER.warn("[SkillKeybind] No skills found in expected slots.");
        }
    }

    // Icon resolution

    public static Identifier getIconForSlot(int hudSlot) {
        if (ICON_CACHE.containsKey(hudSlot)) {
            return ICON_CACHE.get(hudSlot);
        }
        String name = getSkillName(hudSlot);
        Identifier result;
        if (name != null && !name.isEmpty()) {
            // Strip Minecraft formatting codes (§X) before lookup
            String clean = name.replaceAll("§[0-9a-fk-or]", "").toLowerCase().trim();
            LOGGER.info("[SkillKeybind] Icon lookup slot {}: '{}' → clean='{}'", hudSlot, name, clean);
            String path = ICON_MAP.get(clean);
            if (path != null) {
                result = Identifier.fromNamespaceAndPath("stuntracker", path);
            } else {
                LOGGER.warn("[SkillKeybind] No icon mapped for '{}'", clean);
                result = Identifier.fromNamespaceAndPath("stuntracker", PLACEHOLDER_PATH);
            }
        } else {
            result = Identifier.fromNamespaceAndPath("stuntracker", PLACEHOLDER_PATH);
        }
        ICON_CACHE.put(hudSlot, result);
        return result;
    }

    // Accessors

    public static boolean isSynced() { return synced; }

    public static String getSkillName(int slot) {
        if (slot < 0 || slot >= 7) return "";
        return skillNames[slot] != null ? skillNames[slot] : "";
    }

    public static void reset() {
        synced = false;
        ICON_CACHE.clear();
        for (int i = 0; i < 7; i++) skillNames[i] = "";
    }

    /**
     * Reads the first non-empty lore line from an item stack.
     * The server plugin stores the skill name in lore for "Skill Slot" items.
     */
    private static String readSkillFromLore(ItemStack stack) {
        net.minecraft.world.item.component.ItemLore lore =
            stack.get(net.minecraft.core.component.DataComponents.LORE);
        if (lore == null) return null;

        for (net.minecraft.network.chat.Component line : lore.lines()) {
            String text = line.getString().trim();
            if (!text.isEmpty()
                    && !text.equalsIgnoreCase("skill slot")
                    && !text.equalsIgnoreCase("empty")) {

                // Strip "Current Skill: " prefix if present (server sends lore as "Current Skill: <name>")
                String lower = text.toLowerCase();
                if (lower.startsWith("current skill: ")) {
                    text = text.substring("current skill: ".length()).trim();
                }

                return text;
            }
        }
        return null;
    }
}
