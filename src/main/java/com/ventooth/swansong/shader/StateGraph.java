/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.shader;

import com.ventooth.swansong.Share;
import com.ventooth.swansong.StackStateTracker;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import static com.ventooth.swansong.shader.ShaderEngine.lockShader;
import static com.ventooth.swansong.shader.ShaderEngine.popShader;
import static com.ventooth.swansong.shader.ShaderEngine.pushShader;
import static com.ventooth.swansong.shader.ShaderEngine.shaderData;
import static com.ventooth.swansong.shader.ShaderEngine.state;
import static com.ventooth.swansong.shader.ShaderEngine.unlockShader;
import static com.ventooth.swansong.shader.ShaderEngine.use;
import static com.ventooth.swansong.shader.ShaderState.popRenderStage;
import static com.ventooth.swansong.shader.ShaderState.pushRenderStage;
import static com.ventooth.swansong.shader.ShaderState.updateRenderStage;
import static com.ventooth.swansong.shader.StateGraph.Node.BeginFrame;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderBegin;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderBlockDamage;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderBlockEntities0;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderBlockEntities1;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderChunk0;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderChunk1;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderClouds;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderEntities0;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderEntities1;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderHand0;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderLast;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderParticles;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderParticlesLit;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderSelectionBox;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderSkyBasic;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderSkyTextured;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderWeather;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderWeatherEntities0;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderWeatherEntities1;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowBegin;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowBlockEntities0;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowBlockEntities1;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowChunk0;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowChunk1;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowEntities0;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowEntities1;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowLast;
import static com.ventooth.swansong.shader.StateGraph.Node.Unmanaged;

@SuppressWarnings("Convert2MethodRef")
public class StateGraph {
    private static final EnumMap<Node, EnumMap<Node, Runnable>> graph = new EnumMap<>(Node.class);

    private static void edge(Node from, Node to, @NotNull Runnable logic) {
        val map = graph.computeIfAbsent(from, _from -> new EnumMap<>(Node.class));
        if (map.containsKey(to)) {
            throw new AssertionError("Duplicate graph edge: " + from.name() + " -> " + to.name());
        }
        map.put(to, logic);
    }


    private static void edge(Node from, Node[] to, @NotNull Runnable logic) {
        for (val b : to) {
            edge(from, b, logic);
        }
    }

    private static void edge(Node[] from, Node to, @NotNull Runnable logic) {
        for (val a : from) {
            edge(a, to, logic);
        }
    }

    private static void edge(Node[] from, Node[] to, @NotNull Runnable logic) {
        for (val a : from) {
            for (val b : to) {
                edge(a, b, logic);
            }
        }
    }

    private static void edge(Node from, Node to) {
        edge(from, to, () -> {});
    }

