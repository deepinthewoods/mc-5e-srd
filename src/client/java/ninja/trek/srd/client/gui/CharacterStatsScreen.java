package ninja.trek.srd.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import ninja.trek.srd.character.data.CharacterStats;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.network.payloads.DeleteCharacterPayload;

/**
 * Screen displaying full character stats with a delete button.
 */
public class CharacterStatsScreen extends Screen {
    private static final int PANEL_WIDTH = 400;
    private static final int PANEL_PADDING = 20;
    private static final int LINE_HEIGHT = 12;
    private static final int SECTION_SPACING = 16;

    private static final int COLOR_BG = 0xCC000000;
    private static final int COLOR_BORDER = 0xFF888888;
    private static final int COLOR_TITLE = 0xFFFFFFFF;
    private static final int COLOR_TEXT = 0xFFCCCCCC;
    private static final int COLOR_LABEL = 0xFFAAAAAA;

    private final CharacterEntity character;
    private ButtonWidget deleteButton;
    private ButtonWidget closeButton;

    public CharacterStatsScreen(CharacterEntity character) {
        super(Text.literal("Character Stats"));
        this.character = character;
    }

    @Override
    protected void init() {
        super.init();

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = 40;
        int panelHeight = this.height - 80;

        // Close button
        int buttonY = panelY + panelHeight + 10;
        closeButton = ButtonWidget.builder(Text.literal("Close"), button -> {
            this.close();
        })
        .dimensions(panelX + 10, buttonY, 100, 20)
        .build();
        this.addDrawableChild(closeButton);

        // Delete button (right side)
        deleteButton = ButtonWidget.builder(Text.literal("Delete Character"), button -> {
            this.deleteCharacter();
        })
        .dimensions(panelX + PANEL_WIDTH - 130, buttonY, 120, 20)
        .build();
        this.addDrawableChild(deleteButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render semi-transparent background
        renderBackground(context, mouseX, mouseY, delta);

        int panelX = (this.width - PANEL_WIDTH) / 2;
        int panelY = 40;
        int panelHeight = this.height - 80;

        // Draw panel background
        context.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_BG);

        // Draw border
        drawBorder(context, panelX, panelY, PANEL_WIDTH, panelHeight, COLOR_BORDER);

        // Render character stats
        renderCharacterStats(context, panelX + PANEL_PADDING, panelY + PANEL_PADDING);

        // Render buttons
        super.render(context, mouseX, mouseY, delta);
    }

    /**
     * Render all character statistics.
     */
    private void renderCharacterStats(DrawContext context, int x, int y) {
        int currentY = y;

        // Character name (title)
        String name = character.getName().getString();
        context.drawText(this.textRenderer, name, x, currentY, COLOR_TITLE, true);
        currentY += LINE_HEIGHT + 4;

        // Race and Class
        String raceClass = character.getRace().getName() + " " + character.getCharacterClass().getName();
        context.drawText(this.textRenderer, raceClass, x, currentY, COLOR_TEXT, false);
        currentY += LINE_HEIGHT;

        // Level
        context.drawText(this.textRenderer, "Level: " + character.getLevel(), x, currentY, COLOR_TEXT, false);
        currentY += LINE_HEIGHT + SECTION_SPACING;

        // Combat Stats Section
        context.drawText(this.textRenderer, "=== Combat ===", x, currentY, COLOR_TITLE, false);
        currentY += LINE_HEIGHT + 4;

        CombatState combatState = character.getCombatState();
        context.drawText(this.textRenderer, "Hit Points: " + combatState.currentHitPoints() + "/" + combatState.maxHitPoints(), x, currentY, COLOR_TEXT, false);
        currentY += LINE_HEIGHT;

        context.drawText(this.textRenderer, "Armor Class: " + combatState.armorClass(), x, currentY, COLOR_TEXT, false);
        currentY += LINE_HEIGHT + SECTION_SPACING;

        // Ability Scores Section
        context.drawText(this.textRenderer, "=== Ability Scores ===", x, currentY, COLOR_TITLE, false);
        currentY += LINE_HEIGHT + 4;

        CharacterStats stats = character.getStats();
        currentY = renderAbilityScore(context, x, currentY, "Strength", stats.strength(), stats.getStrengthModifier());
        currentY = renderAbilityScore(context, x, currentY, "Dexterity", stats.dexterity(), stats.getDexterityModifier());
        currentY = renderAbilityScore(context, x, currentY, "Constitution", stats.constitution(), stats.getConstitutionModifier());
        currentY = renderAbilityScore(context, x, currentY, "Intelligence", stats.intelligence(), stats.getIntelligenceModifier());
        currentY = renderAbilityScore(context, x, currentY, "Wisdom", stats.wisdom(), stats.getWisdomModifier());
        currentY = renderAbilityScore(context, x, currentY, "Charisma", stats.charisma(), stats.getCharismaModifier());

        currentY += SECTION_SPACING;

        // Equipment Section
        context.drawText(this.textRenderer, "=== Equipment ===", x, currentY, COLOR_TITLE, false);
        currentY += LINE_HEIGHT + 4;

        String weapon = character.getEquippedWeapon().name();
        context.drawText(this.textRenderer, "Weapon: " + weapon, x, currentY, COLOR_TEXT, false);
        currentY += LINE_HEIGHT;

        // Movement Speed
        currentY += SECTION_SPACING;
        context.drawText(this.textRenderer, "=== Movement ===", x, currentY, COLOR_TITLE, false);
        currentY += LINE_HEIGHT + 4;
        context.drawText(this.textRenderer, "Speed: " + character.getRace().getBaseMovementSpeed() + " blocks", x, currentY, COLOR_TEXT, false);
    }

    /**
     * Render a single ability score line.
     */
    private int renderAbilityScore(DrawContext context, int x, int y, String name, int score, int modifier) {
        String modifierStr = (modifier >= 0 ? "+" : "") + modifier;
        String line = String.format("%s: %d (%s)", name, score, modifierStr);
        context.drawText(this.textRenderer, line, x, y, COLOR_TEXT, false);
        return y + LINE_HEIGHT;
    }

    /**
     * Draw a border around a rectangle.
     */
    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Top
        context.fill(x, y, x + width, y + 1, color);
        // Bottom
        context.fill(x, y + height - 1, x + width, y + height, color);
        // Left
        context.fill(x, y, x + 1, y + height, color);
        // Right
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * Delete the character (send packet to server).
     */
    private void deleteCharacter() {
        // TODO: Add confirmation dialog
        ClientPlayNetworking.send(new DeleteCharacterPayload(character.getUuid()));
        this.close();
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }
}
