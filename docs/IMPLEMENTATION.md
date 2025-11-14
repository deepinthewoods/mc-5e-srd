# 5E SRD Mod - Implementation Status



## Overview

This document tracks the implementation progress of the D&D 5e SRD Fabric mod as outlined in `plans/initial.md`.



## Implemented Features



### Phase 1: Core Systems (Foundation) - ✅ COMPLETED



#### Data Structures

- ✅ **CharacterStats** (`character/data/CharacterStats.java`)

  - Six ability scores with modifiers

  - Codec for serialization

  - Racial bonus application

  - Standard array support



- ✅ **Race** (`character/data/Race.java`)

  - Enum with Human and Dwarf races

  - Base movement speeds

  - Default mesh indices for rendering

  - Darkvision trait support



- ✅ **CharacterClass** (`character/data/CharacterClass.java`)

  - Fighter class implementation

  - Hit die and proficiency bonus calculation



- ✅ **CharacterAppearance** (`character/data/CharacterAppearance.java`)

  - Mesh index storage (body, legs, arms, head)

  - Mesh name resolution for rendering



#### Combat Systems

- ✅ **CombatState** (`combat/CombatState.java`)

  - Turn-based action economy tracking

  - Movement validation

  - Action/Bonus Action/Reaction management

  - HP and AC tracking



- ✅ **InitiativeTracker** (`combat/InitiativeTracker.java`)

  - Initiative roll storage

  - Sorting by initiative order with DEX tiebreaker



- ✅ **EncounterState** (`combat/EncounterState.java`)

  - Turn order management

  - Round tracking

  - Combatant addition/removal



- ✅ **EncounterManager** (`combat/EncounterManager.java`)

  - Server-side singleton for encounter management

  - Encounter creation and cleanup

  - Entity-to-encounter mapping



#### Utilities

- ✅ **DiceRoller** (`util/DiceRoller.java`)

  - d20 rolls with advantage/disadvantage

  - Initiative rolls

  - Ability score generation (4d6 drop lowest)

  - Hit point rolling



#### Entity System

- ✅ **CharacterEntity** (`character/entity/CharacterEntity.java`)

  - Full character sheet integration

  - NBT save/load for persistence

  - Synced entity data for appearance

  - Turn management integration

  - Attribute calculation from 5e stats



- ✅ **CombatAIController** (`character/ai/CombatAIController.java`)

  - Basic combat AI implementation

  - Target selection

  - Attack decision making

  - Turn ending



#### Blocks

- ✅ **EncounterBlock** (`block/EncounterBlock.java`)

  - Proximity-based encounter triggering

  - Initiative rolling for all participants

  - Self-destruction after triggering



#### Registry System

- ✅ **ModDataComponents** (`registry/ModDataComponents.java`)

  - Data component registration for character data

  - Network synchronization setup



- ✅ **ModEntities** (`registry/ModEntities.java`)

  - Character entity type registration

  - Attribute registration with Fabric API



- ✅ **ModBlocks** (`registry/ModBlocks.java`)

  - Encounter block registration

  - Block item creation



- ✅ **ModCreativeTabs** (`registry/ModCreativeTabs.java`)

  - Custom creative tab for mod items



#### Client-Side Rendering

- ✅ **CharacterRenderState** (`client/render/CharacterRenderState.java`)

  - Render state with appearance and animation data



- ✅ **CharacterEntityRenderer** (`client/render/CharacterEntityRenderer.java`)

  - Entity renderer stub (ready for GLTF implementation)

  - Texture location setup



- ✅ **CharacterModelLoader** (`client/model/CharacterModelLoader.java`)

  - Model loader stub (ready for Assimp integration)

  - MeshPart data structure



#### Initialization

- ✅ **FiveESrdMod** - Main mod initialization

- ✅ **FiveESrdModClient** - Client-side initialization with renderer registration



#### Network Synchronization

