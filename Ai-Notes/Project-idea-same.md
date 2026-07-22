# Custom Beacon Overhaul Mod - Advanced Technical Architecture Specification

## 1. Project Overview & Environment
*   **Mod Loader:** Fabric (Latest target version).
*   **Architecture Type:** Data-driven, Client-Server synchronized, Mixin-heavy structure.
*   **Objective:** Completely overhaul the `BeaconBlockEntity`, `BeaconScreen`, and `BeaconScreenHandler` to support infinite algorithmic tier scaling, multi-block master-slave networks, dynamic payment inflation, and a custom UI.
*   **Rule of Thumb:** Fallback to vanilla behavior seamlessly if the user configuration does not specify custom parameters.

## 2. Core Mechanics & Algorithmic Scaling
### 2.1 Tier Algorithm
*   Abandon the hardcoded 4-tier vanilla limit. Implement a dynamic formula for the pyramid base: `Base Width = 2n + 1` (where 'n' is the tier/level).
*   **Power Value System:** Different mineral blocks must hold distinct "Power Weights". A 9x9 pyramid made of Iron should yield a lower overall beacon power multiplier than a 9x9 Diamond pyramid.

### 2.2 Master-Slave Network Architecture
*   **Block Entity Logic:** Beacons must have a state to identify as either `MASTER` or `SLAVE`.
*   **Network Formation:** When a Slave beacon is placed, it scans its radius for a Master beacon. If found, it links to it (saving the Master's `BlockPos` in its NBT data).
*   **Bypass Rule:** SLAVE beacons bypass the vanilla "must have a 3x3 mineral base" rule. They only require an active connection to a MASTER.
*   **Validation & Tick Optimization:** To prevent server tick lag, Master-Slave connection validation and pyramid structure checks should run every 40 to 80 ticks, NOT every single tick.
*   **VFX & Rendering:** Utilize a custom `BlockEntityRenderer` for Slave beacons to draw a continuous beam/particle stream directed exactly at the Master beacon's center coordinates.

### 2.3 Expanded Effects & AoE
*   **Multi-Effect:** Allow selection of 2+ primary effects simultaneously based on total Master Power.
*   **Secondary Effect:** Make the hardcoded Vanilla "Regeneration" optional/removable via the UI.
*   **Area of Effect (AoE):** Radius must scale algorithmically alongside the custom 'n' tiers.

## 3. Dynamic Payment System & State Machine
*   **Inflation Logic:** High-tier custom effects (e.g., Haste X) trigger an exponential cost curve to prevent OP mechanics.
*   **Material Evolution Flow:** Iron Ingot -> Gold Ingot -> Emerald -> Diamond -> Netherite.
*   **Stack Capping & Shift:** 
    *   If the calculated cost exceeds a stack (64x) of a material, it shifts to the **Block Variant** (e.g., 65 Iron Ingots becomes 1 Iron Block + 1 Iron Ingot).
    *   If the Block variant cost exceeds 64x, the system shifts to requiring **Nether Stars**.
*   **Anti-OP Hard Caps:** Effects that drastically alter combat balance (Strength, Resistance) must have independent, much steeper cost multipliers defined in the logic.

## 4. GUI & Inventory Overhaul (`BeaconScreen` & `ScreenHandler`)
### 4.1 UI Components
*   **Scrollable Effect Menu:** Inject a custom vertical scrollbar widget into the effect selection grid to support numerous custom effects and extended levels (V, X, etc.).
*   **Blueprint / Helper Panel:** Render a dynamic text panel on the left side of the UI. It must calculate and display:
    *   *Required Tier Level* for the selected setup.
    *   *Total Block Cost* (formatted in Stacks + Remainder) needed to build the base.
*   **Vanilla Visuals:** If the tier is >= 9, force the UI to display the max-tier vanilla pyramid icon.

### 4.2 Custom Inventory Slots
*   The vanilla single-item tribute slot must be overhauled. Implement a dynamic inventory handler within the beacon that can accept multiple stacks of payment blocks or Nether Stars to satisfy the inflated costs.

## 5. Network Synchronization (Packets)
*   **C2S (Client-to-Server):** When a player selects custom effects and pays via the new dynamic slots, custom C2S payload packets must securely transmit the new arrays of effects and consumed items.
*   **S2C (Server-to-Client):** The server must broadcast the Master beacon's updated tier, power multiplier, and active Slave connections to all nearby clients for accurate rendering of the VFX beams.

## 6. Data-Driven Configuration
*   Implement a `config.json` system loaded at server start.
*   **Admin Commands:** Create a `/custombeacon` command node (similar to gamerules) to adjust max tiers, hard caps, and power weights dynamically. Changes via command must serialize and save to the JSON config.
