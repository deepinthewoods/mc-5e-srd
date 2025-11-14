# Animation State Machine - Phase 8.3

## Overview

The Animation State Machine provides a sophisticated priority-based system for managing character animations in the GeckoLib rendering system. It handles smooth transitions, interrupt conditions, animation queueing, and complex behaviors like walking while casting or interrupting attacks to block.

## Architecture

### Core Components

1. **AnimationState** (`ninja.trek.srd.character.animation.AnimationState`)
   - Enum defining all possible animation states
   - Each state has a priority level, interrupt flag, and default transition time
   - States are organized into three priority tiers

2. **AnimationPriority** (`ninja.trek.srd.character.animation.AnimationPriority`)
   - Enum defining priority levels (ACTION, LOCOMOTION, IDLE)
   - Lower numeric values = higher priority
   - Used to determine interrupt rules

3. **AnimationAction** (`ninja.trek.srd.character.animation.AnimationAction`)
   - Represents a triggered animation with metadata
   - Includes duration, transition time, movement allowance, and callbacks
   - Provides builder pattern for custom actions and predefined factory methods

4. **AnimationTransition** (`ninja.trek.srd.character.animation.AnimationTransition`)
   - Tracks smooth transitions between states
   - Provides ease-in-out blending curves
   - Updates over time and reports completion

5. **CharacterAnimationController** (`ninja.trek.srd.client.render.animation.CharacterAnimationController`)
   - Client-side state machine implementation
   - Manages state transitions, queueing, and callbacks
   - Updates locomotion based on entity flags

6. **CharacterEntity Integration** (`ninja.trek.srd.character.entity.CharacterEntity`)
   - Tracks animation state flags (isAttacking, isCasting, etc.)
   - Provides action trigger methods (startAttack(), startCast(), etc.)
   - Implements priority-based animation controller in GeckoLib integration

## Priority System

### Priority Levels

**Priority 1: Action Animations (Highest)**
- ATTACKING - Cannot be interrupted
- CASTING - Cannot be interrupted
- BLOCKING - Looping, can move
- DODGING - Cannot be interrupted
- USING_ITEM - Cannot be interrupted
- CHANNELING - Looping, can move
- DEATH - Cannot be interrupted

**Priority 2: Locomotion**
- RUNNING - Can be interrupted by actions
- WALKING - Can be interrupted by actions
- JUMPING - Can be interrupted
- FALLING - Can be interrupted
- SWIMMING - Can be interrupted

**Priority 3: Idle (Lowest)**
- IDLE - Can be interrupted by anything

### Interrupt Rules

Transitions are allowed if:
1. Target state has higher or equal priority, OR
2. Current state can be interrupted (`canBeInterrupted()` returns true), OR
3. Current animation has completed

Example scenarios:
- **Walking → Attacking**: Allowed (higher priority)
- **Attacking → Walking**: Not allowed (attack must complete)
- **Blocking → Attacking**: Allowed (blocking is interruptible)
- **Casting → Blocking**: Not allowed (cast must complete)
- **Walking → Running**: Allowed (same priority, both interruptible)

## State Transitions

### Transition Flow

```
1. Action triggered (e.g., startAttack())
   ↓
2. Check if transition allowed (priority + interrupt rules)
   ↓
3a. If allowed:
    - Interrupt current action (if any)
    - Start transition to new state
    - Set new action as current
    ↓
3b. If not allowed but queueable:
    - Add to action queue
    - Process when current action completes
    ↓
4. Transition updates over time with blending
   ↓
5. Transition completes, state becomes current
   ↓
6. Animation plays until completion or interruption
   ↓
7. On completion: Execute callback, return to idle/locomotion
```

### Transition Times

Each state has a default transition time:
- **Actions**: 0.4-1.0 seconds (varies by action)
- **Locomotion**: 0.1-0.3 seconds (quick transitions)
- **Idle**: 0.3 seconds

Transitions use ease-in-out curves for smooth blending.