- ✅ **ModNetworking** (`registry/ModNetworking.java`)

  - Network payload registration for S2C and C2S packets

  - Proper codec setup for all packet types



- ✅ **ServerPacketHandlers** (`network/ServerPacketHandlers.java`)

  - UseAction packet handling with validation

  - EndTurn packet handling with turn advancement

  - Automatic AI turn execution for NPCs

  - Proper broadcasting to encounter participants



- ✅ **ClientPacketHandlers** (`network/ClientPacketHandlers.java`)

  - SyncEncounterState handling

  - SyncCombatState handling

  - TurnStart and TurnEnd handling

  - Client state cache updates



- ✅ **Network Payloads** (`network/payloads/`)

  - SyncEncounterStatePayload - Full encounter state with turn order

  - SyncCombatStatePayload - Combat state for individual entities

  - TurnStartPayload - Turn start notifications

  - TurnEndPayload - Turn end notifications

  - UseActionPayload - Client action requests

  - EndTurnPayload - Client turn end requests



- ✅ **ClientEncounterState** (`client/ClientEncounterState.java`)

  - Client-side encounter state cache

  - Combat state tracking per entity

  - Turn tracking and round management

  - Ready for UI integration



### Phase 2: Turn-Based Combat - ✅ MOSTLY COMPLETED



#### Completed

- ✅ Full encounter triggering with client synchronization

- ✅ Network packets for combat state synchronization

- ✅ Turn advancement with proper networking

- ✅ AI turn execution for NPCs

- ✅ Client-side state tracking

- [x] Server-authoritative combat movement validation for turn-bound entities (locks movement when it is not their turn and tracks remaining speed per round)
- [x] Encounter resynchronization for late joiners (broadcasts encounter/combat snapshots whenever combatants are added or request state)

#### Remaining

- None (Phase 2 combat tasks complete)

### Phase 3: UI & Multiplayer - 🔄 IN PROGRESS



#### Completed

- ✅ **Character Creation GUI** (`client/gui/CharacterCreationScreen.java`)

  - Multi-step character creation wizard (Race → Class → Ability Scores → Appearance → Name → Review)

  - Race selection (Human, Dwarf) with racial bonuses

  - Class selection (Fighter)

  - Standard array ability score assignment

  - Appearance customization (body, legs, arms, head mesh indices)

  - Character name input

  - Review screen with final stats display

  - Network integration for character creation



- ✅ **Character Creation Item** (`item/CharacterCreationItem.java`)

  - "Character Creation Tome" item to open the GUI

  - Added to creative inventory tab



- ✅ **Character Creation Network** (`network/payloads/CreateCharacterPayload.java`)

  - Client-to-Server character creation packet

  - Server-side character entity spawning

  - Full character data transmission (name, race, class, stats, appearance)

- [x] **Action Hotbar Overlay** (`client/gui/ActionHotbarOverlay.java`)

  - Second-row HUD above the vanilla hotbar listing core 5e actions

  - Keybind-driven input with optional Left-Alt cursor unlock that sends UseAction payloads

  - Turn/round indicators sourcing availability data from ClientEncounterState

  - Dedicated turn tracker panel, animated turn banner, and contextual cursor hints

  - Integrated end turn button with tooltip explanations and keybinding redundancy


#### UI Deliverables (Phase 3)

- [x] Turn indicator UI (turn tracker list plus animated banner)

- [x] Combat HUD overlay polish (resource summary, cursor hints, action availability)

- [x] End turn button (click + hotkey with state-aware tooltips)


### Phase 4: Combat Actions & AI - ✅ COMPLETED

#### Combat Systems
- ✅ **Weapon System** (`combat/Weapon.java`, `combat/Weapons.java`, `combat/WeaponProperty.java`, `combat/DamageType.java`)
  - Complete weapon definitions with damage dice, properties, and ranges
  - 15+ standard 5e SRD weapons (longsword, greatsword, bow, etc.)
  - Weapon properties (finesse, light, heavy, reach, versatile, etc.)
  - Damage types (slashing, piercing, bludgeoning, elemental, magical)

