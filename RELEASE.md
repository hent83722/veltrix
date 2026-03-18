# Veltrix Release Notes

## New Logic Nodes

This release adds five new logic nodes for flow control and math workflows:

- Loop (`logic.loop`)
  - Inputs: `In` (Execution), `Count` (Data: Number)
  - Outputs: `Loop` (Execution), `Done` (Execution)

- For Each Player (`logic.foreach_player`)
  - Inputs: `In` (Execution)
  - Outputs: `Loop` (Execution), `Player` (Data: Player), `Done` (Execution)

- While Loop (`logic.while`)
  - Inputs: `In` (Execution), `Condition` (Data: Boolean)
  - Outputs: `Loop` (Execution), `Done` (Execution)

- Random Number (`logic.random_number`)
  - Inputs: `Min` (Data: Number), `Max` (Data: Number)
  - Outputs: `Value` (Data: Number)

- Math Operation (`logic.math`)
  - Editable field: `Operator` (`+`, `-`, `*`, `/`, `%`)
  - Inputs: `A` (Data: Number), `B` (Data: Number)
  - Outputs: `Result` (Data: Number)

## Included Improvements

- Export/code generation support for all new nodes
- Math operator editing support in the node UI
- Node reference documentation updated in `docs/NODES.md`
