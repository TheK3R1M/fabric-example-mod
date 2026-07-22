# Custom Beacon Overhaul Mod - Application Architecture Specification

## 1. Overview
This specification outlines the architecture for a data-driven Minecraft mod that overhauls the Vanilla Beacon system. The mod expands beacon capabilities to support algorithmic tier scaling, dynamic payment inflation, custom multi-block networks, and advanced GUI features, while remaining perfectly balanced and retaining the vanilla fallback state.

## 2. Core Mechanics
*   **Algorithmic Tier Scaling:** The pyramid base size is no longer limited to 4 vanilla tiers. It scales algorithmically using the formula: Base Width = 2n + 1 (where 'n' is the tier level).
*   **Master-Slave Network:**
    *   Beacons can be linked. A central "Master" beacon is powered by a pyramid structure.
    *   "Slave" (Support) beacons placed around the Master (e.g., at corners) connect to the Master and amplify its power/allow multiple effects.
    *   Slave beacons do NOT require the vanilla 3x3 base to function; they only require an active connection to a Master beacon. If the Master is broken or loses tier power, Slaves revert to an inactive/vanilla state.
    *   Visual FX: Slave beacons emit a targeted beam/particle stream towards the Master beacon.
*   **Expanded Capabilities:**
    *   **AoE Range:** Beacon area-of-effect range scales dynamically with custom tiers.
    *   **Multi-Effect Selection:** Players can select 2 or more primary effects simultaneously depending on the network's power.
    *   **Secondary Effect Toggle:** The vanilla forced "Regeneration" as a secondary effect is now removable/optional.

## 3. Dynamic Payment & Balancing System
*   **Material Inflation System:** Payment costs scale exponentially with effect strength to prevent OP mechanics (e.g., Strength 10).
    *   *Progression:* Iron Ingot -> Gold Ingot -> Emerald -> Diamond -> Netherite Scrap/Ingot.
    *   *Stack Caps & Evolution:* When a requested material hits a stack limit (64x), the required item evolves into its **Block variant** (e.g., 64 Iron Ingots -> Iron Blocks). When the Block variant hits its cap (64x), the system shifts to requiring **Nether Stars**.
*   **Anti-OP Protocol:** Certain effects (like Strength and Resistance) have strict hard caps defined in the config. Their payment curve is significantly steeper than utility effects like Haste.
*   **Vanilla Fallback:** If the user does not select custom max parameters, the beacon operates exactly like a standard Vanilla beacon with standard costs.

## 4. Extended GUI (Graphical User Interface)
The `BeaconScreen` must be overhauled to handle the new scaling parameters:
*   **Scrollable Effect Menu:** A vertical scrollbar must be implemented in the effect selection area to accommodate custom effects and higher levels (Level V, X, etc.).
*   **Blueprint / Helper Panel:** A dynamic information panel rendered on the left side of the GUI. Based on the selected effects and multiplier, it displays:
    *   The required pyramid tier.
    *   The exact total number of blocks (in stacks) needed to build the base.
*   **Visual Representation:** For any beacon pyramid that reaches Tier 9 or above, the UI will utilize the "Max Vanilla Beacon" pyramid icon as its visual indicator.
*   **Dynamic Tribute Slots:** The GUI must dynamically handle high-cost payments, either by expanding tribute slots or utilizing an internal inventory logic to accept large stacks of blocks/Nether Stars.

## 5. Configuration & Management
*   **Admin Controls:** A custom command structure (e.g., `/custombeacon`) that functions similarly to gamerules.
*   **Config File (`config.json`):** Must dynamically store and read allowed minerals, multiplier scales, hard caps for effects, and payment thresholds. Changes made in-game must save to this file seamlessly.
