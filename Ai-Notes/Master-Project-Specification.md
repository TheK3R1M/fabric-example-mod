# Custom Beacon Overhaul Mod - Master Technical Specification & Architecture Roadmap

## 1. Executive Summary & Core Objective
This document synthesizes and unifies the architectural specifications for the **Custom Beacon Overhaul Mod**, a data-driven **Fabric Mod** designed to revamp Minecraft's vanilla beacon mechanics.

The objective of this mod is to extend the beacon system beyond vanilla limitations by introducing:
1. **Algorithmic Pyramid Tier Scaling** (`Base Width = 2n + 1`) without hardcoded tier caps.
2. **Master-Slave (Support) Multi-Block Beacon Networks** with custom beam rendering and server-tick optimized validation.
3. **Dynamic Payment Inflation & Material Evolution System** (Ingot -> Block -> Nether Star scaling with anti-OP hard caps).
4. **Extended GUI (`BeaconScreen`) & Dynamic Inventory (`BeaconScreenHandler`)** featuring a scrollable effect selector, dynamic blueprint helper panel, and multi-stack tribute slots.
5. **Data-Driven Server Configuration (`config.json`) & Admin Commands (`/custombeacon`)**.
6. **Seamless Vanilla Fallback**: If no custom parameters are set or network conditions break, beacons seamlessly revert to vanilla behavior.

---

## 2. Core Mechanics & Architecture

### 2.1 Algorithmic Tier & Mineral Power Weight Scaling
- **Pyramid Base Formula:** `Base Width = 2n + 1`, where `n` is the pyramid tier level ($n \ge 1$).
- **Mineral Power Weight System:** Each mineral block type contributes a specific power weight value to the beacon system:
  - *Example Power Weights:* Iron = 1.0x, Gold = 1.2x, Emerald = 1.5x, Diamond = 2.0x, Netherite = 3.0x.
- **Dynamic Area of Effect (AoE):** Beacon effect radius scales algorithmically based on both the tier $n$ and the total pyramid mineral power weight.

### 2.2 Master-Slave (Support) Network Architecture
- **State Machine:** Every beacon entity maintains state as `MASTER` or `SLAVE`.
- **Slave Linking Logic:**
  - Placed Slave beacons scan their active radius for a Master beacon and store the Master's `BlockPos` in NBT.
  - **Base Bypass Rule:** Slave beacons **do not** require a standard 3x3 mineral pyramid base; they draw operational power directly from their linked Master beacon.
  - **Fallback:** If the Master is destroyed or unpowered, Slaves immediately revert to an unpowered/inactive state.
- **Server Performance & Tick Optimization:**
  - Master-Slave link validation and pyramid structural checks execute periodically (every **40 to 80 ticks**), avoiding heavy per-tick checks on the main server loop.
- **Visual Effects (VFX) & Rendering:**
  - Custom `BlockEntityRenderer` for Slave beacons renders a continuous directional particle/beam stream targeted directly at the linked Master beacon's center coordinates.

### 2.3 Extended Effect Mechanics
- **Multi-Effect Selection:** Players can activate 2 or more primary effects simultaneously, depending on the network's total power tier.
- **Optional Secondary Effect:** The vanilla mandatory "Regeneration" secondary effect is decoupled and can be toggled on/off or replaced via the custom UI.

---

## 3. Dynamic Payment System & Material Evolution

### 3.1 Cost Inflation & Material Evolution Chain
To balance high-tier effects (e.g., Haste X, Resistance V), payment requirements increase dynamically according to an exponential cost curve:
$$\text{Cost} = f(\text{Effect Level}, \text{Effect Type Weight})$$

#### Progression Chain & Stack Capping Rules:
1. **Base Ingot Tier:** Iron Ingot $\rightarrow$ Gold Ingot $\rightarrow$ Emerald $\rightarrow$ Diamond $\rightarrow$ Netherite Ingot.
2. **Stack Cap Shift (Item $\rightarrow$ Block):** When calculated item cost exceeds **64x** (1 stack), the payment requirement automatically evolves into the **Block Variant** (e.g., 65 Iron Ingots evolves into 1 Iron Block + 1 Iron Ingot).
3. **Block Cap Shift (Block $\rightarrow$ Nether Star):** When Block variant requirements exceed **64x** (1 stack of blocks), the payment requirement shifts to **Nether Stars**.

