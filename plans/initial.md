# 5E SRD Fabric Mod - Initial Plan

## Project Overview
A Fabric mod implementing D&D 5e SRD rules with turn-based combat similar to Baldur's Gate 3 gameplay. Features modular mesh-based character rendering for different races.

---

## 1. Turn-Based Combat System

### 1.1 Initiative System
**Standard SRD Rules**: Initiative = d20 + DEX modifier

**Implementation Details**:
- `InitiativeManager` - Manages turn order for active encounter
  - Roll initiative for all participants (players, NPCs, mobs)
  - Sort by initiative value (descending), ties broken by DEX modifier
  - Track current turn index
  - Handle turn advancement

- Store initiative values per entity using data components or entity data
- Persist initiative across server ticks
- Support re-rolling initiative when new combatants join

**Data Structure**:
```java
class InitiativeTracker {
    UUID entityId;
    int initiativeRoll;
    int dexModifier;
    int getTotalInitiative();
}

class EncounterState {
    List<InitiativeTracker> turnOrder;
    int currentTurnIndex;
    UUID getCurrentEntity();
    void advanceTurn();
}
```

### 1.2 Encounter System

**Encounter Block**:
- Custom block `EncounterBlock` that triggers combat
- Detects players/entities within 10 block radius
- When triggered:
  - Destroys itself immediately
  - Initiates combat for all entities in range
  - Rolls initiative for all participants
  - Begins turn 1

**Implementation**:
- Use block entity with tick method to check for nearby entities
- Store encounter radius in block entity (configurable, default 10)
- Spawn associated mobs when triggered (configurable via NBT data)
- Mark all participants with "in_combat" status

### 1.3 Movement System

**Movement Sphere**:
- Each character has movement speed (typically 30 feet = 6 blocks in Minecraft scale)
- On their turn, character can move freely within sphere centered on turn start position
- Movement is NOT locked until:
  - They perform an Action or Bonus Action, OR
  - They click "End Turn" button

**Implementation Details**:
- `MovementTracker` component per entity:
  - `turnStartPos` - Position when turn began
  - `remainingMovement` - Remaining movement in blocks
  - `hasUsedAction` - Boolean flag
  - `hasUsedBonusAction` - Boolean flag

- Movement validation:
  - On player movement packet, check if in combat and on their turn
  - Calculate distance from `turnStartPos`
  - If distance > remainingMovement AND action used, cancel movement
  - Update position and remaining movement otherwise

- Movement types:
  - Normal movement: character's base speed (e.g., 30ft = 6 blocks)
  - Dash action: doubles movement for the turn
  - Difficult terrain: costs 2 movement per block (future enhancement)

**Movement Sphere Check**:
```java
boolean canMoveTo(Vec3d currentPos, Vec3d targetPos, MovementTracker tracker) {
    if (!tracker.hasUsedAction && !tracker.hasUsedBonusAction) {
        return true; // Can freely explore movement sphere
    }
    double distanceFromStart = tracker.turnStartPos.distanceTo(targetPos);
    return distanceFromStart <= tracker.remainingMovement;
}
```

### 1.4 Action Economy (Standard 5e)

**Per Turn Resources**:
- **Movement**: Based on character speed (default 30ft/6 blocks)
- **Action**: One standard action
- **Bonus Action**: One bonus action (if available)
- **Reaction**: One reaction (until start of next turn)
- **Free Object Interaction**: Not tracked for initial implementation

**Action Types**:

