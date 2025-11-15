package ninja.trek.srd.client.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import ninja.trek.srd.character.data.CharacterClass;
import ninja.trek.srd.character.data.CharacterAppearance;
import ninja.trek.srd.character.data.CharacterStats;
import ninja.trek.srd.character.data.Race;
import ninja.trek.srd.character.entity.CharacterEntity;
import ninja.trek.srd.registry.ModEntities;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Multi-step character creation screen for D&D 5e characters.
 * Steps: Race -> Class -> Ability Scores -> Appearance -> Name -> Review
 */
public class CharacterCreationScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int SPACING = 25;
    private static final int PREVIEW_PANEL_WIDTH = 150;
    private static final int PREVIEW_PANEL_HEIGHT = 230;
    private static final int PREVIEW_MARGIN = 20;
    private static final int PREVIEW_BACKGROUND_COLOR = 0xAA111111;
    private static final int PREVIEW_BORDER_COLOR = 0xFFFFFFFF;
    private static final Text PREVIEW_TITLE = Text.literal("Character Preview");

    private enum CreationStep {
        RACE,
        CLASS,
        ABILITY_SCORES,
        APPEARANCE,
        NAME,
        REVIEW
    }

    private CreationStep currentStep = CreationStep.RACE;

    // Character data being built
    private Race selectedRace = Race.HUMAN;
    private CharacterClass selectedClass = CharacterClass.FIGHTER;
    private int[] abilityScores = new int[6]; // STR, DEX, CON, INT, WIS, CHA
    private List<Integer> availableScores = new ArrayList<>();
    private int bodyIndex = 0;
    private int legsIndex = 0;
    private int armsIndex = 0;
    private int headIndex = 0;
    private String characterName = "";
    private String currentTitle = "";

    // UI widgets
    private TextFieldWidget nameField;
    private final List<ButtonWidget> stepButtons = new ArrayList<>();
    private CharacterEntity previewEntity;
    private boolean previewDirty = true;

    // Preview interaction state
    private boolean isDraggingPreview = false;
    private float previewYaw = 0.0f;
    private float previewPitch = 0.0f;
    private float previewZoom = 1.0f;

    public CharacterCreationScreen() {
        super(Text.literal("Character Creation"));
        // Initialize available scores with standard array
        availableScores.addAll(Arrays.asList(15, 14, 13, 12, 10, 8));
    }

    @Override
    protected void init() {
        super.init();
        stepButtons.clear();

        int centerX = this.width / 2;
        int startY = this.height / 4;

        switch (currentStep) {
            case RACE -> initRaceStep(centerX, startY);
            case CLASS -> initClassStep(centerX, startY);
            case ABILITY_SCORES -> initAbilityScoresStep(centerX, startY);
            case APPEARANCE -> initAppearanceStep(centerX, startY);
            case NAME -> initNameStep(centerX, startY);
            case REVIEW -> initReviewStep(centerX, startY);
        }

        // Add navigation buttons
        addNavigationButtons(centerX, this.height - 40);
    }

    private void initRaceStep(int centerX, int startY) {
        addTitle("Choose Your Race");

        int y = startY;
        for (Race race : Race.values()) {
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(capitalize(race.getName())),
                btn -> {
                    selectedRace = race;
                    // Update appearance defaults
                    bodyIndex = race.getDefaultBodyIndex();
                    legsIndex = race.getDefaultLegsIndex();
                    armsIndex = race.getDefaultArmsIndex();
                    headIndex = race.getDefaultHeadIndex();
                    markPreviewDirty();
                    nextStep();
                }
            )
            .dimensions(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

            stepButtons.add(button);
            addDrawableChild(button);
            y += SPACING;
        }
    }

    private void initClassStep(int centerX, int startY) {
        addTitle("Choose Your Class");

        int y = startY;
        for (CharacterClass charClass : CharacterClass.values()) {
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(capitalize(charClass.getName())),
                btn -> {
                    selectedClass = charClass;
                    markPreviewDirty();
                    nextStep();
                }
            )
            .dimensions(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build();

            stepButtons.add(button);
            addDrawableChild(button);
            y += SPACING;
        }
    }

    private void initAbilityScoresStep(int centerX, int startY) {
        addTitle("Assign Ability Scores");

        String[] abilityNames = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};

        int y = startY - 20;
        for (int i = 0; i < 6; i++) {
            final int index = i;
            int currentScore = abilityScores[i];

            // Create buttons to cycle through available scores
            ButtonWidget decreaseBtn = ButtonWidget.builder(
                Text.literal("<"),
                btn -> cycleAbilityScore(index, false)
            )
            .dimensions(centerX - 120, y, 20, BUTTON_HEIGHT)
            .build();

            ButtonWidget increaseBtn = ButtonWidget.builder(
                Text.literal(">"),
                btn -> cycleAbilityScore(index, true)
            )
            .dimensions(centerX + 100, y, 20, BUTTON_HEIGHT)
            .build();

            stepButtons.add(decreaseBtn);
            stepButtons.add(increaseBtn);
            addDrawableChild(decreaseBtn);
            addDrawableChild(increaseBtn);

            y += SPACING;
        }
    }

    private void initAppearanceStep(int centerX, int startY) {
        addTitle("Customize Appearance");

        String[] partNames = {"Body", "Legs", "Arms", "Head"};
        int[] indices = {bodyIndex, legsIndex, armsIndex, headIndex};

        int y = startY;
        for (int i = 0; i < 4; i++) {
            final int partIndex = i;

            ButtonWidget decreaseBtn = ButtonWidget.builder(
                Text.literal("<"),
                btn -> decreaseAppearance(partIndex)
            )
            .dimensions(centerX - 120, y, 20, BUTTON_HEIGHT)
            .build();

            ButtonWidget increaseBtn = ButtonWidget.builder(
                Text.literal(">"),
                btn -> increaseAppearance(partIndex)
            )
            .dimensions(centerX + 100, y, 20, BUTTON_HEIGHT)
            .build();

            stepButtons.add(decreaseBtn);
            stepButtons.add(increaseBtn);
            addDrawableChild(decreaseBtn);
            addDrawableChild(increaseBtn);

            y += SPACING;
        }
    }

    private void initNameStep(int centerX, int startY) {
        addTitle("Name Your Character");

        nameField = new TextFieldWidget(
            this.textRenderer,
            centerX - BUTTON_WIDTH / 2,
            startY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            Text.literal("Character Name")
        );
        nameField.setMaxLength(32);
        nameField.setText(characterName);
        nameField.setChangedListener(text -> characterName = text);

        addDrawableChild(nameField);
        setInitialFocus(nameField);
    }

    private void initReviewStep(int centerX, int startY) {
        addTitle("Review Your Character");

        ButtonWidget createButton = ButtonWidget.builder(
            Text.literal("Create Character"),
            btn -> createCharacter()
        )
        .dimensions(centerX - BUTTON_WIDTH / 2, this.height / 2 + 60, BUTTON_WIDTH, BUTTON_HEIGHT)
        .build();

        stepButtons.add(createButton);
        addDrawableChild(createButton);
    }

    private void addNavigationButtons(int centerX, int y) {
        // Back button (except on first step)
        if (currentStep != CreationStep.RACE) {
            ButtonWidget backButton = ButtonWidget.builder(
                Text.literal("Back"),
                btn -> previousStep()
            )
            .dimensions(centerX - BUTTON_WIDTH - 10, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT)
            .build();

            addDrawableChild(backButton);
        }

        // Next button (except on last steps)
        if (currentStep != CreationStep.REVIEW && currentStep != CreationStep.NAME) {
            ButtonWidget nextButton = ButtonWidget.builder(
                Text.literal("Next"),
                btn -> nextStep()
            )
            .dimensions(centerX + 10, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT)
            .build();

            addDrawableChild(nextButton);
        } else if (currentStep == CreationStep.NAME) {
            ButtonWidget nextButton = ButtonWidget.builder(
                Text.literal("Next"),
                btn -> {
                    if (!characterName.isEmpty()) {
                        nextStep();
                    }
                }
            )
            .dimensions(centerX + 10, y, BUTTON_WIDTH / 2, BUTTON_HEIGHT)
            .build();

            addDrawableChild(nextButton);
        }

        // Cancel button
        ButtonWidget cancelButton = ButtonWidget.builder(
            Text.literal("Cancel"),
            btn -> close()
        )
        .dimensions(10, this.height - 30, 80, BUTTON_HEIGHT)
        .build();

        addDrawableChild(cancelButton);
    }

    private void cycleAbilityScore(int abilityIndex, boolean increase) {
        // Return current score to available pool if it's not 0
        if (abilityScores[abilityIndex] != 0) {
            availableScores.add(abilityScores[abilityIndex]);
        }

        // Get next available score
        if (!availableScores.isEmpty()) {
            availableScores.sort(Integer::compareTo);
            int scoreIndex = availableScores.indexOf(abilityScores[abilityIndex]);

            if (increase) {
                scoreIndex = (scoreIndex + 1) % availableScores.size();
            } else {
                scoreIndex = (scoreIndex - 1 + availableScores.size()) % availableScores.size();
            }

            int newScore = availableScores.get(scoreIndex);
            availableScores.remove(Integer.valueOf(newScore));
            abilityScores[abilityIndex] = newScore;
        }

        clearAndInit();
        markPreviewDirty();
    }

    private void decreaseAppearance(int partIndex) {
        switch (partIndex) {
            case 0 -> bodyIndex = Math.max(0, bodyIndex - 1);
            case 1 -> legsIndex = Math.max(0, legsIndex - 1);
            case 2 -> armsIndex = Math.max(0, armsIndex - 1);
            case 3 -> headIndex = Math.max(0, headIndex - 1);
        }
        clearAndInit();
        markPreviewDirty();
    }

    private void increaseAppearance(int partIndex) {
        switch (partIndex) {
            case 0 -> bodyIndex++;
            case 1 -> legsIndex++;
            case 2 -> armsIndex++;
            case 3 -> headIndex++;
        }
        clearAndInit();
        markPreviewDirty();
    }

    private void nextStep() {
        currentStep = CreationStep.values()[Math.min(currentStep.ordinal() + 1, CreationStep.values().length - 1)];
        clearAndInit();
    }

    private void previousStep() {
        currentStep = CreationStep.values()[Math.max(currentStep.ordinal() - 1, 0)];
        clearAndInit();
    }

    private void createCharacter() {
        // Validate all required fields are filled
        if (characterName.isEmpty()) {
            return;
        }

        // Check all ability scores are assigned
        for (int score : abilityScores) {
            if (score == 0) {
                return;
            }
        }

        // Build character stats (base stats, not with racial bonuses - server will apply them)
        CharacterStats baseStats = new CharacterStats(
            abilityScores[0], // STR
            abilityScores[1], // DEX
            abilityScores[2], // CON
            abilityScores[3], // INT
            abilityScores[4], // WIS
            abilityScores[5]  // CHA
        );

        // Create appearance
        ninja.trek.srd.character.data.CharacterAppearance appearance =
            new ninja.trek.srd.character.data.CharacterAppearance(
                bodyIndex, legsIndex, armsIndex, headIndex
            );

        // Send character creation packet to server
        ninja.trek.srd.network.payloads.CreateCharacterPayload payload =
            new ninja.trek.srd.network.payloads.CreateCharacterPayload(
                characterName,
                selectedRace,
                selectedClass,
                baseStats,
                appearance
            );

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload);

        // Close the screen
        close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Step 1: Render background first (bottom layer)
        this.renderInGameBackground(context);

        int centerX = this.width / 2;
        int startY = this.height / 4;

        // Step 2: Render title text
        if (!currentTitle.isEmpty()) {
            drawCenteredText(context, currentTitle, centerX, 40);
        }

        // Step 3: Render step-specific text content (middle layer)
        switch (currentStep) {
            case RACE -> renderRaceInfo(context, centerX, startY);
            case CLASS -> renderClassInfo(context, centerX, startY);
            case ABILITY_SCORES -> renderAbilityScoresInfo(context, centerX, startY);
            case APPEARANCE -> renderAppearanceInfo(context, centerX, startY);
            case NAME -> renderNameInfo(context, centerX, startY);
            case REVIEW -> renderReviewInfo(context, centerX, startY);
        }

        renderCharacterPreview(context, mouseX, mouseY);

        // Step 4: Render widgets/buttons on top (top layer)
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCharacterPreview(DrawContext context, int mouseX, int mouseY) {
        if (this.client == null) {
            return;
        }

        ensurePreviewEntity();
        if (previewEntity == null) {
            return;
        }

        if (previewDirty) {
            updatePreviewEntityState();
        }

        previewEntity.tick();
        // Increment age for animation
        previewEntity.age++;

        int panelWidth = PREVIEW_PANEL_WIDTH;
        int availableHeight = Math.max(140, this.height - PREVIEW_MARGIN * 2);
        int panelHeight = Math.min(PREVIEW_PANEL_HEIGHT, availableHeight);
        int left = Math.max(PREVIEW_MARGIN, this.width - panelWidth - PREVIEW_MARGIN);
        int top = (this.height - panelHeight) / 2;

        context.fill(left, top, left + panelWidth, top + panelHeight, PREVIEW_BACKGROUND_COLOR);
        drawPanelBorder(context, left, top, panelWidth, panelHeight, PREVIEW_BORDER_COLOR);
        context.drawText(this.textRenderer, PREVIEW_TITLE, left + 8, top + 8, 0xFFFFFFFF, false);

        // Use stored rotation and zoom values
        int titleHeight = 20;  // Account for "Character Preview" title
        int baseRenderSize = (int)(panelHeight * 0.32f);  // Base size to show full character
        int renderSize = (int)(baseRenderSize * previewZoom);  // Apply zoom

        // Pass the full panel bounds for rendering
        drawPreviewEntity(context, left, top + titleHeight, panelWidth, panelHeight - titleHeight,
                         renderSize, previewYaw, previewPitch);
    }

    private void drawPreviewEntity(DrawContext context, int panelLeft, int panelTop, int panelWidth, int panelHeight,
                                   int size, float yaw, float pitch) {
        if (previewEntity == null) {
            return;
        }

        int x1 = panelLeft;
        int y1 = panelTop;
        int x2 = panelLeft + panelWidth;
        int y2 = panelTop + panelHeight;

        context.enableScissor(x1, y1, x2, y2);

        float clampedPitch = Math.max(-45.0f, Math.min(45.0f, pitch));
        previewEntity.setBodyYaw(180.0f + yaw * 0.5f);
        previewEntity.setYaw(180.0f + yaw);
        previewEntity.setHeadYaw(previewEntity.getYaw());
        previewEntity.setPitch(clampedPitch);

        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf cameraAngle = new Quaternionf().rotateX((float)Math.toRadians(clampedPitch));
        rotation.mul(cameraAngle);

        float entityScale = previewEntity.getScale();
        float renderScale = size / entityScale;
        Vector3f translation = new Vector3f(
            0.0f,
            previewEntity.getHeight() / 2.0f + 0.0625f * entityScale,
            0.0f
        );

        InventoryScreen.drawEntity(
            context,
            x1,
            y1,
            x2,
            y2,
            renderScale,
            translation,
            rotation,
            cameraAngle,
            previewEntity
        );

        context.disableScissor();
    }

    private void drawPanelBorder(DrawContext context, int left, int top, int width, int height, int color) {
        context.fill(left, top, left + width, top + 1, color);
        context.fill(left, top + height - 1, left + width, top + height, color);
        context.fill(left, top, left + 1, top + height, color);
        context.fill(left + width - 1, top, left + width, top + height, color);
    }

    private void ensurePreviewEntity() {
        if (this.client == null) {
            return;
        }

        ClientWorld world = this.client.world;
        if (world == null) {
            previewEntity = null;
            return;
        }

        if (previewEntity != null && previewEntity.getEntityWorld() != world) {
            previewEntity = null;
            previewDirty = true;
        }

        if (previewEntity == null) {
            previewEntity = new CharacterEntity(ModEntities.CHARACTER, world);
            if (previewEntity != null) {
                previewEntity.setAiDisabled(true);
                previewEntity.setSilent(true);
                previewEntity.setNoGravity(true);
                previewEntity.refreshPositionAndAngles(0.0, world.getBottomY(), 0.0, 180.0f, 0.0f);
                previewEntity.setBodyYaw(180.0f);
                previewEntity.setHeadYaw(180.0f);
                previewEntity.setYaw(180.0f);
                previewEntity.setPitch(0.0f);
                // Set age to ensure rendering works properly
                previewEntity.age = 1;
                previewDirty = true;
            }
        }
    }

    private void updatePreviewEntityState() {
        if (previewEntity == null) {
            return;
        }

        CharacterAppearance appearance = new CharacterAppearance(bodyIndex, legsIndex, armsIndex, headIndex);
        previewEntity.initializeCharacter(selectedRace, selectedClass, buildPreviewStats(), appearance);
        previewEntity.setBodyYaw(180.0f);
        previewEntity.setYaw(180.0f);
        previewEntity.setHeadYaw(180.0f);
        previewEntity.setPitch(0.0f);
        previewDirty = false;
    }

    private CharacterStats buildPreviewStats() {
        for (int score : abilityScores) {
            if (score == 0) {
                return CharacterStats.createDefault();
            }
        }

        return new CharacterStats(
            abilityScores[0],
            abilityScores[1],
            abilityScores[2],
            abilityScores[3],
            abilityScores[4],
            abilityScores[5]
        );
    }

    private void markPreviewDirty() {
        previewDirty = true;
    }

    private void renderRaceInfo(DrawContext context, int centerX, int startY) {
        int y = startY + SPACING * Race.values().length + 10;

        drawCenteredText(context, "Race provides ability score bonuses and traits", centerX, y);
        y += 15;
        drawCenteredText(context, "Human: +1 to all abilities", centerX, y);
        y += 12;
        drawCenteredText(context, "Dwarf: +2 Constitution, Darkvision", centerX, y);
    }

    private void renderClassInfo(DrawContext context, int centerX, int startY) {
        int y = startY + SPACING * CharacterClass.values().length + 10;

        drawCenteredText(context, "Class determines your hit die and abilities", centerX, y);
        y += 15;
        drawCenteredText(context, "Fighter: d10 hit die, martial combat expert", centerX, y);
    }

    private void renderAbilityScoresInfo(DrawContext context, int centerX, int startY) {
        String[] abilityNames = {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"};

        drawCenteredText(context, "Standard Array: 15, 14, 13, 12, 10, 8", centerX, startY - 40);

        int y = startY - 20;
        for (int i = 0; i < 6; i++) {
            String text = abilityNames[i] + ": " + (abilityScores[i] == 0 ? "-" : abilityScores[i]);
            drawCenteredText(context, text, centerX, y + 5);
            y += SPACING;
        }

        // Show remaining scores
        y += 10;
        if (!availableScores.isEmpty()) {
            StringBuilder remaining = new StringBuilder("Available: ");
            for (int score : availableScores) {
                remaining.append(score).append(" ");
            }
            drawCenteredText(context, remaining.toString(), centerX, y);
        } else {
            drawCenteredText(context, "All scores assigned!", centerX, y);
        }
    }

    private void renderAppearanceInfo(DrawContext context, int centerX, int startY) {
        String[] partNames = {"Body", "Legs", "Arms", "Head"};
        int[] indices = {bodyIndex, legsIndex, armsIndex, headIndex};

        int y = startY;
        for (int i = 0; i < 4; i++) {
            String text = partNames[i] + ": " + indices[i];
            drawCenteredText(context, text, centerX, y + 5);
            y += SPACING;
        }
    }

    private void renderNameInfo(DrawContext context, int centerX, int startY) {
        drawCenteredText(context, "Enter a name for your character", centerX, startY - 30);
    }

    private void renderReviewInfo(DrawContext context, int centerX, int startY) {
        int y = startY;

        drawCenteredText(context, "Name: " + characterName, centerX, y);
        y += 20;
        drawCenteredText(context, "Race: " + capitalize(selectedRace.getName()), centerX, y);
        y += 15;
        drawCenteredText(context, "Class: " + capitalize(selectedClass.getName()), centerX, y);
        y += 20;

        // Apply racial bonuses for display
        CharacterStats baseStats = new CharacterStats(
            abilityScores[0], abilityScores[1], abilityScores[2],
            abilityScores[3], abilityScores[4], abilityScores[5]
        );
        CharacterStats finalStats = baseStats.applyRacialBonuses(selectedRace);

        String[] abilityNames = {"STR", "DEX", "CON", "INT", "WIS", "CHA"};
        int[] finalScores = {
            finalStats.strength(), finalStats.dexterity(), finalStats.constitution(),
            finalStats.intelligence(), finalStats.wisdom(), finalStats.charisma()
        };

        StringBuilder abilitiesLine = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            abilitiesLine.append(abilityNames[i]).append(": ").append(finalScores[i]);
            if (i < 5) abilitiesLine.append(" | ");
        }
        drawCenteredText(context, abilitiesLine.toString(), centerX, y);
    }

    private void drawCenteredText(DrawContext context, String text, int x, int y) {
        context.drawText(
            this.textRenderer,
            text,
            x - this.textRenderer.getWidth(text) / 2,
            y,
            0xFFFFFFFF,  // ARGB format (FF=opaque, FFFFFF=white) - required for 1.21.6+
            true
        );
    }

    private void addTitle(String title) {
        this.currentTitle = title;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Check if click is within preview panel
        if (click.button() == 0 && isMouseOverPreview(click.x(), click.y())) {
            isDraggingPreview = true;
            this.setDragging(true);  // Enable drag event handling
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && isDraggingPreview) {
            isDraggingPreview = false;
            this.setDragging(false);  // Disable drag event handling
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (isDraggingPreview && click.button() == 0) {
            // Update rotation based on drag delta (use provided deltaX/deltaY)
            previewYaw += (float)deltaX * 0.5f;  // Horizontal drag rotates left/right
            previewPitch += (float)deltaY * 0.5f;  // Vertical drag tilts up/down

            // Clamp pitch to prevent flipping
            previewPitch = Math.max(-45.0f, Math.min(45.0f, previewPitch));

            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOverPreview(mouseX, mouseY)) {
            // Update zoom based on scroll
            previewZoom += (float)verticalAmount * 0.1f;

            // Clamp zoom to reasonable range
            previewZoom = Math.max(0.5f, Math.min(2.0f, previewZoom));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private boolean isMouseOverPreview(double mouseX, double mouseY) {
        int panelWidth = PREVIEW_PANEL_WIDTH;
        int availableHeight = Math.max(140, this.height - PREVIEW_MARGIN * 2);
        int panelHeight = Math.min(PREVIEW_PANEL_HEIGHT, availableHeight);
        int left = Math.max(PREVIEW_MARGIN, this.width - panelWidth - PREVIEW_MARGIN);
        int top = (this.height - panelHeight) / 2;

        return mouseX >= left && mouseX <= left + panelWidth &&
               mouseY >= top && mouseY <= top + panelHeight;
    }

    @Override
    public void removed() {
        super.removed();
        previewEntity = null;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