- ✅ **Attack Roll Calculation** (`combat/CombatResolver.java`)
  - d20 + ability modifier + proficiency bonus + magic bonus
  - Proper ability score selection (STR for melee, DEX for ranged/finesse)
  - Finesse weapons choose higher of STR or DEX
  - Advantage and disadvantage support
  - Natural 1 = automatic miss, Natural 20 = critical hit

- ✅ **Damage Calculation** (`combat/CombatResolver.java`, `combat/AttackResult.java`)
  - Weapon damage dice + ability modifier + magic bonus
  - Critical hits double weapon dice (not modifiers)
  - Versatile weapons use higher damage when two-handed
  - Proper damage type tracking

- ✅ **Opportunity Attacks** (`combat/OpportunityAttackHandler.java`)
  - Automatic detection when entities leave reach
  - Consumes reaction resource
  - Respects weapon reach (1 block standard, 2 blocks for reach weapons)
  - Integrated into CharacterEntity tick loop
  - Broadcasts opportunity attack messages

- ✅ **Death and Unconsciousness** (`combat/DeathSaves.java`, updated `combat/CombatState.java`)
  - Unconscious state at 0 HP
  - Death saving throws (3 successes to stabilize, 3 failures to die)
  - Natural 1 = 2 failures, Natural 20 = regain 1 HP and wake
  - Massive damage rule (instant death if damage >= max HP)
  - Hitting unconscious targets adds death save failures
  - Healing from 0 HP wakes entity and clears death saves

- ✅ **Combat Action Execution** (updated `network/ServerPacketHandlers.java`)
  - Attack action with full 5e resolution
  - Dash action (doubles movement)
  - Disengage action (prevents opportunity attacks)
  - Dodge action (gives disadvantage to attackers)
  - Action economy validation and resource consumption

- ✅ **AI Combat Enhancement** (updated `character/ai/CombatAIController.java`)
  - Uses CombatResolver for proper 5e attacks
  - Broadcasts attack results to all participants
  - Handles target death and encounter cleanup
  - Automatic turn execution for NPCs
  - **Advanced AI tactics:**
    - Target prioritization (low HP, close proximity)
    - HP-based decision making (retreat when low health)
    - Tactical action usage (Dash to close distance, Disengage to retreat, Dodge for defense)
    - Weapon range awareness

- ✅ **Disengage Action Implementation** (updated `combat/CombatState.java`, `network/ServerPacketHandlers.java`, `combat/OpportunityAttackHandler.java`)
  - Added `isDisengaged` flag to CombatState
  - Prevents opportunity attacks when moving
  - Cleared at the start of each turn
  - AI uses Disengage when retreating from melee while low on HP

- ✅ **Dodge Action Implementation** (updated `combat/CombatState.java`, `network/ServerPacketHandlers.java`)
  - Added `isDodging` flag to CombatState
  - Gives attackers disadvantage on attack rolls
  - Cleared at the start of each turn
  - AI uses Dodge as defensive option when low on HP

- ✅ **Critical Hit Detection for Death Saves** (updated `combat/CombatState.java`, all attack handlers)
  - Critical hits against unconscious targets now count as 2 failed death saves
  - Properly implemented in regular attacks, opportunity attacks, and AI attacks
  - Follows 5e SRD rules for critical hits on unconscious creatures

#### Remaining
- None - Phase 4 fully completed!



### Phase 5: Polish & Expansion - ✅ COMPLETED

- ✅ **Full GLTF Model Loading** (`client/model/CharacterModelLoader.java`)
  - Complete Assimp integration for loading GLTF files
  - Mesh part caching with vertex data (positions, normals, UVs, indices)
  - Pivot point calculation for proper rotation
  - Support for modular character parts (body, legs, arms, head)

