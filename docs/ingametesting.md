# 5E SRD Mod - In-Game Testing Guide

## Prerequisites

Before testing, ensure:
- Mod is built and loaded in Minecraft 1.21.10 with Fabric
- Creative mode or appropriate permissions for testing
- Access to the "5E SRD" creative tab

## Test Categories

---

## 1. Character Creation System

### Test 1.1: Opening Character Creation GUI
**Steps:**
1. Open creative inventory
2. Navigate to "5E SRD" tab
3. Find and obtain "Character Creation Tome" item
4. Right-click the item

**Expected Behavior:**
- Character creation screen should open
- Screen should show "Race Selection" as first step
- Available races: Human, Dwarf, Elf, Halfling should be visible
[all ok]

### Test 1.2: Race Selection
**Steps:**
1. Open character creation GUI
2. Select each race (Human, Dwarf, Elf, Halfling)
3. Note the racial bonuses displayed

**Expected Behavior:**
- **Human**: +1 to all ability scores, 30ft movement
- **Dwarf**: +2 CON, Darkvision, 25ft movement
- **Elf**: +2 DEX, Darkvision, 30ft movement
- **Halfling**: +2 DEX, 25ft movement
[all ok]

### Test 1.3: Class Selection
**Steps:**
1. Proceed from race selection to class selection
2. Select Fighter class

**Expected Behavior:**
- Fighter should be available
- Hit die: d10
- Shows starting proficiency bonus (+2 at level 1)
[all ok]

### Test 1.4: Ability Score Assignment
**Steps:**
1. Proceed to ability scores step
2. Assign standard array values (15, 14, 13, 12, 10, 8) to the six abilities
3. Verify racial bonuses are applied

**Expected Behavior:**
- Can assign each value from standard array once
- Final scores show base + racial bonus
- Cannot proceed until all scores assigned
[all ok]

### Test 1.5: Appearance Customization
**Steps:**
1. Proceed to appearance step
2. Adjust mesh indices for: Body, Legs, Arms, Head
3. Test different combinations

**Expected Behavior:**
- Sliders or selection for each body part
- Each race has different mesh index ranges
- Preview updates (if implemented)
[all ok]

### Test 1.6: Name and Final Creation
**Steps:**
1. Enter character name
2. Review final stats
3. Click "Create Character" button

**Expected Behavior:**
- Character entity spawns near player
- Entity has the configured appearance
- Entity name displays above head
- GUI closes automatically
[all ok]
---

## 2. Combat Encounter System

### Test 2.1: Placing Encounter Block
**Steps:**
1. Obtain "Encounter Block" from creative inventory
2. Place block in world
3. Move away and approach the block

**Expected Behavior:**
- Block can be placed
- Has distinctive appearance
- Block triggers when player enters proximity (5 block radius)

### Test 2.2: Encounter Initialization
**Steps:**
1. Create 2-3 character entities
2. Place encounter block near them
3. Walk into trigger range

**Expected Behavior:**
- Encounter block disappears after triggering
- Initiative rolled for all nearby entities (player + NPCs)
- Turn order established based on initiative rolls (with DEX tiebreaker)
- Combat HUD appears on screen

### Test 2.3: Initiative Order
**Steps:**
1. Trigger encounter with multiple entities
2. Check turn tracker UI

**Expected Behavior:**
- Turn tracker shows all combatants in order
- Highest initiative goes first
- DEX modifier used as tiebreaker if initiatives equal
- Current turn highlighted

### Test 2.4: Turn Start Notification
**Steps:**
1. During active encounter, observe turn start

**Expected Behavior:**
- Turn banner animation appears
- Shows whose turn it is
- Action hotbar highlights available actions
- Player can act if it's their turn
- NPC acts automatically if it's their turn

---

## 3. Action Hotbar & UI

### Test 3.1: Action Hotbar Display
**Steps:**
1. Enter combat encounter
2. Observe HUD above normal hotbar

**Expected Behavior:**
- Second row displays above vanilla hotbar
- Shows: Attack, Dash, Disengage, Dodge, End Turn buttons
- Turn/round indicators visible
- Actions show availability (available/used)

