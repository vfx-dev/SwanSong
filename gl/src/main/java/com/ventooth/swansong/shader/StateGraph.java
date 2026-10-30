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
import java.util.function.Consumer;

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

// TODO: Do we prefix this with 'shader'?
@SuppressWarnings("Convert2MethodRef")
public class StateGraph {
    private static final EnumMap<Node, EnumMap<Node, Consumer<ShaderEngine<?>>>> graph = new EnumMap<>(Node.class);

    private static void edge(Node from, Node to, @NotNull Consumer<ShaderEngine<?>> logic) {
        val map = graph.computeIfAbsent(from, _from -> new EnumMap<>(Node.class));
        if (map.containsKey(to)) {
            throw new AssertionError("Duplicate graph edge: " + from.name() + " -> " + to.name());
        }
        map.put(to, logic);
    }


    private static void edge(Node from, Node[] to, @NotNull Consumer<ShaderEngine<?>> logic) {
        for (val b : to) {
            edge(from, b, logic);
        }
    }

    private static void edge(Node[] from, Node to, @NotNull Consumer<ShaderEngine<?>> logic) {
        for (val a : from) {
            edge(a, to, logic);
        }
    }

    private static void edge(Node[] from, Node[] to, @NotNull Consumer<ShaderEngine<?>> logic) {
        for (val a : from) {
            for (val b : to) {
                edge(a, b, logic);
            }
        }
    }

    private static void edge(Node from, Node to) {
        edge(from, to, e -> {});
    }

