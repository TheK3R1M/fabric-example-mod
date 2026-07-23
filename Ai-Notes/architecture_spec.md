# PROJECT ARCHITECTURE SPECIFICATION: Custom Beacon Overhaul (Fabric)

## 1. Project Objective
We are completely overhauling the vanilla Minecraft Beacon system. The vanilla UI is entirely removed and replaced with a highly modular, interactive, and data-driven custom GUI. 

The codebase was previously corrupted by an AI that hallucinated Forge/Mojang mappings and wrote destructive Mixins. **You are here to act as a Senior Fabric Mod Developer to audit, clean, and stabilize this mod.**

---

## 2. STRICT TECHNICAL CONSTRAINTS (DO NOT VIOLATE)

*   **MAPPINGS:** You must strictly use Fabric/Yarn mappings. The previous AI hallucinated Mojang mappings (e.g., it used `net.minecraft.resources.ResourceLocation`). **You must use `net.minecraft.util.Identifier`.** Do not use `fromNamespaceAndPath`, use `Identifier.of()` or `new Identifier()`.
*   **MIXIN SAFETY:** **ABSOLUTELY NO `@Overwrite` ANNOTATIONS ALLOWED.** The previous AI caused critical JVM startup crashes by using `@Overwrite` on `BeaconBlockEntity.setRemoved()`. You must use safe injection methods: `@Inject`, `@ModifyVariable`, `@Redirect`, or `@ModifyArg`.
*   **JSON MIXIN CONFIG:** Ensure `modid.mixins.json` has correct, non-duplicated package paths. (e.g., Do not duplicate the root package like `com.example.mixin.client...` if the root is already `com.example.mixin`).

---

## 3. CORE SYSTEMS & FEATURES

### 3.1. Dynamic UI System (Hot-Reloadable Layout)
To avoid restarting the client for UI pixel adjustments, we use a dynamic layout system.
*   **File:** `config/ui_layout.json`
*   **Manager:** `UILayoutConfig.java`
*   **Mechanism:** Every time the Beacon screen opens (in `init()`), `BeaconScreenMixin` calls `UILayoutConfig.loadOrGenerate()` to read `x, y, width, height` for all custom widgets. 
*   **Coordinate Math:** The `x` and `y` values in the JSON are **relative offsets** from the center of the screen (e.g., `left + layout.blueprintPanel.x`). DO NOT hardcode absolute screen coordinates.

### 3.2. Custom UI Elements
1.  **Vanilla Button Suppression:** `BeaconScreenMixin` must strictly cancel or clear vanilla `BeaconPowerButton` elements.
2.  **`ScrollableEffectWidget`:** A 2-column scrollable grid containing REAL, clickable `ButtonWidget` instances. Do not just draw text on the screen. Buttons must have proper hover states and use Scissor rendering (`DrawContext.enableScissor()`) so they don't bleed outside the panel. Custom item icons (Sugar for Speed, Golden Pickaxe for Haste, etc.) are rendered inside these buttons.
3.  **`BlueprintPanelWidget`:** A panel on the left that listens to the `ScrollableEffectWidget`. When an effect is clicked, this panel dynamically recalculates and displays the inflated cost (e.g., "1 Stack + 8 Blocks").
4.  **Confirm Button:** A checkmark button that reads the current selected primary/secondary states and fires the network packet.

### 3.3. Gameplay Logic & Networking
*   **Payment Slot Override:** The Beacon's payment slot (`BeaconMenu$PaymentSlot`) needs its stack limit increased to 64. The previous AI crashed the game by injecting into `getMaxStackSize(ItemStack)`. **You must `@Inject` into the no-argument method: `getMaxStackSize()`** and set the return value to 64.
*   **Networking:** A custom `BeaconSyncPayload` (C2S payload) is sent upon pressing the confirm button, handled by a server-side receiver to update the `BeaconBlockEntity`.

---

## 4. IMMEDIATE ACTION ITEMS FOR REFACTORING
When you begin editing the code, prioritize the following:
1. Scan all `.java` files and replace any Mojang mapping remnants (`ResourceLocation`) with Yarn (`Identifier`).
2. Audit `BeaconBlockEntityMixin` and remove any `@Overwrite` annotations. Replace them with `@Inject(method = "setRemoved", at = @At("HEAD"))` or similar safe patterns.
3. Audit `BeaconPaymentSlotMixin` and ensure it targets `getMaxStackSize()` (no arguments).
4. Review the GUI classes to ensure `ButtonWidget` instances in the scrollable grid are properly instantiated and handle click events that sync with `BlueprintPanelWidget`.
5. Ensure `ui_layout.json` hot-reloading logic remains intact in the Screen Mixin.