### Test 3.2: Action Availability Indicators
**Steps:**
1. During your turn, check action states
2. Use actions and observe updates

**Expected Behavior:**
- Available actions highlighted/enabled
- Used actions grayed out/disabled
- Bonus actions shown separately if applicable
- Movement remaining displayed

### Test 3.3: Turn Tracker Panel
**Steps:**
1. During encounter, locate turn tracker
2. Observe as turns advance

**Expected Behavior:**
- Lists all combatants
- Shows initiative values
- Current turn clearly indicated
- Updates automatically when turns advance

### Test 3.4: End Turn Button
**Steps:**
1. During your turn, click "End Turn" button
2. Try ending turn with remaining actions
3. Try hotkey for ending turn

**Expected Behavior:**
- Turn ends when clicked
- Tooltip shows if actions remain
- Advances to next combatant's turn
- Hotkey works as alternative input

---

## 4. Combat Actions

### Test 4.1: Attack Action
**Steps:**
1. During your turn, select Attack action
2. Target an enemy entity
3. Observe attack resolution

**Expected Behavior:**
- d20 rolled + ability modifier + proficiency bonus
- Attack roll compared to target AC
- On hit: weapon damage dice rolled + ability modifier
- Damage applied to target HP
- Chat/combat log shows: roll results, hit/miss, damage dealt
- Natural 1 = automatic miss
- Natural 20 = critical hit (double damage dice)

### Test 4.2: Dash Action
**Steps:**
1. Note current movement remaining
2. Use Dash action
3. Observe movement pool

**Expected Behavior:**
- Movement speed doubles for the turn
- Can move further than normal
- Consumes action (cannot also attack this turn)
- Status clears at turn end

### Test 4.3: Disengage Action
**Steps:**
1. Start turn in melee range of enemy
2. Use Disengage action
3. Move away from enemy

**Expected Behavior:**
- Consumes action
- Can move away without triggering opportunity attacks
- Status clears at start of next turn

### Test 4.4: Dodge Action
**Steps:**
1. Use Dodge action
2. Allow enemy to attack you
3. Observe attack rolls

**Expected Behavior:**
- Consumes action
- Enemy attacks have disadvantage (roll twice, take lower)
- Status lasts until start of next turn
- Visual indicator shows dodging status

### Test 4.5: Opportunity Attacks
**Steps:**
1. Create encounter with melee NPCs
2. Start turn in melee range (within 1 block)
3. Move away without using Disengage

**Expected Behavior:**
- Enemy gets automatic opportunity attack
- Consumes enemy's reaction
- Attack resolved normally
- Only one opportunity attack per movement
- Reach weapons (if equipped) trigger from 2 blocks away

---

## 5. Damage & Death System

### Test 5.1: Taking Damage
**Steps:**
1. Create character with known HP
2. Take damage from attacks
3. Monitor HP reduction

**Expected Behavior:**
- HP decreases by damage amount
- HP displayed in UI/overhead
- Cannot go below 0

### Test 5.2: Reaching 0 HP
**Steps:**
1. Reduce character HP to exactly 0

**Expected Behavior:**
- Entity becomes unconscious
- Falls prone (if animation exists)
- Cannot take actions
- Death saving throws begin on their turn

### Test 5.3: Death Saving Throws
**Steps:**
1. Be unconscious at 0 HP
2. Wait for character's turn
3. Observe automatic death save roll

**Expected Behavior:**
- d20 rolled automatically each turn
- 10+ = success (mark one success)
- 9 or less = failure (mark one failure)
- Natural 1 = 2 failures
- Natural 20 = regain 1 HP and wake up
- 3 successes = stabilized (unconscious but stable)
- 3 failures = character dies

### Test 5.4: Massive Damage
**Steps:**
1. Create low-HP character
2. Take damage >= max HP in single hit

**Expected Behavior:**
- Character dies instantly
- No death saving throws
- Entity marked as dead

### Test 5.5: Attacking Unconscious Target
**Steps:**
1. Reduce enemy to 0 HP
2. Attack unconscious enemy

**Expected Behavior:**
- Hit adds failed death save
- Critical hit adds 2 failed death saves
- Accumulating 3 failures kills target