    //Mirrors the layout of MANAGED_STATE.adoc
    static {
        edge(Unmanaged, BeginFrame);
        edge(BeginFrame, ShadowBegin, () -> {
            use(state.manager.shadow);
            lockShader();
        });
        edge(BeginFrame, RenderBegin);
        edge(RenderLast, Unmanaged);

        edge(ShadowLast, RenderBegin, () -> {
            unlockShader();
        });

        edge(ShadowBegin, ShadowChunk0, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_SOLID);
        });
        edge(ShadowChunk0, ShadowEntities0, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowChunk0, ShadowChunk1, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
        });
        edge(ShadowEntities0, ShadowBlockEntities0, () -> {
            updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
        });
        edge(ShadowBlockEntities0, ShadowEntities0, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowBlockEntities0, ShadowChunk1, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
        });
        edge(ShadowChunk1, ShadowEntities1, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowChunk1, ShadowLast);
        edge(ShadowEntities1, ShadowBlockEntities1, () -> {
            updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(ShadowBlockEntities1, ShadowEntities1, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowBlockEntities1, ShadowLast);

        edge(RenderBegin, RenderSkyBasic, () -> {
            shaderData.pushEntity(ShaderEntityData.SKY);
            updateRenderStage(MCRenderStage.SKY);
            use(state.manager.skybasic);
        });
        edge(RenderBegin, RenderClouds, () -> {
            shaderData.pushEntity(ShaderEntityData.CLOUDS);
            updateRenderStage(MCRenderStage.CLOUDS);
            use(state.manager.clouds);
        });
        edge(RenderBegin, RenderChunk0, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            use(state.manager.terrain);
        });
        edge(new Node[]{RenderSkyBasic, RenderSkyTextured}, RenderClouds, () -> {
            shaderData.popEntity();

            shaderData.pushEntity(ShaderEntityData.CLOUDS);
            updateRenderStage(MCRenderStage.CLOUDS);
            use(state.manager.clouds);
        });
        edge(new Node[]{RenderSkyBasic, RenderSkyTextured}, RenderChunk0, () -> {
            shaderData.popEntity();

            updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            use(state.manager.terrain);
        });
        edge(RenderSkyBasic, RenderSkyBasic);
        edge(RenderSkyBasic, RenderSkyTextured, () -> use(state.manager.skytextured));
        edge(RenderSkyTextured, RenderSkyBasic, () -> use(state.manager.skybasic));
        edge(RenderSkyTextured, RenderSkyTextured);

        edge(RenderClouds, RenderChunk0, () -> {
            shaderData.popEntity();
            updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            use(state.manager.terrain);
        });
        edge(RenderChunk0, RenderWeatherEntities0, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
            use(state.manager.entities);
        });
        edge(RenderChunk0, RenderSelectionBox, () -> {
            updateRenderStage(MCRenderStage.NONE);
            use(state.manager.basic);
        });
        edge(RenderWeatherEntities0, RenderEntities0);
        edge(RenderEntities0, RenderBlockEntities0, () -> {
            ShaderState.updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
            use(state.manager.block);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(RenderBlockEntities0, RenderWeatherEntities0, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
            use(state.manager.entities);
        });
        edge(RenderBlockEntities0, RenderSelectionBox, () -> {
            updateRenderStage(MCRenderStage.NONE);
            use(state.manager.basic);
        });
        edge(RenderSelectionBox, RenderBlockDamage, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            use(state.manager.terrain);
        });
        edge(RenderBlockDamage, RenderParticlesLit, () -> {
            updateRenderStage(MCRenderStage.PARTICLES);
            use(state.manager.textured_lit);
        });
        edge(RenderParticlesLit, RenderParticles, () -> {
            use(state.manager.textured);
        });
        edge(RenderParticles, RenderWeather, () -> {
            updateRenderStage(MCRenderStage.RAIN_SNOW);
            use(state.manager.weather);
        });
        edge(RenderWeather, RenderHand0, () -> {
            ShaderEngine.renderHand();
        });
        edge(RenderHand0, RenderChunk1, () -> {
            updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
            use(state.manager.water);
        });
        edge(RenderChunk1, RenderWeatherEntities1, () -> {
            ShaderState.updateRenderStage(MCRenderStage.ENTITIES);
            use(state.manager.entities);
        });
        edge(RenderChunk1, RenderClouds, () -> {
            shaderData.pushEntity(ShaderEntityData.CLOUDS);
            updateRenderStage(MCRenderStage.CLOUDS);
            use(state.manager.clouds);
        });
        edge(RenderChunk1, RenderLast, () -> {
            updateRenderStage(MCRenderStage.NONE);
            use(state.manager.textured_lit);
        });
        edge(RenderWeatherEntities1, RenderEntities1);
        edge(RenderEntities1, RenderBlockEntities1, () -> {
            ShaderState.updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
            use(state.manager.block);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(RenderBlockEntities1, RenderWeatherEntities1, () -> {
            updateRenderStage(MCRenderStage.ENTITIES);
            use(state.manager.entities);
        });
        edge(RenderBlockEntities1, RenderClouds, () -> {
            shaderData.pushEntity(ShaderEntityData.CLOUDS);
            updateRenderStage(MCRenderStage.CLOUDS);
            use(state.manager.clouds);
        });
        edge(RenderBlockEntities1, RenderLast, () -> {
            updateRenderStage(MCRenderStage.NONE);
            use(state.manager.textured_lit);
        });
        edge(RenderClouds, RenderLast, () -> {
            shaderData.popEntity();

            updateRenderStage(MCRenderStage.NONE);
            use(state.manager.textured_lit);
        });
        edge(RenderLast, RenderBegin, () -> {
            use(null);
            ShaderEngine.clearColorBufs();
        });
    }

    public List<Node> graphLog = new ArrayList<>();

    private Node current = Unmanaged;

    private final StackStateTracker<Void> stack = new StackStateTracker<>(false);

    public void moveTo(Node to) {
        val targets = graph.get(current);
        assert targets != null : "No graph edge source for " + current.name();
        val code = targets.get(to);
        if (code == null) {
            val err = new IllegalStateException("Nonexistent graph edge: " + current.name() + " -> " + to.name());
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        execMove(to, code);
    }

    public void moveToEither(Node... nodes) {
        assert nodes.length >= 1;
        val targets = graph.get(current);
        assert targets != null : "No graph edge source for " + current.name();
        Node to = null;
        Runnable code = null;
        for (val node: nodes) {
            to = node;
            code = targets.get(node);
            if (code != null) {
                break;
            }
        }
        if (code == null) {
            val sb = new StringBuilder("Nonexistent graph edges:\n");
            for (val node: nodes) {
                sb.append(current.name()).append(" -> ").append(node.name()).append('\n');
            }
            val err = new IllegalStateException(sb.toString());
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        execMove(to, code);
    }

    private void execMove(Node to, Runnable code) {
        if (!stack.isEmpty()) {
            var b = new StringBuilder("Tried to move graph when stack was not empty!\nEdge: ").append(current.name())
                                                                                              .append(" -> ")
                                                                                              .append(to.name())
                                                                                              .append('\n');
            stack.addStackState(b);
            val err = new IllegalStateException(b.toString());
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        code.run();
        if (ShaderEngine.DO_GRAPH_LOG) {
            graphLog.add(to);
        }
        current = to;
    }

    public boolean isManaged() {
        return current != Unmanaged;
    }

    public boolean isShadowPass() {
        return current.isShadow;
    }

    public boolean isRender() {
        return current.isRender;
    }

    public boolean isSky() {
        return current.isSky;
    }

    public void push(Stack node) {
        if (current == Unmanaged) {
            val err = new IllegalStateException("Tried to push stack \"" + node.name() + "\" while in unmanaged mode!");
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        if (!node.allowedNodes.contains(current)) {
            var b = new StringBuilder("Tried to push stack \"").append(node.name())
                                                               .append("\" in disallowed graph state \"")
                                                               .append(current.name())
                                                               .append("\".\n");
            b.append("Allowed states:\n");
            for (val allowed : node.allowedNodes) {
                b.append(allowed.name())
                 .append('\n');
            }
            val err = new IllegalStateException(b.toString());
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        stack.push(node.key, null);
        node.push(current);
    }

    public void pop(Stack node) {
        if (current == Unmanaged) {
            val err = new IllegalStateException("Tried to pop stack while in unmanaged mode!");
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        stack.pop(node.key);
        node.pop(current);
    }

    public enum Node {
        Unmanaged,

        BeginFrame,

        ShadowBegin,
        ShadowChunk0,
        ShadowEntities0,
        ShadowBlockEntities0,
        ShadowChunk1,
        ShadowEntities1,
        ShadowBlockEntities1,
        ShadowLast,

        RenderBegin,
        RenderSkyBasic,
        RenderSkyTextured,
        RenderClouds,
        RenderChunk0,
        RenderWeatherEntities0,
        RenderEntities0,
        RenderBlockEntities0,
        RenderSelectionBox,
        RenderBlockDamage,
        RenderParticlesLit,
        RenderParticles,
        RenderWeather,
        RenderHand0,
        RenderChunk1,
        RenderWeatherEntities1,
        RenderEntities1,
        RenderBlockEntities1,
        RenderLast;

        public final boolean isShadow = name().startsWith("Shadow");
        public final boolean isRender = name().startsWith("Render");
        public final boolean isSky = name().startsWith("RenderSky");
    }

    private static final EnumSet<Node> ALL_ALLOWED = EnumSet.allOf(Node.class);

    public enum Stack {
        AABBOutline {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    use(state.manager.basic);
                    pushShader();
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        BlockDestroyProgress {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushRenderStage();
                    pushShader();
                    updateRenderStage(MCRenderStage.TERRAIN_SOLID);
                    use(state.manager.terrain);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popRenderStage();
                    popShader();
                }
            }
        },
        SpiderEyes {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushRenderStage();
                    pushShader();
                    updateRenderStage(MCRenderStage.ENTITIES);
                    use(state.manager.spidereyes);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popRenderStage();
                    popShader();
                }
            }
        },
        EntityParticle {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.textured_lit);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        Text {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.textured_lit);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        DragonAPI {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.textured_lit);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        Portal {
            @Override
            protected void push(Node currentNode) {
                pushRenderStage();
                updateRenderStage(MCRenderStage.BLOCK_ENTITIES_PORTAL);
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.portal);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                popRenderStage();
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        Leash {
            @Override
            protected void push(Node currentNode) {
                pushRenderStage();
                updateRenderStage(MCRenderStage.ENTITIES);
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.basic);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                popRenderStage();
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        NEIOverlay {
            @Override
            protected void push(Node currentNode) {
                pushRenderStage();
                pushShader();
                updateRenderStage(MCRenderStage.NONE);
                use(state.manager.basic);
            }

            @Override
            protected void pop(Node currentNode) {
                popRenderStage();
                popShader();
            }
        },
        ExternalShader {
            @Override
            protected void push(Node currentNode) {
                pushShader();
                use(null);
            }

            @Override
            protected void pop(Node currentNode) {
                popShader();
            }
        },
        Beacon {
            @Override
            protected void push(Node currentNode) {
                pushRenderStage();
                updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.beaconbeam);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                popRenderStage();
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        BlockHighlightTextured {
            @Override
            protected void push(Node currentNode) {
                if (currentNode.isRender) {
                    pushShader();
                    use(state.manager.textured);
                }
            }

            @Override
            protected void pop(Node currentNode) {
                if (currentNode.isRender) {
                    popShader();
                }
            }
        },
        ;

        Stack(Node first, Node... rest) {
            this.key = new StackStateTracker.Key(name());
            this.allowedNodes = EnumSet.of(first, rest);
        }

        Stack() {
            this.key = new StackStateTracker.Key(name());
            this.allowedNodes = ALL_ALLOWED;
        }

        private final StackStateTracker.Key key;
        private final EnumSet<Node> allowedNodes;

        protected void push(Node currentNode) {

        }

        protected void pop(Node currentNode) {

        }
    }
}