## Animation Queueing

The system supports queueing actions to be played sequentially:

```java
// Queue multiple attacks
controller.startAction(AnimationAction.meleeAttack());  // Plays immediately
controller.queueAction(AnimationAction.meleeAttack());  // Queued
controller.queueAction(AnimationAction.meleeAttack());  // Queued

// Attacks will play one after another
```

Queue behavior:
- Actions are processed FIFO (first in, first out)
- Queue is processed when transitioning to IDLE
- Non-queueable actions (like casts) cannot be queued
- Queue can be cleared with `clearQueue()`

## Complex Scenarios

### Walking While Casting (Channeled Spell)

```java
// Start channeling - allows movement
entity.startChannel();
// Character can walk while channel animation plays
// Animation: CHANNELING (looping) + locomotion blended
```

The channel animation allows movement (`allowMovement=true`), so locomotion can blend with it.

### Interrupting Attack to Block

```java
// Character is attacking
entity.startAttack();

// Enemy attacks, need to block
entity.stopBlock();  // Won't work - attack cannot be interrupted
entity.startBlock(); // Queued or rejected

// Attack completes, then block can start
```

Since attacking cannot be interrupted, blocking must wait or be queued.

### Transitioning from Combat to Idle

```java
// In combat, blocking
entity.startBlock();

// Combat ends
entity.stopBlock();

// Smooth transition: BLOCKING → IDLE (0.3s transition)
```

When blocking stops, the system automatically transitions to IDLE with smooth blending.

### Attack Combo Queue

```java
// Queue attack combo
entity.startAttack();  // First attack plays
controller.queueAction(AnimationAction.meleeAttack());  // Queued
controller.queueAction(AnimationAction.meleeAttack());  // Queued

// Attacks play sequentially with smooth transitions between them
```

## Integration Points

### CharacterEntity Methods

**Trigger Actions:**
```java
boolean startAttack()    // Start attack animation
boolean startCast()      // Start cast animation
boolean startChannel()   // Start channeling animation
boolean startBlock()     // Start blocking animation
void stopChannel()       // Stop channeling
void stopBlock()         // Stop blocking
void completeAttack()    // Complete attack manually
void completeCast()      // Complete cast manually
```

**State Queries:**
```java
boolean isAttacking()
boolean isCasting()
boolean isBlocking()
boolean isChanneling()
long getAttackElapsedTime()
long getCastElapsedTime()
```

### CharacterAnimationController Methods

**State Control:**
```java
boolean startAction(AnimationAction action)  // Start action with priority checking
void queueAction(AnimationAction action)     // Queue action for later
void clearQueue()                            // Clear action queue
void forceTransition(AnimationState, float)  // Force immediate transition
void reset()                                 // Reset to default state
```

**State Queries:**
```java
AnimationState getCurrentState()
AnimationState getTargetState()
AnimationTransition getActiveTransition()
boolean isTransitioning()
AnimationAction getCurrentAction()
int getQueuedActionCount()
```

**Locomotion Update:**
```java
void updateLocomotion(boolean isMoving, boolean isSprinting,
                     boolean isJumping, boolean isFalling,
                     boolean isSwimming)
```

## Usage Examples

### Basic Attack

```java
CharacterEntity entity = ...;

// Start attack
if (entity.startAttack()) {
    // Attack started successfully
    // Animation will play for 0.6 seconds
    // Then automatically complete and return to idle/locomotion
}
```

### Spell Casting with Callback

```java
AnimationAction castAction = AnimationAction.builder(AnimationState.CASTING)
    .duration(1.5f)
    .onComplete(e -> {
        // Cast completed, execute spell effect
        castSpell((CharacterEntity) e);
    })
    .onInterrupt(e -> {
        // Cast interrupted, refund mana
        refundMana((CharacterEntity) e);
    })
    .build();

controller.startAction(castAction);
```

### Complex Combat Sequence

