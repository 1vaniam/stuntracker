# SkillKeybind

A Fabric client-side mod for **1.21.11** that turns hotbar slot-based skills into customizable keybinds.

## What it does

- **Skill keybinds** — Press a key to enter skill mode, cast the skill, and exit automatically. No need to manually switch hotbar slots.
- **Stun tracker** — Tracks Speed II + Invisibility II for the Awakened Assassin class and shows a HUD indicator when stun is ready or on cooldown.
- **Class detection** — Automatically detects your class by reading the item you click in the character selection screen.
- **Class HUD** — Shows your current class on screen. Position is draggable.

## Requirements

- Minecraft 1.21.11
- [Fabric Loader](https://fabricmc.net/) 0.18.4+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)
- [ModMenu](https://modrinth.com/mod/modmenu) (optional, for the config screen)

## Building

```
.\gradlew.bat build
```

Output: `build/libs/stun-tracker-1.0.0.jar` — drop into your `mods` folder.

## Configuration

Open ModMenu → SkillKeybind to access the config screen.

- **Skill keys** — Click any skill button to bind it to a key. Default: Z, X, C, V, B, N.
- **Skill Mode Key** — Optional manual toggle for skill mode. Unbound by default.
- **Class HUD** — Toggle visibility and click "Edit Position" to drag it anywhere on screen.
- **Debug Mode** — When enabled, prints item name and lore to chat on every inventory click. Useful for troubleshooting class detection.

## Notes

- Stun tracker and stun HUD only activate when playing as Awakened Assassin.
- Class is detected automatically when you click your character head in the selection screen.
- Skill casting works by sending slot-switch packets to the server — the server plugin reads the slot number to determine which skill to activate.