*Standard Actions*:
- Attack (melee/ranged)
- Cast a Spell (with casting time of 1 action)
- Dash (double movement for this turn)
- Disengage (movement doesn't provoke opportunity attacks)
- Dodge (attacks against you have disadvantage)
- Help (give ally advantage)
- Hide (Dexterity - Stealth check)
- Ready (prepare action with trigger)
- Search (Wisdom - Perception check)
- Use Object

*Bonus Actions*:
- Class-specific features
- Spells with casting time of 1 bonus action
- Two-weapon fighting off-hand attack
- Cunning Action (Rogue: Dash, Disengage, Hide)

*Reactions*:
- Opportunity Attack (when enemy leaves reach without Disengaging)
- Readied Action trigger
- Spell reactions (Shield, Counterspell, etc.)

**Implementation**:
- `ActionManager` per entity:
  - `hasAction: boolean`
  - `hasBonusAction: boolean`
  - `hasReaction: boolean`
  - `hasMovement: int` (remaining blocks)

- Reset at start of each entity's turn (except Reaction resets at start of their next turn in initiative)
- UI buttons grey out when resource is spent
- Network packets to notify server of action usage

### 1.5 Turn Flow

**Turn Structure**:
1. **Turn Start**:
   - Reset Action, Bonus Action, Movement
   - Trigger any "start of turn" effects (ongoing damage, regeneration, etc.)
   - Save current position as `turnStartPos`
   - Enable movement and action UI

2. **During Turn**:
   - Player can move freely within sphere (if movement available and no action taken)
   - Player selects actions from hotbar
   - Each action/bonus action locks movement distance
   - Player can "End Turn" at any time

3. **Turn End**:
   - Disable movement and actions for this entity
   - Trigger any "end of turn" effects
   - Advance to next entity in initiative order
   - If last entity, start new round

4. **Round End**:
   - Increment round counter
   - Check for encounter end conditions
   - Return to turn 1

**End Conditions**:
- All enemies defeated
- All players defeated/unconscious
- Players flee outside encounter range (e.g., 100 blocks)
- Manual combat end (DM command)

### 1.6 Multiplayer Support

**Server-Authoritative**:
- All combat state managed server-side
- Client sends action requests, server validates and executes
- Server broadcasts state updates to all clients

**Synchronization**:
- `EncounterState` synced to all players in encounter
- Current turn entity highlighted for all players
- Action availability synced per-player
- Initiative order visible to all participants

**Player Interactions**:
- Players can only control their own character(s) on their turn
- Players can see all combatants and their positions
- Turn timer optional (future enhancement)

### 1.7 Mob AI

**AI System**:
- `CombatAIController` for each mob type
- During mob's turn, AI evaluates actions:
  - Find nearest hostile target
  - Calculate optimal position (movement)
  - Choose action (attack, spell, special ability)
  - Execute action
  - End turn

**Basic AI Behavior**:
- Melee mobs: Move toward nearest enemy, attack if in range
- Ranged mobs: Maintain distance, attack with ranged weapon
- Spellcasters: Choose spell based on situation (damage, buff, debuff)
- Support: Heal allies, provide buffs

**AI Decision Tree** (simple initial version):
```
1. If enemy in melee range -> Attack
2. Else if can reach enemy with movement -> Move and Attack
3. Else if has ranged attack -> Ranged Attack
4. Else -> Dash toward nearest enemy
```

---

## 2. Modular Rendering System

### 2.1 Architecture Overview

**Design Philosophy**:
- Load GLTF file once containing all mesh layers
- Cache all mesh parts in memory
- At runtime, select only the layers needed for each character
- Similar to GoldGolem example but with layer filtering

**Character Composition**:
- Each character defined by a set of mesh indices
- Example: `{ body: 0, legs: 1, arms: 0, head: 2 }`
- Mesh layers named with index: `body_0`, `legs_1`, `arms_0`, `head_2`

### 2.2 GLTF Model Structure

**Single GLTF File**: `assets/srd/models/entity/character_parts.gltf`

**Layer Naming Convention**:
```
body_0    (human body)
body_1    (dwarf body - shorter, stockier)
legs_0    (human legs)
legs_1    (dwarf legs - shorter)
arms_0    (human arms)
arms_1    (dwarf arms)
head_0    (human head variant 1)
head_1    (human head variant 2)
head_2    (dwarf head variant 1)
head_3    (dwarf head variant 2)
```

**Mesh Requirements**:
- All parts use consistent pivot points for alignment
- Each part is a separate node in GLTF hierarchy
- All parts share same texture atlas
- Pivot points aligned for proper animation

### 2.3 Model Loader System

**`CharacterModelLoader`** (based on GoldGolemModelLoader):
```java
class CharacterModelLoader implements SimpleSynchronousResourceReloadListener {
    private static volatile Map<String, MeshPart> meshParts = Collections.emptyMap();

    // Load all mesh parts from GLTF
    void reload(ResourceManager manager) {
        meshParts = loadMeshParts(manager);
    }

    // Returns map: "body_0" -> MeshPart, "legs_1" -> MeshPart, etc.
    static Map<String, MeshPart> getMeshParts() {
        return meshParts;
    }

    // Get specific mesh by name
    static MeshPart getMesh(String name) {
        return meshParts.get(name);
    }
}
```

**Loading Process**:
1. Load GLTF file using Assimp (same as GoldGolem example)
2. Traverse all nodes in scene hierarchy
3. For each mesh node, extract name and bake geometry
4. Store in map with name as key
5. Cache indefinitely (reload only on resource reload)

**Memory Considerations**:
- All parts loaded into memory (~50-100 mesh parts total)
- Each MeshPart is small (a few KB per part)
- Total memory footprint: ~5-10 MB for all parts
- Acceptable for up to 50 visible characters

### 2.4 Character Definition System

**`CharacterAppearance`** component:
```java
class CharacterAppearance {
    int bodyIndex;    // e.g., 0 for human, 1 for dwarf
    int legsIndex;
    int armsIndex;
    int headIndex;

    // Resolve to mesh names
    String getBodyMesh() { return "body_" + bodyIndex; }
    String getLegsMesh() { return "legs_" + legsIndex; }
    String getArmsMesh() { return "arms_" + armsIndex; }
    String getHeadMesh() { return "head_" + headIndex; }

    // Get all required meshes for rendering
    List<String> getAllMeshNames() {
        return List.of(getBodyMesh(), getLegsMesh(), getArmsMesh(), getHeadMesh());
    }
}
```

**Storage**:
- Store as entity data component (NBT serializable)
- Sync to client for rendering
- Set during character creation
- Persists with entity save data

### 2.5 Race Definitions

**Initial Races**: Human, Dwarf

**Race Data Structure**:
```java
enum Race {
    HUMAN(
        "human",
        6,  // movement speed in blocks (30 feet)
        0,  // default body index
        0,  // default legs index
        0,  // default arms index
        0   // default head index (can randomize)
    ),
    DWARF(
        "dwarf",
        5,  // movement speed in blocks (25 feet - slow speed)
        1,  // dwarf body
        1,  // dwarf legs
        1,  // dwarf arms
        2   // dwarf head (can randomize between 2-3)
    );

    final String name;
    final int baseMovementSpeed;
    final int defaultBodyIndex;
    final int defaultLegsIndex;
    final int defaultArmsIndex;
    final int defaultHeadIndex;
}
```

**Race-Specific Traits** (SRD):
- **Human**:
  - +1 to all ability scores
  - 30ft movement
  - Size: Medium

- **Dwarf**:
  - +2 CON
  - 25ft movement (speed not reduced by heavy armor)
  - Darkvision 60ft
  - Dwarven Resilience (advantage on saves vs poison)
  - Size: Medium

### 2.6 Character Renderer

**`CharacterEntityRenderer`** (based on GoldGolemEntityRenderer):
```java
class CharacterEntityRenderer extends EntityRenderer<CharacterEntity, CharacterRenderState> {

    void render(CharacterRenderState state, MatrixStack matrices, ...) {
        CharacterAppearance appearance = state.appearance;

        // Get required mesh parts
        MeshPart body = CharacterModelLoader.getMesh(appearance.getBodyMesh());
        MeshPart legs = CharacterModelLoader.getMesh(appearance.getLegsMesh());
        MeshPart arms = CharacterModelLoader.getMesh(appearance.getArmsMesh());
        MeshPart head = CharacterModelLoader.getMesh(appearance.getHeadMesh());

        matrices.push();

        // Render body (static)
        renderMesh(matrices, queue, layer, body, overlay, light);

        // Render legs with walk animation (rotate based on limbAngle)
        renderAnimatedLegs(matrices, queue, layer, legs, state.limbAngle, state.limbDistance);

        // Render arms with swing animation
        renderAnimatedArms(matrices, queue, layer, arms, state.handSwingProgress);

        // Render head with look direction
        renderAnimatedHead(matrices, queue, layer, head, state.yaw, state.pitch);

        matrices.pop();
    }

    // Similar to player model animation
    void renderAnimatedLegs(MatrixStack matrices, ..., MeshPart legs, float limbAngle, float limbDistance) {
        // Rotate legs back and forth based on walking
        float legRotation = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;

        matrices.push();
        matrices.translate(legs.pivotX(), legs.pivotY(), legs.pivotZ());
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(legRotation));
        matrices.translate(-legs.pivotX(), -legs.pivotY(), -legs.pivotZ());
        renderMesh(matrices, queue, layer, legs, overlay, light);
        matrices.pop();
    }
}
```

**Animation System**:
- **Legs**: Rotate like player legs during walking (swinging motion)
- **Arms**: Swing during walking, rotate during attacks
- **Head**: Rotate to look at target during combat
- **Body**: Static, no rotation

**Render State**:
```java
class CharacterRenderState extends EntityRenderState {
    CharacterAppearance appearance;  // Which mesh parts to use
    float limbAngle;                  // Walking animation
    float limbDistance;
    float handSwingProgress;          // Attack animation
    float yaw;                        // Head rotation
    float pitch;
}
```

### 2.7 Performance Optimization

**Caching Strategy**:
- Load GLTF once at game start / resource reload
- All mesh parts kept in memory as baked vertex data
- No per-frame loading or mesh generation
- Rendering only submits cached vertices

**Culling**:
- Frustum culling handled by Minecraft's entity renderer
- Only render entities on screen
- Distance-based LOD not needed initially (meshes are low-poly)

**Expected Performance**:
- 50 characters visible: ~50 * 4 parts = 200 mesh parts rendered
- Each mesh: ~500-1000 triangles
- Total: ~100K-200K triangles per frame
- Well within modern GPU capabilities (targeting 60+ FPS)

---

## 3. UI System

### 3.1 Action Hotbar

**Design**:
- Second hotbar rendered above vanilla hotbar
- Expandable height (1-3 rows)
- Displays all available actions, spells, abilities
- Context-sensitive based on class/equipped items

**Layout**:
```
┌─────────────────────────────────────────┐
│  [Attack] [Dash] [Disengage] [Hide]     │  <- Actions row
│  [Spell1] [Spell2] [Spell3] [...]       │  <- Spells row
│  [Ability1] [Ability2] [...]            │  <- Class abilities row
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│  [ 1 ]  [ 2 ]  [ 3 ]  ...  [ 9 ]        │  <- Vanilla hotbar
└─────────────────────────────────────────┘
```

**Implementation**:
- `ActionHotbarScreen` overlay (rendered via HUD render event)
- Button grid layout, each button represents an action
- Buttons show:
  - Icon (custom texture or item icon)
  - Name
  - Resource cost (action/bonus action/reaction)
  - Keybind (1-9, Shift+1-9, etc.)

**Button States**:
- **Available**: Full color, clickable
- **Unavailable**: Greyed out (resource spent, not your turn)
- **Not Applicable**: Hidden or dark (not usable in current context)
- **Hovered**: Tooltip with description

**Interaction**:
- Click to use action
- Sends packet to server
- Server validates and executes
- Client updates UI state

### 3.2 Character Creation GUI

**Simple Text Modal**:
- `CharacterCreationScreen` - Full screen GUI
- Text-based selection interface
- Appears when player first joins or uses creation item

**Flow**:
```
1. Select Race
   > Human
   > Dwarf

2. Select Class (future - start with Fighter)
   > Fighter

3. Roll Ability Scores (or use standard array)
   STR: [15]  DEX: [14]  CON: [13]
   INT: [12]  WIS: [10]  CHA: [8]
   [Reroll] [Accept]

4. Customize Appearance
   Head: [< Variant 0 >]  (cycle through head indices)
   [Random]

5. [Create Character]
```

**Implementation**:
- Extend `Screen` class
- Use `Text` widgets for display
- `ButtonWidget` for navigation
- Store selections in temp data structure
- On confirm, send packet to server to create character entity
- Server validates and sets entity data components

**Data Sent to Server**:
```java
class CharacterCreationData {
    Race race;
    Class characterClass;
    int[] abilityScores; // STR, DEX, CON, INT, WIS, CHA
    int headVariant;     // Selected head index
}
```

### 3.3 Turn Indicator UI

**Turn Display**:
- Show current turn in top-left corner of screen
- Display initiative order
- Highlight current entity

**Design**:
```
┌─────────────────────┐
│ TURN 3 - Round 2    │
│                     │
│ Initiative:         │
│ ► Goblin (18)       │  <- Current turn (highlighted)
│   Player 1 (15)     │
│   Player 2 (12)     │
│   Orc (8)           │
└─────────────────────┘
```

**Implementation**:
- `CombatHudOverlay` rendered via HUD event
- Receives initiative order from server (synced packet)
- Updates each turn
- Shows entity name, initiative value
- Arrow or highlight for current turn

**Additional Indicators**:
- Colored outline/glow on entity in world (current turn entity)
- Particle effect above current entity's head
- Name tag shows "[TURN]" prefix

### 3.4 End Turn Button

**Design**:
- Button in bottom-right corner (or near action hotbar)
- Only visible during player's turn
- Click to end turn immediately

**States**:
- **Visible & Active**: Player's turn, can end turn
- **Hidden**: Not player's turn
- **Grayed Out**: Player hasn't acted yet (optional warning)

**Implementation**:
```java
if (isPlayerTurn && !combatEnded) {
    renderEndTurnButton(matrices, mouseX, mouseY);
}

void onEndTurnClick() {
    ClientPlayNetworking.send(new EndTurnPacket());
}
```

**Confirmation**:
- Optional: Show confirmation if player has unused actions
- "You have unused action(s). End turn anyway?"

---

## 4. Core Data Structures

### 4.1 Entity Components (using data components)

**CharacterStats**:
```java
record CharacterStats(
    int strength,
    int dexterity,
    int constitution,
    int intelligence,
    int wisdom,
    int charisma
) {
    int getModifier(int score) {
        return (score - 10) / 2;
    }
}
```

**CombatState**:
```java
class CombatState {
    boolean inCombat;
    int initiative;
    Vec3d turnStartPosition;
    int remainingMovement;
    boolean hasAction;
    boolean hasBonusAction;
    boolean hasReaction;
    int armorClass;
    int currentHitPoints;
    int maxHitPoints;
}
```

**CharacterSheet** (full 5e data):
```java
class CharacterSheet {
    Race race;
    CharacterClass characterClass;
    int level;
    int proficiencyBonus;
    CharacterStats abilityScores;
    List<String> proficiencies;
    List<Spell> knownSpells;
    List<Feature> classFeatures;
    Equipment equipment;
}
```

### 4.2 Networking Packets

**Server -> Client**:
- `SyncEncounterStatePacket` - Initiative order, current turn
- `SyncCombatStatePacket` - Entity combat state updates
- `TurnStartPacket` - Notify turn start for entity
- `TurnEndPacket` - Notify turn end

**Client -> Server**:
- `UseActionPacket(ActionType, target)` - Request action use
- `EndTurnPacket` - Request end turn
- `CreateCharacterPacket(CharacterCreationData)` - Create character

### 4.3 Encounter Management

**EncounterManager** (singleton, server-side):
```java
class EncounterManager {
    Map<UUID, EncounterState> activeEncounters;

    UUID startEncounter(List<Entity> participants);
    void addCombatant(UUID encounterId, Entity entity);
    void advanceTurn(UUID encounterId);
    void endEncounter(UUID encounterId);
    EncounterState getEncounter(UUID encounterId);
}
```

**EncounterState**:
```java
class EncounterState {
    UUID encounterId;
    List<InitiativeTracker> turnOrder;
    int currentTurnIndex;
    int roundNumber;
    Vec3d centerPosition;

    UUID getCurrentTurnEntity();
    void nextTurn();
    boolean isEnded();
}
```

---

## 5. Implementation Phases

### Phase 1: Core Systems (Foundation)
1. Character entity with data components
2. Character model loader (load GLTF, cache mesh parts)
3. Character renderer (render selected mesh parts)
4. Basic race definitions (Human, Dwarf)
5. Character creation GUI
6. Ability score system

### Phase 2: Turn-Based Combat (Core Gameplay)
1. Encounter block and encounter triggering
2. Initiative system and turn order
3. Movement tracking and validation
4. Action economy system (Action, Bonus Action, Reaction)
5. Turn advancement and round tracking
6. End turn functionality

### Phase 3: UI & Multiplayer
1. Action hotbar UI
2. Turn indicator UI
3. Combat HUD overlay
4. Network packet sync for combat state
5. Multiplayer turn management
6. Client-side validation and feedback

### Phase 4: Combat Actions & AI
1. Basic attack action (melee/ranged)
2. Movement actions (Dash, Disengage)
3. Opportunity attacks (reactions)
4. Basic mob AI for turns
5. Damage calculation (dice rolling)
6. Death and unconsciousness

### Phase 5: Polish & Expansion
1. More actions (Dodge, Help, Hide, etc.)
2. Spell system (basic spells)
3. Class features
4. Additional races
5. Animations and visual effects
6. Sound effects

---

## 6. Technical Considerations

### 6.1 Minecraft Scale Conversion
- **5e Distance**: 1 square = 5 feet
- **Minecraft**: 1 block = 1 meter ≈ 3.28 feet
- **Conversion**: 5 feet ≈ 1.52 meters ≈ 1.5 blocks
- **Simplified**: 1 square = 2 blocks (easier math, close enough)

**Example Movement**:
- 30 feet = 6 squares = **6 blocks** in Minecraft
- 25 feet (Dwarf) = 5 squares = **5 blocks**

### 6.2 Dice Rolling System
```java
class DiceRoller {
    Random random;

    int roll(int sides) {
        return random.nextInt(sides) + 1;  // 1 to sides
    }

    int rollMultiple(int count, int sides) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += roll(sides);
        }
        return total;
    }

    // Advantage: roll twice, take higher
    int rollWithAdvantage(int sides) {
        return Math.max(roll(sides), roll(sides));
    }

    // Disadvantage: roll twice, take lower
    int rollWithDisadvantage(int sides) {
        return Math.min(roll(sides), roll(sides));
    }
}
```

### 6.3 Server Tick Management
- Minecraft runs at 20 TPS (ticks per second)
- Combat state updates happen server-side
- Use tick method to check for:
  - Encounter triggers
  - Turn timeouts (optional)
  - Ongoing effects (poison, burning, etc.)

### 6.4 Save Data Persistence
- Character data saved with entity NBT
- Encounter state stored in world saved data
- Handle player disconnect during combat gracefully:
  - Skip their turn if offline
  - Remove from encounter if offline > 30 seconds
  - Or pause encounter (optional)

---

## 7. Asset Requirements

### 7.1 Models
- `character_parts.gltf` - All body parts (body, legs, arms, heads)
  - Human variants (body_0, legs_0, arms_0, head_0-1)
  - Dwarf variants (body_1, legs_1, arms_1, head_2-3)

### 7.2 Textures
- `character_atlas.png` - Texture atlas for all character parts
- `action_icons.png` - Icons for action buttons (attack, dash, etc.)
- `ui_elements.png` - UI decorations, borders

### 7.3 Configuration Files
- `races.json` - Race definitions with stats
- `classes.json` - Class definitions (future)
- `spells.json` - Spell data (future)

---

## 8. Testing Strategy

### 8.1 Unit Tests
- Dice rolling (verify distribution)
- Initiative calculation
- Movement validation
- Action economy state machine

### 8.2 Integration Tests
- Encounter triggering
- Turn advancement
- Multiplayer synchronization
- Character creation flow

### 8.3 Manual Testing Scenarios
1. **Single Player Combat**:
   - Trigger encounter
   - Verify turn order
   - Test movement sphere
   - Use actions and verify resource depletion
   - End turn, verify AI takes turn

2. **Multiplayer Combat**:
   - Two players trigger same encounter
   - Verify both see same initiative order
   - Verify turn restrictions (can't act on other's turn)
   - Test combat with 10+ entities

3. **Edge Cases**:
   - Player leaves during their turn
   - Encounter block destroyed manually
   - Entity dies mid-combat
   - Multiple encounters simultaneously

---

## 9. Known Limitations & Future Work

### Current Scope Limitations:
- No equipment rendering (armor/weapons visual)
- No spell effects or particles
- No character leveling system
- Only 2 races (Human, Dwarf)
- Basic AI (no advanced tactics)
- No stealth or hiding mechanics
- No environment interaction (cover, difficult terrain)

### Future Enhancements:
- Full SRD race roster (Elf, Halfling, Dragonborn, etc.)
- All SRD classes with subclasses
- Complete spell list with visual effects
- Advanced AI with tactical positioning
- Environmental hazards and terrain effects
- Character progression and leveling
- Equipment system with stat bonuses
- Party management and formations
- DM mode for manual control

---

## 10. References

### D&D 5e SRD Resources:
- [5e SRD Official](https://dnd.wizards.com/resources/systems-reference-document)
- Combat rules: Chapter 9
- Ability scores: Chapter 7
- Races: Chapter 2
- Classes: Chapter 3

### Technical References:
- Fabric API documentation
- Minecraft 1.21.x entity rendering
- LWJGL Assimp bindings
- JOML matrix operations

---

## Summary

This plan outlines a comprehensive implementation of D&D 5e rules in Minecraft using the Fabric mod loader. The core systems are:

1. **Turn-Based Combat**: Standard SRD initiative, action economy, and movement
2. **Modular Rendering**: Efficient GLTF-based character rendering with indexed mesh selection
3. **UI System**: Action hotbar, character creation, and turn indicators

The implementation prioritizes multiplayer compatibility, server-authoritative gameplay, and efficient rendering for large numbers of characters. The modular design allows for incremental implementation and future expansion.