### Test 5.6: Healing from 0 HP
**Steps:**
1. Be unconscious with death saves
2. Receive healing (spell or potion)

**Expected Behavior:**
- HP restored by heal amount
- Death saves cleared
- Character wakes up
- Can act normally on next turn

---

## 6. AI Combat Behavior

### Test 6.1: Basic AI Turn Execution
**Steps:**
1. Create encounter with NPC characters
2. Observe NPC behavior on their turns

**Expected Behavior:**
- NPCs automatically take actions
- Uses attack action if in range
- Targets player or other enemies
- Ends turn automatically

### Test 6.2: AI Target Prioritization
**Steps:**
1. Create encounter with multiple player-side characters
2. Have some at low HP, some at high HP
3. Observe AI targeting

**Expected Behavior:**
- AI prioritizes low-HP targets when possible
- Considers proximity (prefers closer targets)
- Makes reasonable tactical decisions

### Test 6.3: AI Tactical Actions - Dash
**Steps:**
1. Create encounter with ranged enemy
2. Position AI too far to attack

**Expected Behavior:**
- AI uses Dash action to close distance
- Moves toward enemy
- Prioritizes getting in attack range

### Test 6.4: AI Tactical Actions - Disengage
**Steps:**
1. Create melee NPC at low HP
2. Position enemy in melee range

**Expected Behavior:**
- Low-HP NPCs use Disengage to retreat
- Moves away from threats
- Avoids opportunity attacks

### Test 6.5: AI Tactical Actions - Dodge
**Steps:**
1. Create NPC at low HP
2. Observe defensive behavior

**Expected Behavior:**
- May use Dodge action when low HP
- Especially if cannot escape or attack effectively
- Provides defensive benefit

---

## 7. Weapon System

### Test 7.1: Melee Weapon Attacks
**Steps:**
1. Equip character with melee weapon (longsword, greatsword, etc.)
2. Attack enemy in melee range
3. Verify damage calculation

**Expected Behavior:**
- Attack roll uses STR modifier (or DEX if finesse)
- Damage dice match weapon (d8 for longsword, 2d6 for greatsword)
- Damage modifier uses STR (or DEX if finesse, whichever is higher)
- Two-handed weapons cannot use shield

### Test 7.2: Finesse Weapons
**Steps:**
1. Equip finesse weapon (rapier, dagger)
2. Compare attack with high STR vs high DEX character

**Expected Behavior:**
- Automatically uses higher of STR or DEX
- Both attack rolls and damage use chosen modifier
- Player sees which modifier was used in combat log

### Test 7.3: Versatile Weapons
**Steps:**
1. Equip versatile weapon (longsword, battleaxe)
2. Use one-handed (with shield)
3. Use two-handed (without shield)

**Expected Behavior:**
- One-handed: lower damage die (d8 for longsword)
- Two-handed: higher damage die (d10 for longsword)
- System automatically determines based on shield equipped

### Test 7.4: Reach Weapons
**Steps:**
1. Equip reach weapon (pike, glaive)
2. Attack from 2 blocks away
3. Test opportunity attack range

**Expected Behavior:**
- Can attack targets up to 2 blocks away
- Opportunity attacks trigger when targets leave 2-block range
- Standard weapons only work at 1 block

### Test 7.5: Ranged Weapons
**Steps:**
1. Equip ranged weapon (longbow, crossbow)
2. Attack from distance
3. Test range limits

**Expected Behavior:**
- Uses DEX modifier for attacks and damage
- Effective within normal range
- Disadvantage beyond normal range (if implemented)
- Cannot make opportunity attacks with ranged weapons

### Test 7.6: Damage Types
**Steps:**
1. Test different weapons with different damage types
2. Observe combat log

**Expected Behavior:**
- Slashing (swords, axes)
- Piercing (spears, bows, daggers)
- Bludgeoning (maces, clubs, hammers)
- Damage type displayed in combat messages
- (Future: damage resistance/immunity checks)

---

## 8. Movement During Combat

### Test 8.1: Movement Restriction on Other's Turn
**Steps:**
1. Enter combat encounter
2. Try to move during another combatant's turn

**Expected Behavior:**
- Movement locked when not your turn
- Character cannot move (or moves with extreme slowness)
- Can move freely on your turn

