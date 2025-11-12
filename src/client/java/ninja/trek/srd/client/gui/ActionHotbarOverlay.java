package ninja.trek.srd.client.gui;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.KeyBinding.Category;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ninja.trek.srd.FiveESrdMod;
import ninja.trek.srd.client.ClientEncounterState;
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
    private boolean cursorUnlockedForHotbar;

    private ActionHotbarOverlay() {}

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
        if (!shouldRender(client)) {
            renderedButtons.clear();
            return;
        }

        UUID playerId = client.player.getUuid();
        ClientEncounterState state = ClientEncounterState.getInstance();
        UUID encounterId = state.getEncounterForEntity(playerId);
        if (encounterId == null) {
            renderedButtons.clear();
            return;
        }

        ClientEncounterState.EncounterData encounterData = state.getEncounter(encounterId);
        if (encounterData == null || encounterData.ended()) {
            renderedButtons.clear();
            return;
        }

        ClientEncounterState.CombatStateData combatState = state.getCombatState(playerId);
        boolean isPlayersTurn = playerId.equals(encounterData.getCurrentTurnEntity());

        renderHotbar(drawContext, client, encounterData, combatState, isPlayersTurn);
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

    private void renderHotbar(
        DrawContext drawContext,
        MinecraftClient client,
        ClientEncounterState.EncounterData encounter,
        ClientEncounterState.CombatStateData combatState,
        boolean isPlayersTurn
    ) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int contentHeight = BUTTON_ROWS.size() * BUTTON_HEIGHT + Math.max(0, BUTTON_ROWS.size() - 1) * ROW_SPACING;
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

        double mouseX = client.mouse.getX() / client.getWindow().getScaleFactor();
        double mouseY = client.mouse.getY() / client.getWindow().getScaleFactor();
        RenderedButton hovered = null;

        renderedButtons.clear();
        int currentY = panelY + PANEL_PADDING;
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
            drawTooltip(drawContext, client, hovered, mouseX, mouseY);
        }
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

    private void drawTooltip(DrawContext drawContext, MinecraftClient client, RenderedButton hovered, double mouseX, double mouseY) {
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
            "key.5e-srd.actionhotbar." + actionType.name().toLowerCase(Locale.ROOT),
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
