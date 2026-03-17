# Veltrix Node Reference

This document lists every built-in node, what it does, and its ports.

## Wire Types

- Execution wires: Control flow between event, logic, and action nodes.
- Data wires: Pass values between data/event/logic/action ports.

## Node Colors

- Events: Red
- Logic: Purple
- Actions: Blue
- Data: Green

## Event Nodes

### Plugin Enable

- Type: `event.plugin_enable`
- Purpose: Runs once during plugin startup.
- Outputs:
  - `Then` (Execution)

### Plugin Disable

- Type: `event.plugin_disable`
- Purpose: Runs once during plugin shutdown.
- Outputs:
  - `Then` (Execution)

### Player Join

- Type: `event.player_join`
- Purpose: Triggered when a player joins.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)

### Player Leave

- Type: `event.player_leave`
- Purpose: Triggered when a player leaves.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)

### Player Break Block

- Type: `event.player_break_block`
- Purpose: Triggered when a player breaks a block.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Location` (Data: Location)

### Player Place Block

- Type: `event.player_place_block`
- Purpose: Triggered when a player places a block.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Location` (Data: Location)

### Player Chat

- Type: `event.player_chat`
- Purpose: Triggered when a player sends chat.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Message` (Data: Text)

### Player Move

- Type: `event.player_move`
- Purpose: Triggered when a player moves.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `From` (Data: Location)
  - `To` (Data: Location)

### Player Death

- Type: `event.player_death`
- Purpose: Triggered when a player dies.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Killer` (Data: Player)

### Player Damage

- Type: `event.player_damage`
- Purpose: Triggered when a player takes damage.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Damage` (Data: Number)

### Player Interact

- Type: `event.player_interact`
- Purpose: Triggered when a player interacts.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Location` (Data: Location)
  - `Item` (Data: ItemStack)

### Player Drop Item

- Type: `event.player_drop_item`
- Purpose: Triggered when a player drops an item.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Item` (Data: ItemStack)

### Player Pickup Item

- Type: `event.player_pickup_item`
- Purpose: Triggered when a player picks up an item.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Item` (Data: ItemStack)

### Entity Death

- Type: `event.entity_death`
- Purpose: Triggered when an entity dies.
- Outputs:
  - `Then` (Execution)
  - `Entity` (Data: Entity)
  - `Killer` (Data: Player)

### Block Explode

- Type: `event.block_explode`
- Purpose: Triggered when a block explodes.
- Outputs:
  - `Then` (Execution)
  - `Location` (Data: Location)

### Redstone Change

- Type: `event.redstone_change`
- Purpose: Triggered when redstone power changes.
- Outputs:
  - `Then` (Execution)
  - `Location` (Data: Location)
  - `Power` (Data: Number)

### Command Run

- Type: `event.command_run`
- Purpose: Triggered when a player runs a command.
- Outputs:
  - `Then` (Execution)
  - `Player` (Data: Player)
  - `Command` (Data: Text)
  - `Arguments` (Data: Text)

### Server Tick

- Type: `event.server_tick`
- Purpose: Runs repeatedly every tick.
- Outputs:
  - `Then` (Execution)

## Logic Nodes

### If Condition

- Type: `logic.if_condition`
- Purpose: Branches flow based on a boolean.
- Inputs:
  - `In` (Execution)
  - `Condition` (Data: Boolean)
- Outputs:
  - `True` (Execution)
  - `False` (Execution)

### Compare Values

- Type: `logic.compare_values`
- Purpose: Compares two values and returns boolean.
- Editable fields:
  - `Operator` (`==`, `!=`, `>`, `>=`, `<`, `<=`)
- Inputs:
  - `A` (Data: Any)
  - `B` (Data: Any)
- Outputs:
  - `Equal` (Data: Boolean)

### Boolean And

- Type: `logic.boolean_and`
- Purpose: Boolean conjunction.
- Inputs:
  - `A` (Data: Boolean)
  - `B` (Data: Boolean)
- Outputs:
  - `Result` (Data: Boolean)

### Boolean Or

- Type: `logic.boolean_or`
- Purpose: Boolean disjunction.
- Inputs:
  - `A` (Data: Boolean)
  - `B` (Data: Boolean)
- Outputs:
  - `Result` (Data: Boolean)

### Boolean Not

- Type: `logic.boolean_not`
- Purpose: Boolean negation.
- Inputs:
  - `Value` (Data: Boolean)
- Outputs:
  - `Result` (Data: Boolean)

### Delay Timer

- Type: `logic.delay_timer`
- Purpose: Delays execution flow by ticks.
- Editable fields:
  - `Ticks` (default: `20`)
- Inputs:
  - `In` (Execution)
  - `Ticks` (Data: Number)
- Outputs:
  - `Then` (Execution)

### Random Chance

- Type: `logic.random_chance`
- Purpose: Returns a random boolean chance.
- Editable fields:
  - `Chance` (default: `0.5`, clamped to 0..1)
- Inputs:
  - `Chance` (Data: Number)
- Outputs:
  - `Success` (Data: Boolean)