### Test 8.2: Movement Speed Tracking
**Steps:**
1. On your turn, move character
2. Observe remaining movement

**Expected Behavior:**
- Movement pool equals base speed (30ft for Human/Elf, 25ft for Dwarf/Halfling)
- 1 block = 5 feet
- Remaining movement decreases as you move
- Movement refreshes at start of turn
- UI shows remaining movement

### Test 8.3: Difficult Terrain (if implemented)
**Steps:**
1. Move through potentially difficult terrain during combat

**Expected Behavior:**
- If implemented: costs 2 feet per 1 foot of movement
- If not yet implemented: moves normally

---

## 9. Equipment System

### Test 9.1: Equipping Armor
**Steps:**
1. Create character
2. Equip different armor types from creative inventory

**Expected Behavior:**
- Armor items available in creative tab
- Can equip light, medium, heavy armor
- AC calculated: Base AC + DEX modifier (with limits)
  - Light armor: 11-12 + full DEX
  - Medium armor: 12-15 + DEX (max +2)
  - Heavy armor: 14-18 + no DEX
- AC displayed in character sheet/UI

### Test 9.2: Equipping Shields
**Steps:**
1. Equip shield
2. Observe AC bonus

**Expected Behavior:**
- Adds +2 to AC
- Cannot use two-handed weapons with shield
- Prevents using versatile weapons two-handed

### Test 9.3: Equipping Weapons
**Steps:**
1. Equip different weapons from creative inventory
2. Verify stats in tooltip

**Expected Behavior:**
- Weapon items show damage dice
- Show properties (finesse, light, heavy, reach, versatile, etc.)
- Show weapon type (simple/martial, melee/ranged)
- Attacks use equipped weapon's stats

---

## 10. Spell System (Basic)

### Test 10.1: Spell Access (if UI exists)
**Steps:**
1. Create spellcaster character (if wizard/cleric implemented)
2. Access spell list

**Expected Behavior:**
- Can view known spells
- Shows spell level, school, components
- Shows casting time (action, bonus action, etc.)

### Test 10.2: Cantrip Casting
**Steps:**
1. Cast Fire Bolt or Shocking Grasp (cantrips)

**Expected Behavior:**
- No spell slot consumed
- Can cast repeatedly
- Fire Bolt: ranged spell attack, fire damage
- Shocking Grasp: melee spell attack, lightning damage, prevents reactions

### Test 10.3: Leveled Spell Casting
**Steps:**
1. Cast level 1+ spells (Magic Missile, Cure Wounds, Shield, Scorching Ray)

**Expected Behavior:**
- Consumes spell slot of appropriate level
- Magic Missile: automatic hit, force damage
- Cure Wounds: restores HP to target
- Shield: +5 AC until next turn (reaction)
- Scorching Ray: multiple fire ray attacks

### Test 10.4: Upcasting
**Steps:**
1. Cast lower-level spell using higher slot

**Expected Behavior:**
- Additional effects based on spell
- Magic Missile: +1 dart per slot level above 1st
- Scorching Ray: +1 ray per slot level above 2nd
- Cure Wounds: +1d8 healing per slot level above 1st

---

## 11. Multiplayer Functionality

### Test 11.1: Multiple Players in Combat
**Steps:**
1. Have 2+ players join server
2. Trigger encounter with both players in range

**Expected Behavior:**
- Both players added to initiative order
- Each player can act on their turn
- Other players' turns are visible in turn tracker
- Combat state synchronized across clients

### Test 11.2: Late Joiner Synchronization
**Steps:**
1. Start encounter with player 1
2. Have player 2 join server mid-combat

**Expected Behavior:**
- Player 2 receives current encounter state
- Sees correct turn order
- Sees correct HP/status of all combatants
- Can participate if added to encounter

### Test 11.3: Combat Broadcasts
**Steps:**
1. Multiple players in same encounter
2. Have one player take actions

**Expected Behavior:**
- All players see action results
- Attack rolls visible to all participants
- Damage numbers visible to all
- Turn changes visible to all

---

## 12. Rendering & Animation (Phase 5)