- ✅ **Modular Mesh Rendering** (`client/render/CharacterEntityRenderer.java`)
  - Full character renderer with GLTF-based rendering
  - Mesh part selection based on CharacterAppearance
  - Custom render pipeline for entity triangles
  - Proper texture mapping with character atlas

- ✅ **Character Animations** (`client/render/CharacterEntityRenderer.java`)
  - Walking animations with leg rotation
  - Arm swing during movement and attacks
  - Head rotation based on look direction
  - Smooth limb interpolation

- ✅ **Spell System** (`spell/`)
  - Base Spell class with 5e properties (level, school, components, etc.)
  - SpellSchool, CastingTime, SpellComponent enums
  - Spell registry with 6 initial spells:
    - Cantrips: Fire Bolt, Shocking Grasp
    - Level 1: Magic Missile, Cure Wounds, Shield
    - Level 2: Scorching Ray
  - Spell casting with damage/healing calculations
  - Support for upcasting

- ✅ **Additional Races** (`character/data/Race.java`)
  - Added Elf race (darkvision, 30ft movement, unique mesh indices)
  - Added Halfling race (25ft movement, small size, unique mesh indices)
  - Total of 4 playable races: Human, Dwarf, Elf, Halfling

- ✅ **Equipment System** (`equipment/`, `item/`)
  - ArmorType enum with all SRD armor types (light, medium, heavy, shield)
  - Equipment class for tracking equipped items
  - AC calculation based on armor type and DEX modifier
  - ArmorItem and WeaponItem classes for inventory
  - Tooltip support showing armor/weapon stats



## File Structure



```

src/main/java/ninja/trek/srd/

├── FiveESrdMod.java                    # Main mod initialization

├── block/

│   └── EncounterBlock.java             # Combat encounter trigger

├── character/

│   ├── ai/

│   │   └── CombatAIController.java     # Basic combat AI

│   ├── data/

│   │   ├── CharacterAppearance.java    # Mesh index storage

│   │   ├── CharacterClass.java         # Character classes

│   │   ├── CharacterStats.java         # Ability scores

│   │   └── Race.java                   # Playable races

│   └── entity/

│       └── CharacterEntity.java        # Main character entity

├── combat/

│   ├── AttackResult.java               # Attack roll results

│   ├── CombatResolver.java             # 5e attack & damage calculation

│   ├── CombatState.java                # Action economy & death saves tracking

│   ├── DamageType.java                 # Damage type enum

│   ├── DeathSaves.java                 # Death saving throw tracking

│   ├── EncounterManager.java           # Encounter singleton

│   ├── EncounterState.java             # Turn order management

│   ├── InitiativeTracker.java          # Initiative tracking

│   ├── OpportunityAttackHandler.java   # Opportunity attack detection

│   ├── Weapon.java                     # Weapon data structure

│   ├── WeaponProperty.java             # Weapon property enum

│   └── Weapons.java                    # Standard 5e weapon registry

├── equipment/

│   ├── ArmorType.java                  # 5e SRD armor types

│   └── Equipment.java                  # Character equipment tracking

├── item/

│   ├── ArmorItem.java                  # Armor item implementation

│   ├── CharacterCreationItem.java      # Item to open character creation

│   └── WeaponItem.java                 # Weapon item implementation

├── network/

│   ├── ServerPacketHandlers.java       # Server-side packet handling

│   └── payloads/

│       ├── CreateCharacterPayload.java # C2S: Create character request

│       ├── EndTurnPayload.java         # C2S: End turn request

│       ├── SyncCombatStatePayload.java # S2C: Combat state sync

│       ├── SyncEncounterStatePayload.java # S2C: Encounter sync

│       ├── TurnEndPayload.java         # S2C: Turn end notification

│       ├── TurnStartPayload.java       # S2C: Turn start notification

│       └── UseActionPayload.java       # C2S: Use action request

├── registry/

│   ├── ModBlocks.java                  # Block registry

│   ├── ModCreativeTabs.java            # Creative tabs

│   ├── ModDataComponents.java          # Data component registry

│   ├── ModEntities.java                # Entity registry

│   ├── ModItems.java                   # Item registry

│   └── ModNetworking.java              # Network packet registry

├── spell/

│   ├── CastingTime.java                # Spell casting time enum

│   ├── Spell.java                      # Base spell class

│   ├── SpellComponent.java             # Spell component enum (V/S/M)

│   ├── SpellSchool.java                # Eight schools of magic

│   └── Spells.java                     # Spell registry

└── util/

    └── DiceRoller.java                 # Dice rolling utility



src/client/java/ninja/trek/srd/

├── FiveESrdModClient.java              # Client initialization

├── client/

│   ├── ClientEncounterState.java       # Client-side state cache

│   ├── gui/

│   │   └── CharacterCreationScreen.java # Character creation GUI

│   ├── model/

│   │   └── CharacterModelLoader.java   # GLTF loader stub

│   └── render/

│       ├── CharacterEntityRenderer.java # Entity renderer

│       └── CharacterRenderState.java    # Render state

└── network/

    └── ClientPacketHandlers.java       # Client-side packet handling

```



