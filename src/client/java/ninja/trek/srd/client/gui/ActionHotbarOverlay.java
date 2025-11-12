package ninja.trek.srd.client.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.client.ClientEncounterState;
import ninja.trek.srd.network.payloads.EndTurnPayload;
import ninja.trek.srd.network.payloads.SyncEncounterStatePayload;
import ninja.trek.srd.network.payloads.UseActionPayload;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * HUD overlay that renders a second action hotbar above the vanilla hotbar.
 * Displays the standard 5e actions and wires them to keybinds / mouse clicks.
 */
public final class ActionHotbarOverlay implements HudRenderCallback {
    private static final Identifier KEYBIND_CATEGORY_ID = Identifier.of(FiveESrdMod.MOD_ID, "actionhotbar");
    private static final Category KEYBIND_CATEGORY = Category.create(KEYBIND_CATEGORY_ID);
    private static final ActionHotbarOverlay INSTANCE = new ActionHotbarOverlay();

    private static final int BUTTON_WIDTH = 90;
    private static final int BUTTON_HEIGHT = 22;
    private static final int BUTTON_SPACING = 4;
    private static final int ROW_SPACING = 6;
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_BOTTOM_OFFSET = 56;
    private static final int RESOURCE_SECTION_HEIGHT = 24;
    private static final int TURN_PANEL_WIDTH = 180;
    private static final int TURN_PANEL_PADDING = 6;
    private static final int TURN_HEADER_HEIGHT = 16;
    private static final int TURN_ROW_HEIGHT = 14;
    private static final int MAX_TURN_ROWS = 6;
    private static final int TURN_PANEL_MARGIN = 8;
    private static final int END_TURN_BUTTON_WIDTH = 112;
    private static final int END_TURN_BUTTON_HEIGHT = 22;
    private static final int TURN_BANNER_DURATION_TICKS = 80;
    private static final int TURN_BANNER_FADE_TICKS = 12;
    private static final int TURN_BANNER_MARGIN_TOP = 18;
    private static final int TURN_BANNER_PADDING = 8;
    private static final Text CURSOR_HINT_TEXT = Text.literal("Hold Left Alt to interact with the combat UI");

    private static final List<List<ActionButtonDefinition>> BUTTON_ROWS = List.of(
        List.of(
            createActionButton("Attack", "Make a weapon attack.", UseActionPayload.ActionType.ATTACK, ResourceType.ACTION, GLFW.GLFW_KEY_Z),
            createActionButton("Dash", "Double your movement speed for this turn.", UseActionPayload.ActionType.DASH, ResourceType.ACTION, GLFW.GLFW_KEY_X),
            createActionButton("Disengage", "Move without provoking opportunity attacks.", UseActionPayload.ActionType.DISENGAGE, ResourceType.ACTION, GLFW.GLFW_KEY_C),
            createActionButton("Dodge", "Attacks against you have disadvantage.", UseActionPayload.ActionType.DODGE, ResourceType.ACTION, GLFW.GLFW_KEY_V),
            createActionButton("Help", "Grant an ally advantage on their next attack.", UseActionPayload.ActionType.HELP, ResourceType.ACTION, GLFW.GLFW_KEY_B),
            createActionButton("Hide", "Attempt to become hidden.", UseActionPayload.ActionType.HIDE, ResourceType.ACTION, GLFW.GLFW_KEY_N),
            createActionButton("Ready", "Prepare a trigger for an action.", UseActionPayload.ActionType.READY, ResourceType.ACTION, GLFW.GLFW_KEY_M),
            createActionButton("Search", "Look for clues or hidden objects.", UseActionPayload.ActionType.SEARCH, ResourceType.ACTION, GLFW.GLFW_KEY_COMMA),
            createActionButton("Use Object", "Interact with an object in the world.", UseActionPayload.ActionType.USE_OBJECT, ResourceType.ACTION, GLFW.GLFW_KEY_PERIOD)
        ),
        List.of(
            createActionButton("Off-Hand", "Two-weapon fighting bonus attack.", UseActionPayload.ActionType.OFFHAND_ATTACK, ResourceType.BONUS_ACTION, GLFW.GLFW_KEY_SEMICOLON),
            createActionButton("Opportunity", "Make a reaction attack on movement.", UseActionPayload.ActionType.OPPORTUNITY_ATTACK, ResourceType.REACTION, GLFW.GLFW_KEY_APOSTROPHE)
        )
    );