### Test 12.1: Character Model Rendering
**Steps:**
1. Create character with custom appearance
2. Observe entity in world

**Expected Behavior:**
- If GLTF assets exist: modular mesh parts render correctly
- Different body/leg/arm/head parts visible
- Textures applied from character atlas
- If assets don't exist: default cube/model rendering

### Test 12.2: Walk Animation
**Steps:**
1. Command or AI-control character to walk
2. Observe movement

**Expected Behavior:**
- Legs rotate/animate during movement
- Arms swing
- Animation speed matches movement speed
- Smooth transitions

### Test 12.3: Attack Animation
**Steps:**
1. Have character perform attack action
2. Observe animation

**Expected Behavior:**
- Arm swing during attack
- Animation plays when attack executed
- Returns to idle after attack completes

### Test 12.4: Look Direction
**Steps:**
1. Rotate character or have AI look at targets
2. Observe head rotation

**Expected Behavior:**
- Head rotates toward look direction
- Smooth interpolation
- Body may rotate to follow

---

## 13. Persistence & Data

### Test 13.1: Character Data Saving
**Steps:**
1. Create character
2. Save and quit world
3. Reload world

**Expected Behavior:**
- Character entity persists
- Stats, appearance, name retained
- HP and status retained
- Equipment retained

### Test 13.2: Combat State Persistence
**Steps:**
1. Start combat encounter
2. Save and quit mid-combat
3. Reload world

**Expected Behavior:**
- Encounter state may reset (expected)
- Character HP/status retained
- OR: Encounter continues from saved state (if implemented)

---

## 14. Edge Cases & Stress Tests

### Test 14.1: Large Encounters
**Steps:**
1. Create encounter with 10+ combatants
2. Observe performance and functionality

**Expected Behavior:**
- Turn order correctly managed
- No crashes or lag spikes
- UI remains readable
- All combatants take turns properly

### Test 14.2: Simultaneous Initiative Ties
**Steps:**
1. Create characters with identical DEX modifiers
2. Trigger encounter, reroll if needed to force initiative ties

**Expected Behavior:**
- DEX tiebreaker applied
- Higher DEX goes first
- If DEX also tied: consistent arbitrary order

### Test 14.3: Killing Last Enemy
**Steps:**
1. Combat encounter with enemies
2. Defeat all enemies

**Expected Behavior:**
- Encounter ends automatically
- Combat UI disappears
- Normal movement restored
- Players can move freely

### Test 14.4: Player Death
**Steps:**
1. Allow player character to fail death saves

**Expected Behavior:**
- Player respawns (normal Minecraft behavior)
- OR: Special death handling if implemented
- Encounter may continue or end based on implementation

### Test 14.5: Out of Range Movement
**Steps:**
1. During combat, try to run far from encounter area

**Expected Behavior:**
- Movement restrictions may apply
- OR: May be ejected from encounter
- Implementation-dependent behavior

---

## Known Limitations & Future Features

Based on implementation status:

### Not Yet Implemented (Per IMPLEMENTATION.md)
1. **GLTF Model Assets** - character_parts.gltf file not created yet
   - May see placeholder/default rendering
2. **Character Texture Atlas** - Texture file not created yet
   - Colors may be default/solid
3. **Spell Casting UI** - Basic spells exist but UI may be limited
4. **Concentration Mechanics** - Spells don't track concentration yet
5. **Multiple Classes** - Only Fighter implemented
6. **Class Features** - No fighter features (Action Surge, Second Wind, etc.)
7. **Full Spell List** - Only 6 initial spells
8. **Resistance/Immunity** - Damage types tracked but not yet affecting calculations

### Testing Priority
1. **High Priority**: Character creation, basic combat, turn order, attack/damage
2. **Medium Priority**: AI behavior, opportunity attacks, death saves, movement restriction
3. **Low Priority**: Advanced UI, animations, spell system (limited implementation)

---

## Reporting Issues

When testing, note for each issue:
- Clear steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots/logs if applicable
- Whether issue is consistent or intermittent

## Performance Metrics

During testing, monitor:
- FPS during encounters
- Network lag in multiplayer
- Time for turn transitions
- UI responsiveness
- Entity pathfinding performance
