package ninja.trek.srd.client.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.client.ClientPossessionManager;
import ninja.trek.srd.combat.CombatState;
import ninja.trek.srd.network.payloads.SelectCharacterPayload;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HUD overlay that displays the player's character list on the left side of the screen.
 * Always visible and shows character HP, AC, and active state.
 */
public class CharacterListHud implements HudRenderCallback {
    private static final CharacterListHud INSTANCE = new CharacterListHud();

    private static final int PANEL_PADDING = 6;
    private static final int PANEL_MARGIN_LEFT = 8;
    private static final int PANEL_MARGIN_TOP = 50;
    private static final int CHARACTER_ENTRY_HEIGHT = 40;
    private static final int CHARACTER_ENTRY_SPACING = 4;
    private static final int CHARACTER_ENTRY_WIDTH = 180;
    private static final int HP_BAR_HEIGHT = 4;
    private static final int PORTRAIT_SIZE = 32;
    private static final int TEXT_OFFSET_X = PORTRAIT_SIZE + 8;

    // Colors
    private static final int COLOR_PANEL_BG = 0xCC000000; // Semi-transparent black
    private static final int COLOR_ENTRY_BG = 0x88444444; // Semi-transparent gray
    private static final int COLOR_ENTRY_BG_HOVER = 0xAA666666; // Lighter gray on hover
    private static final int COLOR_ENTRY_BG_ACTIVE = 0xCC336633; // Green tint for active
    private static final int COLOR_BORDER = 0xFF888888; // Light gray border
    private static final int COLOR_BORDER_ACTIVE = 0xFF00FF00; // Green border for active
    private static final int COLOR_HP_FULL = 0xFF00FF00; // Green
    private static final int COLOR_HP_MEDIUM = 0xFFFFFF00; // Yellow
    private static final int COLOR_HP_LOW = 0xFFFF0000; // Red
    private static final int COLOR_HP_BG = 0xFF333333; // Dark gray background
    private static final int COLOR_TEXT = 0xFFFFFFFF; // White
    private static final int COLOR_TEXT_SECONDARY = 0xFFAAAAAA; // Light gray

    private final List<CharacterEntry> characterEntries = new ArrayList<>();
    private int mouseX = 0;
    private int mouseY = 0;

    private CharacterListHud() {}

