package com.example.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ScrollableEffectWidget extends AbstractWidget {

    public record EffectOption(String effectId, String displayName, int level) {}

    private final List<EffectOption> options = new ArrayList<>();
    private final BiConsumer<String, Integer> onSelect;
    private double scrollAmount = 0.0;
    private int selectedIndex = -1;

    public ScrollableEffectWidget(int x, int y, int width, int height, BiConsumer<String, Integer> onSelect) {
        super(x, y, width, height, Component.literal("Scrollable Effects"));
        this.onSelect = onSelect;
        populateEffects();
    }

    private void populateEffects() {
        String[] defaultEffects = {
            "minecraft:speed", "minecraft:haste", "minecraft:resistance", 
            "minecraft:jump_boost", "minecraft:strength", "minecraft:regeneration", "minecraft:night_vision"
        };

        for (String eff : defaultEffects) {
            String name = eff.substring(eff.indexOf(":") + 1).replace("_", " ").toUpperCase();
            for (int lvl = 1; lvl <= 10; lvl++) {
                options.add(new EffectOption(eff, name + " " + lvl, lvl));
            }
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        // Draw background box
        extractor.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xAA000000);

        int itemHeight = 18;
        int visibleCount = this.height / itemHeight;
        int startIndex = Math.max(0, (int) (scrollAmount / itemHeight));
        int endIndex = Math.min(options.size(), startIndex + visibleCount + 1);

        for (int i = startIndex; i < endIndex; i++) {
            EffectOption opt = options.get(i);
            int itemY = this.getY() + (i * itemHeight) - (int) scrollAmount;

            if (itemY < this.getY() || itemY + itemHeight > this.getY() + this.height) continue;

            int color = (i == selectedIndex) ? 0xFFFFFF00 : 0xFFFFFFFF;
            if (mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= itemY && mouseY < itemY + itemHeight) {
                color = 0xFF00FF00;
            }

            extractor.text(Minecraft.getInstance().font, opt.displayName(), this.getX() + 4, itemY + 4, color);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            scrollAmount = Math.max(0, Math.min((options.size() * 18) - this.height, scrollAmount - (verticalAmount * 12)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (isMouseOver(mouseX, mouseY)) {
            int itemHeight = 18;
            int clickedIdx = (int) ((mouseY - this.getY() + scrollAmount) / itemHeight);
            if (clickedIdx >= 0 && clickedIdx < options.size()) {
                this.selectedIndex = clickedIdx;
                EffectOption opt = options.get(clickedIdx);
                if (onSelect != null) {
                    onSelect.accept(opt.effectId(), opt.level());
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