### Loop

- Type: `logic.loop`
- Purpose: Runs execution multiple times.
- Inputs:
  - `In` (Execution)
  - `Count` (Data: Number)
- Outputs:
  - `Loop` (Execution)
  - `Done` (Execution)

### For Each Player

- Type: `logic.foreach_player`
- Purpose: Iterates through all online players.
- Inputs:
  - `In` (Execution)
- Outputs:
  - `Loop` (Execution)
  - `Player` (Data: Player)
  - `Done` (Execution)

### While Loop

- Type: `logic.while`
- Purpose: Repeats execution while a condition is true.
- Inputs:
  - `In` (Execution)
  - `Condition` (Data: Boolean)
- Outputs:
  - `Loop` (Execution)
  - `Done` (Execution)

### Random Number

- Type: `logic.random_number`
- Purpose: Generates a random number between Min and Max.
- Inputs:
  - `Min` (Data: Number)
  - `Max` (Data: Number)
- Outputs:
  - `Value` (Data: Number)

### Math Operation

- Type: `logic.math`
- Purpose: Performs a math operation on two numbers.
- Editable fields:
  - `Operator` (`+`, `-`, `*`, `/`, `%`)
- Inputs:
  - `A` (Data: Number)
  - `B` (Data: Number)
- Outputs:
  - `Result` (Data: Number)

### Text Join

- Type: `logic.text_join`
- Purpose: Combines multiple text values into one string.
- Inputs:
  - `A` (Data: Text)
  - `B` (Data: Text)
- Outputs:
  - `Text` (Data: Text)

### Number To Text

- Type: `logic.number_to_text`
- Purpose: Converts a number value into text.
- Inputs:
  - `Number` (Data: Number)
- Outputs:
  - `Text` (Data: Text)

## Action Nodes

### Send Message

- Type: `action.send_message`
- Purpose: Sends a chat message to a player.
- Editable fields:
  - `Default Text` (used when Text input is not wired)
- Inputs:
  - `In` (Execution)
  - `Player` (Data: Player)
  - `Text` (Data: Text)
- Outputs:
  - `Then` (Execution)

### Spawn Entity

- Type: `action.spawn_entity`
- Purpose: Spawns an entity at a location.
- Editable fields:
  - `Entity` (default: `ZOMBIE`, used when input not wired)
- Inputs:
  - `In` (Execution)
  - `Location` (Data: Location)
  - `Entity Type` (Data: Text)
- Outputs:
  - `Then` (Execution)

### Give Item

- Type: `action.give_item`
- Purpose: Gives an item to a player.
- Editable fields:
  - `Default Material` (used when Item input is not wired)
  - `Default Amount` (used when Item input is not wired)
- Inputs:
  - `In` (Execution)
  - `Player` (Data: Player)
  - `Item` (Data: ItemStack)
- Outputs:
  - `Then` (Execution)

### Teleport Player

- Type: `action.teleport_player`
- Purpose: Teleports player to location.
- Inputs:
  - `In` (Execution)
  - `Player` (Data: Player)
  - `Location` (Data: Location)
- Outputs:
  - `Then` (Execution)

### Play Sound

- Type: `action.play_sound`
- Purpose: Plays a Bukkit sound for a player.
- Editable fields:
  - `Sound` (default: `ENTITY_EXPERIENCE_ORB_PICKUP`, used when input not wired)
- Inputs:
  - `In` (Execution)
  - `Player` (Data: Player)
  - `Sound` (Data: Text)
- Outputs:
  - `Then` (Execution)

### Run Command

- Type: `action.run_command`
- Purpose: Runs a console command.
- Editable fields:
  - `Command` (default command if input is not wired)
- Inputs:
  - `In` (Execution)
  - `Command` (Data: Text)
- Outputs:
  - `Then` (Execution)

## Data Nodes

### Player Object

- Type: `data.player`
- Purpose: Exposes player context from event-based flows.
- Outputs:
  - `Player` (Data: Player)

### Location

- Type: `data.location`
- Purpose: Provides a configured world location.
- Editable fields:
  - `World` (default: `world`)
  - `X` (default: `0`)
  - `Y` (default: `64`)
  - `Z` (default: `0`)
- Outputs:
  - `Location` (Data: Location)

### ItemStack

- Type: `data.itemstack`
- Purpose: Provides a configured item stack.
- Editable fields:
  - `Material` (default: `DIAMOND`)
  - `Amount` (default: `1`)
- Outputs:
  - `Item` (Data: ItemStack)

### Number

- Type: `data.number`
- Purpose: Provides a numeric value.
- Editable fields:
  - `Number` (default: `1`)
- Outputs:
  - `Value` (Data: Number)

### Text

- Type: `data.text`
- Purpose: Provides a text string.
- Editable fields:
  - `Text`
- Outputs:
  - `Text` (Data: Text)

## Practical Notes

- If a node input is not wired, most nodes use their editable default field.
- Compare operator defaults to `==` if invalid.
- Numeric fields fallback safely if invalid input is entered.
- Material, sound, and entity names should use Bukkit enum names.
