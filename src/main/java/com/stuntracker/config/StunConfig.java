package com.stuntracker.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;

@Config(name = "stuntracker")
public class StunConfig implements ConfigData {

    @ConfigEntry.Gui.Tooltip
    public boolean enabled = true;

    // Stun HUD (Assassin only)
    @ConfigEntry.Gui.Tooltip
    public boolean showHud = true;
    public int stunHudX = 4;
    public int stunHudY = 4;
    @ConfigEntry.Gui.Tooltip
    public float stunHudScale = 1.0f;

    @ConfigEntry.Gui.Tooltip
    public boolean showSubtitle = false;

    @ConfigEntry.Gui.Tooltip
    public boolean playReadySound = true;

    @ConfigEntry.Gui.Tooltip
    public boolean playCooldownSound = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 40)
    public int cooldownDebounce = 5;

    // Class HUD
    @ConfigEntry.Gui.Tooltip
    public boolean showClassHud = true;
    public int classHudX = 4;
    public int classHudY = 20;
    @ConfigEntry.Gui.Tooltip
    public float classHudScale = 1.0f;

    // Shaman HUD
    @ConfigEntry.Gui.Tooltip
    public boolean showShamanHud = true;
    public int shamanHudX = 4;
    public int shamanHudY = 36;
    @ConfigEntry.Gui.Tooltip
    public float shamanHudScale = 1.0f;

    //Skill Bar HUD
    @ConfigEntry.Gui.Tooltip
    public boolean showSkillBar = true;
    public int skillBarX = 0;
    public int skillBarY = 0;
    @ConfigEntry.Gui.Tooltip
    public float skillBarScale = 1.0f;

    // Skill keybinds
    @ConfigEntry.Gui.Tooltip
    public int skillModeKey = GLFW.GLFW_KEY_UNKNOWN;

    @ConfigEntry.Gui.Tooltip public int skillKey1 = GLFW.GLFW_KEY_Z;
    @ConfigEntry.Gui.Tooltip public int skillKey2 = GLFW.GLFW_KEY_X;
    @ConfigEntry.Gui.Tooltip public int skillKey3 = GLFW.GLFW_KEY_C;
    @ConfigEntry.Gui.Tooltip public int skillKey4 = GLFW.GLFW_KEY_V;
    @ConfigEntry.Gui.Tooltip public int skillKey5 = GLFW.GLFW_KEY_B;
    @ConfigEntry.Gui.Tooltip public int skillKey6 = GLFW.GLFW_KEY_M;

    // Debug / Filter
    @ConfigEntry.Gui.Tooltip
    public boolean debugMode = false;

    @ConfigEntry.Gui.Tooltip
    public boolean filterSpamMessages = true;

    @ConfigEntry.Gui.Tooltip
    public boolean filterEndPortalSound = true;

    @ConfigEntry.Gui.Tooltip
    public boolean filterExtinguishSound = true;

    public int[] getSkillKeys() {
        // Index 0=Skill1(server slot 0), ..., 5=Skill6(slot 5), 6=Ultimate(slot 6)
        // Index 0=Skill1(slot0), 1=Skill2(slot1), ..., 4=Skill5(slot4), 5=Ultimate(slot5)
        return new int[]{ skillKey1, skillKey2, skillKey3, skillKey4, skillKey5, skillKey6 };
    }

    // Internal fixed values
    public final boolean requireSpeed = true;
    public final int speedMinAmplifier = 1;
    public final boolean requireInvisibility = true;
    public final int invisibilityMinAmplifier = 1;

    public static StunConfig get() {
        return AutoConfig.getConfigHolder(StunConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(StunConfig.class, GsonConfigSerializer::new);
    }
}
