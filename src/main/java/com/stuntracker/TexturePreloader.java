package com.stuntracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class TexturePreloader {

    private static final Logger LOGGER = LoggerFactory.getLogger("SkillKeybind");

    // Assassin textures them named after their actual skills
    private static final String[] ASSASSIN_SLUGS = {
        "deadly_calm", "lethal_combo", "ravaging_dash", "death_bloom",
        "shadowquake",  "crimson_arc",  "last_dance"
    };

    // Other classes still use generic slot names
    private static final String[] OTHER_CLASSES = {"mage", "shaman", "summoner", "archer"};
    private static final String[] GENERIC_SLOTS  = {"passive","skill1","skill2","skill3","skill4","skill5","skill6"};

    private TexturePreloader() {}

    public static void preloadAll() {
        Minecraft mc = Minecraft.getInstance();
        List<Identifier> toLoad = new ArrayList<>();

        toLoad.add(Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/placeholder.png"));

        for (String slug : ASSASSIN_SLUGS)
            toLoad.add(Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/assassin/" + slug + ".png"));

        for (String cls : OTHER_CLASSES)
            for (String slot : GENERIC_SLOTS)
                toLoad.add(Identifier.fromNamespaceAndPath("stuntracker", "textures/gui/skill/" + cls + "/" + slot + ".png"));

        LOGGER.info("[SkillKeybind] Preloading {} textures...", toLoad.size());
        int ok = 0, fail = 0;
        for (Identifier id : toLoad) {
            try {
                AbstractTexture tex = mc.getTextureManager().getTexture(id);
                if (tex != null) ok++; else { LOGGER.warn("[SkillKeybind] NULL: {}", id); fail++; }
            } catch (Exception e) {
                LOGGER.error("[SkillKeybind] FAILED: {} — {}", id, e.getMessage()); fail++;
            }
        }
        LOGGER.info("[SkillKeybind] Done: {} OK, {} failed", ok, fail);
    }
}