### 3.2 Anti-OP Hard Caps & Combat Balancing
- Game-changing combat effects (e.g., **Strength**, **Resistance**) possess steeper payment cost curves and strict hard caps defined in the config.
- Utility effects (e.g., **Haste**, **Speed**, **Night Vision**) follow standard payment scaling curves.

### 3.3 Optional Upkeep & Active Drain Protocol (Config Toggle)
- **Design Philosophy:** To ensure flexibility for future balancing, the mod must include an optional "Active Drain" system, which can be toggled on/off via `config.json` (e.g., `"enable_active_drain": false` by default).
- **Tick-Based Consumption (If Enabled):** 
  - **Material Drain:** The beacon's dynamic inventory acts as a fuel engine, slowly consuming stored tribute items over time (e.g., 1 Item/Block every X minutes) to maintain OP custom effects.
  - **Player Drain (Alternative):** A mechanic that slowly drains the XP or Hunger bar of players who are actively receiving high-tier buffs.
- **Power Outage Fallback:** If the drain is enabled and the beacon runs out of fuel (or players deplete their XP/Hunger), the custom effects immediately downgrade to the maximum vanilla fallback state until the beacon is refueled.

---

## 4. GUI & Inventory Overhaul (`BeaconScreen` & `ScreenHandler`)

### 4.1 UI Layout & Custom Widgets
- **Scrollable Effect Menu:** Replaces the static vanilla effect layout with a custom scrollable grid component to display higher effect levels (Level V, X) and added modded/custom effects.
- **Blueprint / Helper Panel:** Renders on the left side of the beacon GUI:
  - Displays the required **Pyramid Tier ($n$)**.
  - Displays exact **Total Mineral Block Costs** (formatted clearly in Stacks + Remainder).
- **High-Tier Visual Indicator:** For any beacon pyramid at Tier 9 or above, the GUI displays the max-tier vanilla pyramid icon as a clean visual indicator.

### 4.2 Dynamic Tribute Inventory Handling
- Replaces the single vanilla tribute slot in `BeaconScreenHandler` with a dynamic multi-slot inventory system capable of holding and consuming multiple stacks of mineral blocks or Nether Stars.

---

## 5. Network Protocol & Synchronization (C2S / S2C)

- **Client-to-Server (C2S):** Transmits custom payload packets when players confirm effect selections and submit multi-stack tribute payments.
- **Server-to-Client (S2C):** Broadcasts Master beacon power stats, active Slave links, and rendering targets to all nearby clients for client-side VFX beam rendering.

---

## 6. Data-Driven Configuration & Admin Commands

- **Config File (`config.json`):**
  - Stores mineral power weights, max tier limits, effect hard caps, payment inflation curves, and network scan radii.
- **In-Game Command System (`/custombeacon`):**
  - Admin command structure (similar to gamerules) allowing admins to query or modify settings in real-time with automatic JSON serialization and server sync.

---

## 7. Implementation Roadmap & Technical Phases

1. **Phase 1: Architecture & Data Structures**
   - Config manager (`config.json`), registration of `/custombeacon` command, data models for mineral weights & effect caps.
2. **Phase 2: Master-Slave Entity & Algorithmic Pyramid Logic**
   - Mixins and extensions for `BeaconBlockEntity`, 40-80 tick validation loop, Master-Slave NBT state, beam rendering (`BlockEntityRenderer`).
3. **Phase 3: Extended Inventory & Screen Handler**
   - `BeaconScreenHandler` dynamic tribute slots, multi-stack item consumption, C2S payment packets.
4. **Phase 4: GUI & Blueprint Panel**
   - `BeaconScreen` scrollable effect grid widget, blueprint helper panel layout, S2C synchronization.
5. **Phase 5: Dynamic Payment & Inflation State Machine**
   - Calculation engine for item/block/Nether Star conversions and anti-OP balancing curves.
6. **Phase 6: Verification & Polish**
   - Fallback testing, server performance profiling, stress testing network rendering.