    /**
     * Register the HUD overlay.
     */
    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);

        // Track mouse position
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client != null && client.mouse != null) {
                INSTANCE.mouseX = (int) client.mouse.getX();
                INSTANCE.mouseY = (int) client.mouse.getY();
            }
        });

        // Handle mouse clicks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) {
                return;
            }

            // Left click to select character
            if (client.mouse.wasLeftButtonClicked()) {
                INSTANCE.handleClick(client, false);
            }

            // Right click to open character stats (TODO: implement stats screen)
            if (client.mouse.wasRightButtonClicked()) {
                INSTANCE.handleClick(client, true);
            }
        });
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) {
            return;
        }

        // Only show in party mode
        ClientPossessionManager possessionManager = ClientPossessionManager.getInstance();
        if (possessionManager.isPlayerMode()) {
            return;
        }

        // Collect player's characters
        updateCharacterList(client);

        if (characterEntries.isEmpty()) {
            return;
        }

        // Render the panel
        renderPanel(context, client);
    }

    /**
     * Update the list of characters from the world.
     */
    private void updateCharacterList(MinecraftClient client) {
        characterEntries.clear();

        if (client.world == null || client.player == null) {
            return;
        }

        UUID playerUUID = client.player.getUuid();

        // Find all character entities owned by this player
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof CharacterEntity character) {
                UUID ownerUUID = character.getOwnerUUID();
                if (ownerUUID != null && ownerUUID.equals(playerUUID)) {
                    characterEntries.add(new CharacterEntry(character));
                }
            }
        }
    }

    /**
     * Render the character list panel.
     */
    private void renderPanel(DrawContext context, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        UUID possessedCharacterUUID = ClientPossessionManager.getInstance().getPossessedCharacterUUID();

        int panelHeight = (PANEL_PADDING * 2) +
            (characterEntries.size() * CHARACTER_ENTRY_HEIGHT) +
            ((characterEntries.size() - 1) * CHARACTER_ENTRY_SPACING);

        int panelX = PANEL_MARGIN_LEFT;
        int panelY = PANEL_MARGIN_TOP;
        int panelWidth = CHARACTER_ENTRY_WIDTH + (PANEL_PADDING * 2);

        // Draw panel background
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, COLOR_PANEL_BG);

        // Draw panel border
        drawBorder(context, panelX, panelY, panelWidth, panelHeight, COLOR_BORDER);

        // Draw character entries
        int entryY = panelY + PANEL_PADDING;
        for (CharacterEntry entry : characterEntries) {
            renderCharacterEntry(context, textRenderer, entry, panelX + PANEL_PADDING, entryY, possessedCharacterUUID);
            entryY += CHARACTER_ENTRY_HEIGHT + CHARACTER_ENTRY_SPACING;
        }
    }

    /**
     * Render a single character entry.
     */
    private void renderCharacterEntry(DrawContext context, TextRenderer textRenderer, CharacterEntry entry,
                                     int x, int y, UUID possessedCharacterUUID) {
        boolean isActive = entry.character.getUuid().equals(possessedCharacterUUID);
        boolean isHovered = isMouseOver(x, y, CHARACTER_ENTRY_WIDTH, CHARACTER_ENTRY_HEIGHT);

        // Background color
        int bgColor = isActive ? COLOR_ENTRY_BG_ACTIVE : (isHovered ? COLOR_ENTRY_BG_HOVER : COLOR_ENTRY_BG);
        context.fill(x, y, x + CHARACTER_ENTRY_WIDTH, y + CHARACTER_ENTRY_HEIGHT, bgColor);

        // Border
        int borderColor = isActive ? COLOR_BORDER_ACTIVE : COLOR_BORDER;
        drawBorder(context, x, y, CHARACTER_ENTRY_WIDTH, CHARACTER_ENTRY_HEIGHT, borderColor);

        // Portrait placeholder (simple colored square for now)
        int portraitX = x + 4;
        int portraitY = y + 4;
        context.fill(portraitX, portraitY, portraitX + PORTRAIT_SIZE, portraitY + PORTRAIT_SIZE, 0xFF555555);
        drawBorder(context, portraitX, portraitY, PORTRAIT_SIZE, PORTRAIT_SIZE, COLOR_BORDER);

        // Character name
        String name = entry.character.getName().getString();
        int textX = x + TEXT_OFFSET_X;
        int textY = y + 4;
        context.drawText(textRenderer, name, textX, textY, COLOR_TEXT, true);

        // AC and Level
        CombatState combatState = entry.character.getCombatState();
        String stats = String.format("AC %d | Lvl %d", combatState.armorClass(), entry.character.getLevel());
        context.drawText(textRenderer, stats, textX, textY + 10, COLOR_TEXT_SECONDARY, true);

        // HP bar
        int hpBarX = textX;
        int hpBarY = textY + 20;
        int hpBarWidth = CHARACTER_ENTRY_WIDTH - TEXT_OFFSET_X - 8;

        // HP background
        context.fill(hpBarX, hpBarY, hpBarX + hpBarWidth, hpBarY + HP_BAR_HEIGHT, COLOR_HP_BG);

        // HP foreground
        float hpPercent = (float) combatState.currentHitPoints() / combatState.maxHitPoints();
        int hpFillWidth = (int) (hpBarWidth * hpPercent);
        int hpColor = getHPColor(hpPercent);
        context.fill(hpBarX, hpBarY, hpBarX + hpFillWidth, hpBarY + HP_BAR_HEIGHT, hpColor);

        // HP text
        String hpText = String.format("%d/%d", combatState.currentHitPoints(), combatState.maxHitPoints());
        int hpTextWidth = textRenderer.getWidth(hpText);
        context.drawText(textRenderer, hpText, hpBarX + hpBarWidth + 4, hpBarY - 2, COLOR_TEXT_SECONDARY, true);

        // Store bounds for click detection
        entry.bounds = new Rect(x, y, CHARACTER_ENTRY_WIDTH, CHARACTER_ENTRY_HEIGHT);
    }

    /**
     * Get HP bar color based on percentage.
     */
    private int getHPColor(float percent) {
        if (percent > 0.5f) {
            return COLOR_HP_FULL;
        } else if (percent > 0.25f) {
            return COLOR_HP_MEDIUM;
        } else {
            return COLOR_HP_LOW;
        }
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
     * Check if mouse is over a rectangle.
     */
    private boolean isMouseOver(int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /**
     * Handle mouse click on character list.
     */
    private void handleClick(MinecraftClient client, boolean isRightClick) {
        for (CharacterEntry entry : characterEntries) {
            if (entry.bounds != null && isMouseOver(entry.bounds.x, entry.bounds.y, entry.bounds.width, entry.bounds.height)) {
                if (isRightClick) {
                    // TODO: Open character stats screen
                    openCharacterStats(client, entry.character);
                } else {
                    // Left click - select character
                    selectCharacter(client, entry.character);
                }
                break;
            }
        }
    }

    /**
     * Select a character (send packet to server).
     */
    private void selectCharacter(MinecraftClient client, CharacterEntity character) {
        ClientPlayNetworking.send(new SelectCharacterPayload(character.getUuid()));
    }

    /**
     * Open character stats screen (right-click).
     */
    private void openCharacterStats(MinecraftClient client, CharacterEntity character) {
        // TODO: Implement CharacterStatsScreen
        client.setScreen(new CharacterStatsScreen(character));
    }

    /**
     * Represents a character entry in the list.
     */
    private static class CharacterEntry {
        final CharacterEntity character;
        Rect bounds;

        CharacterEntry(CharacterEntity character) {
            this.character = character;
        }
    }

    /**
     * Simple rectangle for bounds checking.
     */
    private record Rect(int x, int y, int width, int height) {}
}
