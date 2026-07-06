# Stage132 — TC4 Node Stabilizer / Transducer / Node Jar Parity

Base: Stage131.

This stage continues the aura-node branch instead of jumping to a new system. The goal is to make nodes behave closer to Thaumcraft 4 1.7.10 as movable, stabilizable and energizable magical objects.

## Runtime additions

- `thaumcraft:node_jar`
- `thaumcraft:advanced_node_stabilizer`
- `thaumcraft:node_transducer`

## Behaviour

### Node Stabilizer

The normal stabilizer still protects nodes in a TC4-like radius, but it now exposes a strength value to node runtime. Stabilized nodes recover stability and unstable/hungry/dark effects are softened.

### Advanced Node Stabilizer

The advanced stabilizer has a larger radius and stronger effect. It is also used by the node jar runtime to preserve more of the node when it is captured.

### Node Transducer

A powered transducer near a stabilized aura node increases transduction charge. When the node has charged enough it becomes energized. Energized nodes render larger/brighter and regenerate primal vis above their base profile in a controlled way.

### Node in a Jar

Right-click an aura node with an empty node jar to capture it. The jar stores:

- node type;
- node modifier;
- current aspects;
- base aspects;
- stability;
- scanned state.

Capturing is not free: without an advanced stabilizer the node receives a TC4-like preservation penalty and usually weakens toward Pale/Fading. Right-click a block with a filled jar to release the node again.

## Still not final

The exact TC4 OBJ model rendering for stabilizer/transducer and the full energized node relay network still need a later pass. This stage focuses on getting the runtime mechanics working on Forge 1.19.2.