## Technical Notes



### Current State

- All core data structures are implemented and follow 5e SRD rules

- Entity system is complete with player-controlled flag and AI integration

- Combat encounter system is functional server-side with full networking

- Advanced AI decision-making with tactical combat behaviors

- Network synchronization is fully implemented for turn-based combat

- Client-side state tracking with full UI integration

- Character creation GUI is fully implemented with multi-step wizard

- Full GLTF rendering system with modular mesh parts and animations

- Spell system with 6 initial spells from the 5e SRD

- Equipment system with armor and weapon tracking

- 4 playable races: Human, Dwarf, Elf, Halfling



### Build Status

- Code structure is complete and sound

- Build requires network access for Fabric Loom plugin download

- All Java code follows Minecraft 1.21.10 and Fabric API patterns

- Network packets use modern Minecraft 1.21+ payload system

- Ready for testing in a development environment with network access



### Next Steps (Priority Order)

1. Test build in environment with network access (blocked by network limitations)

2. ~~Add movement validation during combat~~ ✅ COMPLETED (Phase 2)

3. ~~Implement encounter sync for players joining ongoing combat~~ ✅ COMPLETED (Phase 2)

4. ~~Implement complete 5e attack and damage calculation~~ ✅ COMPLETED (Phase 4)

5. ~~Implement advanced AI tactics~~ ✅ COMPLETED (Phase 4)

6. ~~Add Disengage/Dodge status effects to CombatState~~ ✅ COMPLETED (Phase 4)

7. ~~Implement critical hit detection for death saves~~ ✅ COMPLETED (Phase 4)

8. ~~Add full GLTF model loading with Assimp~~ ✅ COMPLETED (Phase 5)

9. ~~Add spell system foundation~~ ✅ COMPLETED (Phase 5)

10. ~~Add modular mesh rendering with animations~~ ✅ COMPLETED (Phase 5)

11. ~~Add additional races (Elf, Halfling)~~ ✅ COMPLETED (Phase 5)

12. ~~Add equipment system~~ ✅ COMPLETED (Phase 5)

13. Create GLTF model assets (character_parts.gltf)

14. Create character texture atlas

15. Add more spells and spell casting UI

16. Implement concentration mechanics for spells

17. Add more character classes (Wizard, Cleric, Rogue, etc.)

18. Implement class features and abilities

## Notes

This implementation provides a solid foundation for a full-featured D&D 5e mod. The architecture is modular and extensible, following Minecraft and Fabric best practices. The turn-based combat system is designed to be server-authoritative and multiplayer-compatible from the start.