```java
// Character starts walking toward enemy
// (Locomotion automatically handled by entity movement)

// Player presses attack
entity.startAttack();
// State: WALKING → ATTACKING (0.5s transition)

// Attack completes
// State: ATTACKING → IDLE (0.3s transition)

// Player moves again
// State: IDLE → WALKING (0.2s transition)

// Enemy attacks, player blocks
entity.startBlock();
// State: WALKING → BLOCKING (0.3s transition)

// Player can still move while blocking
// BLOCKING continues while moving

// Player releases block
entity.stopBlock();
// State: BLOCKING → WALKING (0.3s transition)
```

## Animation Definitions Required

The following animations must be defined in GeckoLib model files:

### Action Animations
- `attack` - Melee attack animation (0.6s)
- `cast` - Spell casting animation (1.0s)
- `block` - Blocking/defending loop
- `dodge` - Quick dodge animation (0.4s)
- `channel` - Channeling spell loop
- `use_item` - Using item animation (0.8s)
- `death` - Death animation (1.5s)

### Locomotion Animations
- `walk` - Walking loop
- `run` - Running loop
- `jump` - Jump animation (0.4s)
- `fall` - Falling loop
- `swim` - Swimming loop

### Idle Animation
- `idle` - Default idle loop

## Future Enhancements

Potential improvements for future phases:

1. **Animation Blending Layers**
   - Upper body animations (attacks) + lower body animations (walking)
   - Would allow attacking while walking

2. **Animation Events**
   - Trigger events at specific animation keyframes
   - Example: Deal damage at the apex of sword swing

3. **Context-Aware Transitions**
   - Different transition animations based on context
   - Example: Different attack → block transition vs walk → block

4. **Animation Speed Modifiers**
   - Speed up/slow down animations based on haste/slow effects
   - Already partially implemented for locomotion based on race

5. **Directional Animations**
   - Different animations for forward/backward/strafe movement
   - Dodge in different directions

6. **Network Synchronization**
   - Sync animation states across clients
   - Currently handled implicitly through entity state

## Technical Notes

- **Client-Side Only**: The CharacterAnimationController is client-side only. Server uses entity state flags.
- **Delta Time**: Controller calculates delta time from system time, may need adjustment for better precision.
- **GeckoLib Integration**: Uses GeckoLib 5's `AnimationTest` API for compatibility.
- **Thread Safety**: Not thread-safe, should only be accessed from render thread.
- **Memory**: Action queue uses `LinkedList` for efficient add/remove operations.

## Files Modified/Created

### Created Files
- `/src/main/java/ninja/trek/srd/character/animation/AnimationState.java`
- `/src/main/java/ninja/trek/srd/character/animation/AnimationPriority.java`
- `/src/main/java/ninja/trek/srd/character/animation/AnimationAction.java`
- `/src/main/java/ninja/trek/srd/character/animation/AnimationTransition.java`
- `/ANIMATION_STATE_MACHINE.md` (this file)

### Modified Files
- `/src/client/java/ninja/trek/srd/client/render/animation/CharacterAnimationController.java`
  - Expanded with full state machine implementation
  - Added state tracking, transitions, queueing
  - Implemented priority-based animation system

- `/src/main/java/ninja/trek/srd/character/entity/CharacterEntity.java`
  - Added animation state tracking fields
  - Added action trigger methods (startAttack, startCast, etc.)
  - Enhanced animation controller with priority system
  - Integrated with combat state for death/unconscious

## Summary

The Animation State Machine provides a robust, priority-based system for managing character animations with:

- **3-tier priority system** (Action > Locomotion > Idle)
- **Smooth transitions** with configurable timing and easing
- **Interrupt handling** based on priority rules
- **Action queueing** for combo sequences
- **Completion callbacks** for game logic integration
- **Complex behavior support** (walking while casting, etc.)

The system is designed to be extensible and integrates seamlessly with the existing GeckoLib rendering pipeline and D&D combat system.