    private static final List<ActionButtonDefinition> ALL_BUTTONS;
    private static final Map<UseActionPayload.ActionType, ActionButtonDefinition> BUTTON_LOOKUP;
    private static final KeyBinding END_TURN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.fiveesrd.actionhotbar.end_turn",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        KEYBIND_CATEGORY
    ));

    static {
        List<ActionButtonDefinition> combined = new ArrayList<>();
        for (List<ActionButtonDefinition> row : BUTTON_ROWS) {
            combined.addAll(row);
        }
        ALL_BUTTONS = List.copyOf(combined);

        Map<UseActionPayload.ActionType, ActionButtonDefinition> map = new EnumMap<>(UseActionPayload.ActionType.class);
        for (ActionButtonDefinition definition : ALL_BUTTONS) {
            map.put(definition.actionType(), definition);
        }
        BUTTON_LOOKUP = Map.copyOf(map);
    }

    private final List<RenderedButton> renderedButtons = new ArrayList<>();
    private Rect endTurnButtonBounds;
    private boolean endTurnButtonEnabled;
    private Text endTurnStatusText = Text.empty();
    private boolean cursorUnlockedForHotbar;
    private UUID turnBannerEncounterId;
    private UUID turnBannerEntityId;
    private Text turnBannerPrimaryText = Text.empty();
    private Text turnBannerSecondaryText = Text.empty();
    private boolean turnBannerIsPlayer;
    private int turnBannerTicksRemaining;

    private ActionHotbarOverlay() {}

    /**
     * Notify the overlay that a new turn started so the banner can animate.
     */
    public static void notifyTurnStart(UUID encounterId, UUID entityId) {
        INSTANCE.updateTurnBanner(encounterId, entityId);
    }

    /**
     * Notify the overlay that a turn finished so the banner can fade out faster.
     */
    public static void notifyTurnEnd(UUID encounterId, UUID entityId) {
        INSTANCE.handleTurnEndNotification(encounterId, entityId);
    }

    /**
     * Register the overlay renderer and key/input handlers.
     */
    public static void register() {
        HudRenderCallback.EVENT.register(INSTANCE);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.player == null) {
                return;
            }

            boolean overlayActive = INSTANCE.shouldRender(client);
            boolean altHeld = overlayActive && InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_LEFT_ALT);
            INSTANCE.handleCursorState(client, overlayActive, altHeld);

            // Handle keybind activations
            if (!client.isPaused()) {
                for (ActionButtonDefinition definition : ALL_BUTTONS) {
                    while (definition.keyBinding().wasPressed()) {
                        INSTANCE.sendActionIfAllowed(client, definition.actionType());
                    }
                }
                if (overlayActive) {
                    while (END_TURN_KEY.wasPressed()) {
                        INSTANCE.sendEndTurnIfAllowed(client);
                    }
                }
            }

            // Handle mouse clicks when the hotbar cursor is unlocked
            if (INSTANCE.cursorUnlockedForHotbar && client.mouse.wasLeftButtonClicked()) {
                INSTANCE.handleMouseClick(client);
            }
        });
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        tickTurnBannerTimer();
        if (!shouldRender(client)) {
            clearInteractiveElements();
            return;
        }

        UUID playerId = client.player.getUuid();
        ClientEncounterState state = ClientEncounterState.getInstance();
        UUID encounterId = state.getEncounterForEntity(playerId);
        if (encounterId == null) {
            clearInteractiveElements();
            return;
        }

        ClientEncounterState.EncounterData encounterData = state.getEncounter(encounterId);
        if (encounterData == null || encounterData.ended()) {
            clearInteractiveElements();
            return;
        }

        ClientEncounterState.CombatStateData combatState = state.getCombatState(playerId);
        boolean isPlayersTurn = playerId.equals(encounterData.getCurrentTurnEntity());

        double scale = client.getWindow().getScaleFactor();
        double mouseX = client.mouse.getX() / scale;
        double mouseY = client.mouse.getY() / scale;

        PanelBounds hotbarBounds = renderHotbar(drawContext, client, encounterData, combatState, isPlayersTurn, mouseX, mouseY);
        renderTurnTracker(drawContext, client, encounterData, playerId);
        if (turnBannerEncounterId == null || !turnBannerEncounterId.equals(encounterId)) {
            resetTurnBanner();
        }
        renderTurnBanner(drawContext, client);
        renderEndTurnButton(drawContext, client, hotbarBounds, combatState, isPlayersTurn, mouseX, mouseY);
        renderCursorHint(drawContext, client, hotbarBounds);
    }

    private boolean shouldRender(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return false;
        }
        if (client.options.hudHidden || client.currentScreen != null || client.player.isSpectator()) {
            return false;
        }
        ClientEncounterState state = ClientEncounterState.getInstance();
        UUID encounterId = state.getEncounterForEntity(client.player.getUuid());
        if (encounterId == null) {
            return false;
        }
        ClientEncounterState.EncounterData encounter = state.getEncounter(encounterId);
        return encounter != null && !encounter.ended();
    }

    private PanelBounds renderHotbar(
        DrawContext drawContext,
        MinecraftClient client,
        ClientEncounterState.EncounterData encounter,
        ClientEncounterState.CombatStateData combatState,
        boolean isPlayersTurn,
        double mouseX,
        double mouseY
    ) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int buttonRowsHeight = BUTTON_ROWS.size() * BUTTON_HEIGHT + Math.max(0, BUTTON_ROWS.size() - 1) * ROW_SPACING;
        int contentHeight = RESOURCE_SECTION_HEIGHT + ROW_SPACING + buttonRowsHeight;
        int panelHeight = contentHeight + PANEL_PADDING * 2;
        int panelWidth = calculatePanelWidth();

        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = screenHeight - PANEL_BOTTOM_OFFSET - panelHeight;

        drawContext.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xA0000000);
        drawBorder(drawContext, panelX, panelY, panelWidth, panelHeight, 0x80FFFFFF);

        Text roundText = Text.literal("Round " + encounter.roundNumber());
        drawContext.drawTextWithShadow(
            client.textRenderer,
            roundText,
            panelX + 4,
            panelY - 10,
            0xFFE8DFC0
        );

        Text turnText = isPlayersTurn ? Text.literal("Your turn") : Text.literal("Waiting for turn");
        drawContext.drawTextWithShadow(
            client.textRenderer,
            turnText,
            panelX + panelWidth - client.textRenderer.getWidth(turnText) - 4,
            panelY - 10,
            isPlayersTurn ? 0xFFF2C066 : 0xFFAAAAAA
        );

        RenderedButton hovered = null;

        renderedButtons.clear();
        int currentY = panelY + PANEL_PADDING;

        renderResourceSummary(drawContext, client, combatState, panelX + PANEL_PADDING, currentY);
        currentY += RESOURCE_SECTION_HEIGHT + ROW_SPACING;

        for (List<ActionButtonDefinition> row : BUTTON_ROWS) {
            if (row.isEmpty()) {
                continue;
            }
            int rowWidth = row.size() * BUTTON_WIDTH + (row.size() - 1) * BUTTON_SPACING;
            int startX = panelX + (panelWidth - rowWidth) / 2;

            for (int i = 0; i < row.size(); i++) {
                ActionButtonDefinition definition = row.get(i);
                ButtonState state = determineButtonState(definition, isPlayersTurn, combatState);
                int x = startX + i * (BUTTON_WIDTH + BUTTON_SPACING);

                renderButton(drawContext, client, definition, state, x, currentY, mouseX, mouseY);

                RenderedButton rendered = new RenderedButton(
                    definition,
                    x,
                    currentY,
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT,
                    state.enabled(),
                    state.statusText()
                );

                if (rendered.contains(mouseX, mouseY)) {
                    hovered = rendered;
                }
                renderedButtons.add(rendered);
            }
            currentY += BUTTON_HEIGHT + ROW_SPACING;
        }

        if (hovered != null && cursorUnlockedForHotbar) {
            drawActionTooltip(drawContext, client, hovered, mouseX, mouseY);
        }

        return new PanelBounds(panelX, panelY, panelWidth, panelHeight);
    }

    private int calculatePanelWidth() {
        int width = 0;
        for (List<ActionButtonDefinition> row : BUTTON_ROWS) {
            if (row.isEmpty()) {
                continue;
            }
            int rowWidth = row.size() * BUTTON_WIDTH + (row.size() - 1) * BUTTON_SPACING;
            width = Math.max(width, rowWidth);
        }
        return width + PANEL_PADDING * 2;
    }

    private void renderResourceSummary(
        DrawContext drawContext,
        MinecraftClient client,
        ClientEncounterState.CombatStateData combatState,
        int x,
        int y
    ) {
        TextRenderer renderer = client.textRenderer;
        Text hpValue = combatState != null
            ? Text.literal(combatState.currentHitPoints() + " / " + combatState.maxHitPoints())
            : Text.literal("--");
        Text acValue = combatState != null
            ? Text.literal(String.valueOf(combatState.armorClass()))
            : Text.literal("--");
        Text moveValue = combatState != null
            ? Text.literal(String.valueOf(combatState.remainingMovement()))
            : Text.literal("--");

        int baseY = y + 2;
        int cursor = x;
        cursor = drawLabelValue(drawContext, renderer, cursor, baseY, Text.literal("HP: "), hpValue, 0xFFF2C066);
        cursor = drawLabelValue(drawContext, renderer, cursor, baseY, Text.literal("AC: "), acValue, 0xFFB8C4FF);
        drawLabelValue(drawContext, renderer, cursor, baseY, Text.literal("Move: "), moveValue, 0xFFE3F2FD);

        int resourceY = baseY + 12;
        Text readyText = Text.literal("Ready");
        Text spentText = Text.literal("Spent");

        cursor = x;
        cursor = drawLabelValue(drawContext, renderer, cursor, resourceY, Text.literal("Action: "),
            combatState == null ? Text.literal("--") : (combatState.hasAction() ? readyText : spentText),
            combatState == null ? 0xFFAAAAAA : (combatState.hasAction() ? 0xFF7BC37E : 0xFFB86E6E)
        );
        cursor = drawLabelValue(drawContext, renderer, cursor, resourceY, Text.literal("Bonus: "),
            combatState == null ? Text.literal("--") : (combatState.hasBonusAction() ? readyText : spentText),
            combatState == null ? 0xFFAAAAAA : (combatState.hasBonusAction() ? 0xFF7BC37E : 0xFFB86E6E)
        );
        drawLabelValue(drawContext, renderer, cursor, resourceY, Text.literal("Reaction: "),
            combatState == null ? Text.literal("--") : (combatState.hasReaction() ? readyText : spentText),
            combatState == null ? 0xFFAAAAAA : (combatState.hasReaction() ? 0xFF7BC37E : 0xFFB86E6E)
        );
    }

    private int drawLabelValue(
        DrawContext drawContext,
        TextRenderer renderer,
        int x,
        int y,
        Text label,
        Text value,
        int valueColor
    ) {
        drawContext.drawText(renderer, label, x, y, 0xFF9AA0A6, false);
        x += renderer.getWidth(label);
        drawContext.drawText(renderer, value, x, y, valueColor, false);
        return x + renderer.getWidth(value) + 10;
    }

    private void renderTurnTracker(
        DrawContext drawContext,
        MinecraftClient client,
        ClientEncounterState.EncounterData encounter,
        UUID playerId
    ) {
        List<SyncEncounterStatePayload.InitiativeEntry> turnOrder = encounter.turnOrder();
        if (turnOrder.isEmpty()) {
            return;
        }

        int totalEntries = turnOrder.size();
        int rows = Math.min(totalEntries, MAX_TURN_ROWS);
        boolean hasOverflow = totalEntries > MAX_TURN_ROWS;
        int panelWidth = TURN_PANEL_WIDTH;
        int panelHeight = TURN_PANEL_PADDING * 2 + TURN_HEADER_HEIGHT + rows * TURN_ROW_HEIGHT + (hasOverflow ? TURN_ROW_HEIGHT : 0);

        int x = TURN_PANEL_MARGIN;
        int y = TURN_PANEL_MARGIN;
        drawContext.fill(x, y, x + panelWidth, y + panelHeight, 0x90000000);
        drawBorder(drawContext, x, y, panelWidth, panelHeight, 0x60FFFFFF);

        TextRenderer renderer = client.textRenderer;
        Text roundText = Text.literal("Round " + encounter.roundNumber());
        drawContext.drawText(renderer, roundText, x + TURN_PANEL_PADDING, y + 4, 0xFFE8DFC0, false);

        int currentTurnDisplay = Math.min(encounter.currentTurnIndex() + 1, totalEntries);
        Text turnCounter = Text.literal("Turn " + currentTurnDisplay + "/" + totalEntries);
        int counterWidth = renderer.getWidth(turnCounter);
        drawContext.drawText(renderer, turnCounter, x + panelWidth - TURN_PANEL_PADDING - counterWidth, y + 4, 0xFFCCCCCC, false);
        drawContext.drawText(renderer, Text.literal("Turn Order"), x + TURN_PANEL_PADDING, y + 4 + 10, 0xFFB0B0B0, false);

        int startIndex = encounter.currentTurnIndex();
        for (int i = 0; i < rows; i++) {
            int orderIndex = (startIndex + i) % totalEntries;
            SyncEncounterStatePayload.InitiativeEntry entry = turnOrder.get(orderIndex);
            int rowTop = y + TURN_PANEL_PADDING + TURN_HEADER_HEIGHT + i * TURN_ROW_HEIGHT;
            int rowBottom = rowTop + TURN_ROW_HEIGHT - 2;
            int rowLeft = x + TURN_PANEL_PADDING;
            int rowRight = x + panelWidth - TURN_PANEL_PADDING;

            boolean isCurrent = (i == 0);
            boolean isPlayer = entry.entityId().equals(playerId);

            int rowColor = isCurrent ? 0x603E4C27 : 0x40101010;
            drawContext.fill(rowLeft, rowTop, rowRight, rowBottom, rowColor);

            int indicatorColor = isCurrent ? 0xFFF2C066 : (isPlayer ? 0xFF5BA8FF : 0xFF444444);
            drawContext.fill(rowLeft - 3, rowTop, rowLeft - 1, rowBottom, indicatorColor);

            Text name = entry.displayName() != null ? entry.displayName() : Text.literal(entry.entityId().toString().substring(0, 8));
            int nameColor = isPlayer ? 0xFFFAF3C0 : 0xFFECE7DA;
            drawContext.drawText(renderer, name, rowLeft + 4, rowTop + 1, nameColor, false);

            int initiativeTotal = entry.initiativeRoll() + entry.dexModifier();
            Text initiativeText = Text.literal(String.valueOf(initiativeTotal));
            int initiativeWidth = renderer.getWidth(initiativeText);
            drawContext.drawText(renderer, initiativeText, rowRight - initiativeWidth - 4, rowTop + 1, 0xFF9AD7FF, false);
        }

        if (hasOverflow) {
            int remaining = totalEntries - rows;
            Text overflow = Text.literal("+ " + remaining + " more");
            drawContext.drawText(
                renderer,
                overflow,
                x + TURN_PANEL_PADDING,
                y + panelHeight - TURN_ROW_HEIGHT + 2,
                0xFFAAAAAA,
                false
            );
        }
    }

    private void renderEndTurnButton(
        DrawContext drawContext,
        MinecraftClient client,
        PanelBounds hotbarBounds,
        ClientEncounterState.CombatStateData combatState,
        boolean isPlayersTurn,
        double mouseX,
        double mouseY
    ) {
        if (hotbarBounds == null) {
            endTurnButtonBounds = null;
            endTurnButtonEnabled = false;
            endTurnStatusText = Text.empty();
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int x = hotbarBounds.x() + hotbarBounds.width() + 8;
        if (x + END_TURN_BUTTON_WIDTH > screenWidth - 6) {
            x = screenWidth - END_TURN_BUTTON_WIDTH - 6;
        }
        int y = hotbarBounds.y() + hotbarBounds.height() - END_TURN_BUTTON_HEIGHT;

        boolean enabled = isPlayersTurn && combatState != null;
        endTurnButtonBounds = new Rect(x, y, END_TURN_BUTTON_WIDTH, END_TURN_BUTTON_HEIGHT);
        endTurnButtonEnabled = enabled;
        endTurnStatusText = enabled
            ? Text.literal("End your turn and advance the initiative.")
            : (combatState == null ? Text.literal("Awaiting combat sync") : Text.literal("Not your turn"));

        boolean hovered = cursorUnlockedForHotbar && endTurnButtonBounds.contains(mouseX, mouseY);
        int background = enabled ? 0xB01D2F1D : 0x60101010;
        if (hovered) {
            background = enabled ? 0xC0283B28 : 0x70353535;
        }

        drawContext.fill(x, y, x + END_TURN_BUTTON_WIDTH, y + END_TURN_BUTTON_HEIGHT, background);
        drawBorder(drawContext, x, y, END_TURN_BUTTON_WIDTH, END_TURN_BUTTON_HEIGHT, enabled ? 0xFF7BC37E : 0xFF5A5A5A);

        Text label = Text.literal("End Turn");
        int labelX = x + (END_TURN_BUTTON_WIDTH - client.textRenderer.getWidth(label)) / 2;
        drawContext.drawTextWithShadow(client.textRenderer, label, labelX, y + 6, enabled ? 0xFFFFFFFF : 0xFFB0B0B0);

        Text keyText = END_TURN_KEY.getBoundKeyLocalizedText();
        int keyWidth = client.textRenderer.getWidth(keyText);
        drawContext.drawText(client.textRenderer, keyText, x + END_TURN_BUTTON_WIDTH - keyWidth - 4, y + END_TURN_BUTTON_HEIGHT - 9, 0xFFE0E0E0, false);

        if (hovered) {
            drawEndTurnTooltip(drawContext, client, mouseX, mouseY);
        }
    }

    private void renderCursorHint(DrawContext drawContext, MinecraftClient client, PanelBounds hotbarBounds) {
        if (hotbarBounds == null || cursorUnlockedForHotbar || client == null) {
            return;
        }
        int textWidth = client.textRenderer.getWidth(CURSOR_HINT_TEXT);
        int x = hotbarBounds.x() + (hotbarBounds.width() - textWidth) / 2;
        int y = hotbarBounds.y() + hotbarBounds.height() + 6;
        drawContext.drawText(client.textRenderer, CURSOR_HINT_TEXT, x, y, 0xFFB7B7B7, false);
    }

    private void renderTurnBanner(DrawContext drawContext, MinecraftClient client) {
        if (turnBannerTicksRemaining <= 0 || turnBannerPrimaryText == null || turnBannerPrimaryText.getString().isEmpty()) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        TextRenderer renderer = client.textRenderer;
        int primaryWidth = renderer.getWidth(turnBannerPrimaryText);
        int secondaryWidth = turnBannerSecondaryText != null ? renderer.getWidth(turnBannerSecondaryText) : 0;
        boolean hasSecondary = turnBannerSecondaryText != null && !turnBannerSecondaryText.getString().isEmpty();
        int bannerWidth = Math.max(primaryWidth, secondaryWidth) + TURN_BANNER_PADDING * 2;
        int bannerHeight = hasSecondary ? 34 : 24;
        int x = (screenWidth - bannerWidth) / 2;
        int y = TURN_BANNER_MARGIN_TOP;

        int background = turnBannerIsPlayer ? 0xC0283B28 : 0xC0121212;
        int border = turnBannerIsPlayer ? 0xFF7BC37E : 0xFF5A5A5A;
        if (turnBannerTicksRemaining < TURN_BANNER_FADE_TICKS) {
            float alpha = turnBannerTicksRemaining / (float) TURN_BANNER_FADE_TICKS;
            int alphaByte = (int) (alpha * 255) << 24;
            background = (background & 0x00FFFFFF) | alphaByte;
            border = (border & 0x00FFFFFF) | alphaByte;
        }

        drawContext.fill(x, y, x + bannerWidth, y + bannerHeight, background);
        drawBorder(drawContext, x, y, bannerWidth, bannerHeight, border);

        int primaryX = x + (bannerWidth - primaryWidth) / 2;
        drawContext.drawText(renderer, turnBannerPrimaryText, primaryX, y + 6, 0xFFF8F3DF, false);

        if (hasSecondary) {
            int secondaryX = x + (bannerWidth - secondaryWidth) / 2;
            drawContext.drawText(renderer, turnBannerSecondaryText, secondaryX, y + 18, 0xFFE4E0D1, false);
        }
    }

    private void drawEndTurnTooltip(DrawContext drawContext, MinecraftClient client, double mouseX, double mouseY) {
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal("End Turn"));
        tooltip.add(endTurnStatusText);
        tooltip.add(Text.literal("Hotkey: " + END_TURN_KEY.getBoundKeyLocalizedText().getString()));
        drawContext.drawTooltip(client.textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private ButtonState determineButtonState(
        ActionButtonDefinition definition,
        boolean isPlayersTurn,
        ClientEncounterState.CombatStateData combatState
    ) {
        if (combatState == null) {
            return new ButtonState(false, Text.literal("Awaiting combat data"));
        }
        if (!isPlayersTurn) {
            return new ButtonState(false, Text.literal("Wait for your turn"));
        }

        boolean hasResource = switch (definition.resourceType()) {
            case ACTION -> combatState.hasAction();
            case BONUS_ACTION -> combatState.hasBonusAction();
            case REACTION -> combatState.hasReaction();
        };

        if (!hasResource) {
            return new ButtonState(false, definition.resourceType().spentText());
        }

        return new ButtonState(true, Text.literal("Ready"));
    }

    private void renderButton(
        DrawContext drawContext,
        MinecraftClient client,
        ActionButtonDefinition definition,
        ButtonState state,
        int x,
        int y,
        double mouseX,
        double mouseY
    ) {
        boolean hovered = cursorUnlockedForHotbar && mouseX >= x && mouseX <= x + BUTTON_WIDTH && mouseY >= y && mouseY <= y + BUTTON_HEIGHT;
        int background = state.enabled() ? 0xB0101010 : 0x60101010;
        if (hovered) {
            background = state.enabled() ? 0xC0151515 : 0x70151515;
        }

        drawContext.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, background);
        drawContext.fill(x, y, x + BUTTON_WIDTH, y + 2, definition.resourceType().color());
        drawBorder(drawContext, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, state.enabled() ? 0xFF4E89D8 : 0xFF3A3A3A);

        int labelColor = state.enabled() ? 0xFFFFFFFF : 0xFFA0A0A0;
        int labelX = x + (BUTTON_WIDTH - client.textRenderer.getWidth(definition.label())) / 2;
        drawContext.drawTextWithShadow(client.textRenderer, definition.label(), labelX, y + 6, labelColor);

        drawContext.drawText(
            client.textRenderer,
            definition.resourceType().displayText(),
            x + 4,
            y + BUTTON_HEIGHT - 9,
            definition.resourceType().color(),
            false
        );

        Text keyText = definition.keyBinding().getBoundKeyLocalizedText();
        if (!keyText.getString().isEmpty()) {
            int keyWidth = client.textRenderer.getWidth(keyText);
            drawContext.drawText(
                client.textRenderer,
                keyText,
                x + BUTTON_WIDTH - keyWidth - 4,
                y + BUTTON_HEIGHT - 9,
                0xFFCCCCCC,
                false
            );
        }
    }

    private void drawActionTooltip(DrawContext drawContext, MinecraftClient client, RenderedButton hovered, double mouseX, double mouseY) {
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(hovered.definition().label());
        tooltip.add(hovered.definition().description());
        tooltip.add(hovered.definition().resourceType().tooltipLine());
        tooltip.add(hovered.statusText());
        drawContext.drawTooltip(client.textRenderer, tooltip, (int) mouseX, (int) mouseY);
    }

    private void handleMouseClick(MinecraftClient client) {
        if (!cursorUnlockedForHotbar || !shouldRender(client)) {
            return;
        }
        double mouseX = client.mouse.getX() / client.getWindow().getScaleFactor();
        double mouseY = client.mouse.getY() / client.getWindow().getScaleFactor();
        for (RenderedButton button : renderedButtons) {
            if (button.enabled() && button.contains(mouseX, mouseY)) {
                sendActionIfAllowed(client, button.definition().actionType());
                return;
            }
        }
        if (endTurnButtonEnabled && endTurnButtonBounds != null && endTurnButtonBounds.contains(mouseX, mouseY)) {
            sendEndTurnIfAllowed(client);
        }
    }

    private void sendActionIfAllowed(MinecraftClient client, UseActionPayload.ActionType actionType) {
        if (client.player == null) {
            return;
        }

        ClientEncounterState state = ClientEncounterState.getInstance();
        UUID playerId = client.player.getUuid();
        UUID encounterId = state.getEncounterForEntity(playerId);
        if (encounterId == null) {
            return;
        }

        ClientEncounterState.EncounterData encounter = state.getEncounter(encounterId);
        ClientEncounterState.CombatStateData combatState = state.getCombatState(playerId);
        if (encounter == null || encounter.ended()) {
            return;
        }

        boolean isPlayersTurn = playerId.equals(encounter.getCurrentTurnEntity());
        ActionButtonDefinition definition = BUTTON_LOOKUP.get(actionType);
        if (definition == null) {
            return;
        }

        ButtonState buttonState = determineButtonState(definition, isPlayersTurn, combatState);
        if (!buttonState.enabled()) {
            return;
        }

        ClientPlayNetworking.send(new UseActionPayload(actionType, Optional.empty()));
    }

    private void sendEndTurnIfAllowed(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        ClientEncounterState state = ClientEncounterState.getInstance();
        UUID playerId = client.player.getUuid();
        UUID encounterId = state.getEncounterForEntity(playerId);
        if (encounterId == null) {
            return;
        }

        ClientEncounterState.EncounterData encounter = state.getEncounter(encounterId);
        if (encounter == null || encounter.ended()) {
            return;
        }

        if (!playerId.equals(encounter.getCurrentTurnEntity())) {
            return;
        }

        ClientPlayNetworking.send(new EndTurnPayload());
    }

    private void clearInteractiveElements() {
        renderedButtons.clear();
        endTurnButtonBounds = null;
        endTurnButtonEnabled = false;
        endTurnStatusText = Text.empty();
        resetTurnBanner();
    }

    private void tickTurnBannerTimer() {
        if (turnBannerTicksRemaining > 0) {
            turnBannerTicksRemaining--;
            if (turnBannerTicksRemaining <= 0) {
                resetTurnBanner();
            }
        }
    }

    private void resetTurnBanner() {
        turnBannerEncounterId = null;
        turnBannerEntityId = null;
        turnBannerTicksRemaining = 0;
        turnBannerPrimaryText = Text.empty();
        turnBannerSecondaryText = Text.empty();
        turnBannerIsPlayer = false;
    }

    private void updateTurnBanner(UUID encounterId, UUID entityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        ClientEncounterState state = ClientEncounterState.getInstance();
        ClientEncounterState.EncounterData encounter = state.getEncounter(encounterId);
        if (encounter == null) {
            return;
        }
        Text displayName = resolveDisplayName(encounter.turnOrder(), entityId);
        boolean isPlayerTurn = client.player.getUuid().equals(entityId);

        this.turnBannerEncounterId = encounterId;
        this.turnBannerEntityId = entityId;
        this.turnBannerPrimaryText = isPlayerTurn
            ? Text.literal("Your Turn")
            : displayName;
        this.turnBannerSecondaryText = buildTurnBannerSubtitle(isPlayerTurn, displayName);
        this.turnBannerIsPlayer = isPlayerTurn;
        this.turnBannerTicksRemaining = TURN_BANNER_DURATION_TICKS;
    }

    private void handleTurnEndNotification(UUID encounterId, UUID entityId) {
        if (turnBannerEncounterId == null || !turnBannerEncounterId.equals(encounterId)) {
            return;
        }
        if (turnBannerEntityId != null && turnBannerEntityId.equals(entityId)) {
            turnBannerTicksRemaining = Math.min(turnBannerTicksRemaining, TURN_BANNER_FADE_TICKS);
        }
    }

    private Text buildTurnBannerSubtitle(boolean isPlayerTurn, Text targetName) {
        if (isPlayerTurn) {
            String keyName = END_TURN_KEY.getBoundKeyLocalizedText().getString();
            return Text.literal("Choose an action or press " + keyName + " to end turn");
        }
        return Text.literal("Waiting for " + targetName.getString());
    }

    private Text resolveDisplayName(List<SyncEncounterStatePayload.InitiativeEntry> turnOrder, UUID entityId) {
        for (SyncEncounterStatePayload.InitiativeEntry entry : turnOrder) {
            if (entry.entityId().equals(entityId)) {
                Text display = entry.displayName();
                if (display != null && !display.getString().isEmpty()) {
                    return Text.literal(display.getString());
                }
                break;
            }
        }
        String fallback = entityId.toString();
        return Text.literal(fallback.substring(0, Math.min(8, fallback.length())));
    }

    private void handleCursorState(MinecraftClient client, boolean overlayActive, boolean altHeld) {
        if (!overlayActive || client.currentScreen != null || client.isPaused()) {
            if (cursorUnlockedForHotbar) {
                cursorUnlockedForHotbar = false;
                if (!client.mouse.isCursorLocked()) {
                    client.mouse.lockCursor();
                }
            }
            return;
        }

        if (altHeld && !cursorUnlockedForHotbar) {
            client.mouse.unlockCursor();
            cursorUnlockedForHotbar = true;
        } else if (!altHeld && cursorUnlockedForHotbar) {
            cursorUnlockedForHotbar = false;
            client.mouse.lockCursor();
        }
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static ActionButtonDefinition createActionButton(
        String label,
        String description,
        UseActionPayload.ActionType actionType,
        ResourceType resourceType,
        int defaultKey
    ) {
        KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fiveesrd.actionhotbar." + actionType.name().toLowerCase(Locale.ROOT),
            InputUtil.Type.KEYSYM,
            defaultKey,
            KEYBIND_CATEGORY
        ));

        return new ActionButtonDefinition(
            actionType,
            Text.literal(label),
            Text.literal(description),
            resourceType,
            keyBinding
        );
    }

    private record ActionButtonDefinition(
        UseActionPayload.ActionType actionType,
        Text label,
        Text description,
        ResourceType resourceType,
        KeyBinding keyBinding
    ) {}

    private record ButtonState(boolean enabled, Text statusText) {}

    private record RenderedButton(
        ActionButtonDefinition definition,
        int x,
        int y,
        int width,
        int height,
        boolean enabled,
        Text statusText
    ) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private record PanelBounds(int x, int y, int width, int height) {}

    private record Rect(int x, int y, int width, int height) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private enum ResourceType {
        ACTION("Action", 0xFFE0A458),
        BONUS_ACTION("Bonus", 0xFF78A0FF),
        REACTION("Reaction", 0xFFF2D479);

        private final String label;
        private final Text displayText;
        private final int color;

        ResourceType(String label, int color) {
            this.label = label;
            this.displayText = Text.literal(label);
            this.color = color;
        }

        public Text displayText() {
            return displayText;
        }

        public int color() {
            return color;
        }

        public Text tooltipLine() {
            return Text.literal("Resource: " + label);
        }

        public Text spentText() {
            return Text.literal(label + " already used");
        }
    }
}