    //Mirrors the layout of MANAGED_STATE.adoc
    static {
        edge(Unmanaged, BeginFrame);
        edge(BeginFrame, ShadowBegin, e -> {
            e.use(e.fixed.manager.shadow);
            e.lockShader();
        });
        edge(BeginFrame, RenderBegin);
        edge(RenderLast, Unmanaged);

        edge(ShadowLast, RenderBegin, e -> {
            e.unlockShader();
        });

        edge(ShadowBegin, ShadowChunk0, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
        });
        edge(ShadowChunk0, ShadowEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowChunk0, ShadowChunk1, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
        });
        edge(ShadowEntities0, ShadowBlockEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
        });
        edge(ShadowBlockEntities0, ShadowEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowBlockEntities0, ShadowChunk1, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
        });
        edge(ShadowChunk1, ShadowEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowChunk1, ShadowLast);
        edge(ShadowEntities1, ShadowBlockEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(ShadowBlockEntities1, ShadowEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
        });
        edge(ShadowBlockEntities1, ShadowLast);

        edge(RenderBegin, RenderSkyBasic, e -> {
            e.shaderData.pushEntity(ShaderEntityData.SKY);
            e.state().updateRenderStage(MCRenderStage.SKY);
            e.use(e.fixed.manager.skybasic);
        });
        edge(RenderBegin, RenderClouds, e -> {
            e.shaderData.pushEntity(ShaderEntityData.CLOUDS);
            e.state().updateRenderStage(MCRenderStage.CLOUDS);
            e.use(e.fixed.manager.clouds);
        });
        edge(RenderBegin, RenderChunk0, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            e.use(e.fixed.manager.terrain);
        });
        edge(new Node[]{RenderSkyBasic, RenderSkyTextured}, RenderClouds, e -> {
            e.shaderData.popEntity();

            e.shaderData.pushEntity(ShaderEntityData.CLOUDS);
            e.state().updateRenderStage(MCRenderStage.CLOUDS);
            e.use(e.fixed.manager.clouds);
        });
        edge(new Node[]{RenderSkyBasic, RenderSkyTextured}, RenderChunk0, e -> {
            e.shaderData.popEntity();

            e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            e.use(e.fixed.manager.terrain);
        });
        edge(RenderSkyBasic, RenderSkyBasic);
        edge(RenderSkyBasic, RenderSkyTextured, e -> e.use(e.fixed.manager.skytextured));
        edge(RenderSkyTextured, RenderSkyBasic, e -> e.use(e.fixed.manager.skybasic));
        edge(RenderSkyTextured, RenderSkyTextured);

        edge(RenderClouds, RenderChunk0, e -> {
            e.shaderData.popEntity();
            e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            e.use(e.fixed.manager.terrain);
        });
        edge(RenderChunk0, RenderWeatherEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
            e.use(e.fixed.manager.entities);
        });
        edge(RenderChunk0, RenderSelectionBox, e -> {
            e.state().updateRenderStage(MCRenderStage.NONE);
            e.use(e.fixed.manager.basic);
        });
        edge(RenderWeatherEntities0, RenderEntities0);
        edge(RenderEntities0, RenderBlockEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
            e.use(e.fixed.manager.block);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(RenderBlockEntities0, RenderWeatherEntities0, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
            e.use(e.fixed.manager.entities);
        });
        edge(RenderBlockEntities0, RenderSelectionBox, e -> {
            e.state().updateRenderStage(MCRenderStage.NONE);
            e.use(e.fixed.manager.basic);
        });
        edge(RenderSelectionBox, RenderBlockDamage, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
            e.use(e.fixed.manager.terrain);
        });
        edge(RenderBlockDamage, RenderParticlesLit, e -> {
            e.state().updateRenderStage(MCRenderStage.PARTICLES);
            e.use(e.fixed.manager.textured_lit);
        });
        edge(RenderParticlesLit, RenderParticles, e -> {
            e.use(e.fixed.manager.textured);
        });
        edge(RenderParticles, RenderWeather, e -> {
            e.state().updateRenderStage(MCRenderStage.RAIN_SNOW);
            e.use(e.fixed.manager.weather);
        });
        edge(RenderWeather, RenderHand0, e -> {
            e.renderHand();
        });
        edge(RenderHand0, RenderChunk1, e -> {
            e.state().updateRenderStage(MCRenderStage.TERRAIN_TRANSLUCENT);
            e.use(e.fixed.manager.water);
        });
        edge(RenderChunk1, RenderWeatherEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
            e.use(e.fixed.manager.entities);
        });
        edge(RenderChunk1, RenderClouds, e -> {
            e.shaderData.pushEntity(ShaderEntityData.CLOUDS);
            e.state().updateRenderStage(MCRenderStage.CLOUDS);
            e.use(e.fixed.manager.clouds);
        });
        edge(RenderChunk1, RenderLast, e -> {
            e.state().updateRenderStage(MCRenderStage.NONE);
            e.use(e.fixed.manager.textured_lit);
        });
        edge(RenderWeatherEntities1, RenderEntities1);
        edge(RenderEntities1, RenderBlockEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
            e.use(e.fixed.manager.block);
        });
        //Can happen (CNPC+ DBC Addon is one example)
        edge(RenderBlockEntities1, RenderWeatherEntities1, e -> {
            e.state().updateRenderStage(MCRenderStage.ENTITIES);
            e.use(e.fixed.manager.entities);
        });
        edge(RenderBlockEntities1, RenderClouds, e -> {
            e.shaderData.pushEntity(ShaderEntityData.CLOUDS);
            e.state().updateRenderStage(MCRenderStage.CLOUDS);
            e.use(e.fixed.manager.clouds);
        });
        edge(RenderBlockEntities1, RenderLast, e -> {
            e.state().updateRenderStage(MCRenderStage.NONE);
            e.use(e.fixed.manager.textured_lit);
        });
        edge(RenderClouds, RenderLast, e -> {
            e.shaderData.popEntity();

            e.state().updateRenderStage(MCRenderStage.NONE);
            e.use(e.fixed.manager.textured_lit);
        });
        edge(RenderLast, RenderBegin, e -> {
            e.use(null);
            e.clearColorBufs();
        });
    }

    private final ShaderEngine<?> engine;

    StateGraph(ShaderEngine<?> engine) {
        this.engine = engine;
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
        Consumer<ShaderEngine<?>> code = null;
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

    private void execMove(Node to, Consumer<ShaderEngine<?>> code) {
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
        code.accept(engine);
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
        node.push(engine, current);
    }

    public void pop(Stack node) {
        if (current == Unmanaged) {
            val err = new IllegalStateException("Tried to pop stack while in unmanaged mode!");
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        stack.pop(node.key);
        node.pop(engine, current);
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
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.use(e.fixed.manager.basic);
                    e.pushShader();
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        BlockDestroyProgress {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.state().pushRenderStage();
                    e.pushShader();
                    e.state().updateRenderStage(MCRenderStage.TERRAIN_SOLID);
                    e.use(e.fixed.manager.terrain);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.state().popRenderStage();
                    e.popShader();
                }
            }
        },
        SpiderEyes {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.state().pushRenderStage();
                    e.pushShader();
                    e.state().updateRenderStage(MCRenderStage.ENTITIES);
                    e.use(e.fixed.manager.spidereyes);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.state().popRenderStage();
                    e.popShader();
                }
            }
        },
        EntityParticle {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.textured_lit);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        Text {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.textured_lit);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        DragonAPI {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.textured_lit);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        Leash {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                e.state().pushRenderStage();
                e.state().updateRenderStage(MCRenderStage.ENTITIES);
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.basic);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                e.state().popRenderStage();
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        NEIOverlay {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                e.state().pushRenderStage();
                e.pushShader();
                e.state().updateRenderStage(MCRenderStage.NONE);
                e.use(e.fixed.manager.basic);
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                e.state().popRenderStage();
                e.popShader();
            }
        },
        ExternalShader {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                e.pushShader();
                e.use(null);
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                e.popShader();
            }
        },
        Beacon {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                e.state().pushRenderStage();
                e.state().updateRenderStage(MCRenderStage.BLOCK_ENTITIES);
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.beaconbeam);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                e.state().popRenderStage();
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        },
        BlockHighlightTextured {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.textured);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
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

        protected void push(ShaderEngine<?> e, Node currentNode) {

        }

        protected void pop(ShaderEngine<?> e, Node currentNode) {

        }
    }
}
