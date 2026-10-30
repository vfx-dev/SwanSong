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
import com.ventooth.swansong.shader.shaderobjects.ManagedShader;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

import static com.ventooth.swansong.shader.StateGraph.Node.BeginFrame;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderBegin;
import static com.ventooth.swansong.shader.StateGraph.Node.RenderLast;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowBegin;
import static com.ventooth.swansong.shader.StateGraph.Node.ShadowLast;
import static com.ventooth.swansong.shader.StateGraph.Node.Unmanaged;

// TODO: Do we prefix this with 'shader'?
@SuppressWarnings("Convert2MethodRef")
public class StateGraph {
    private final ShaderEngine<?> engine;
    private final Order order;

    StateGraph(ShaderEngine<?> engine, Order order) {
        this.engine = engine;
        this.order = order;
    }

    public List<Node> graphLog = new ArrayList<>();

    private Node current = Unmanaged;

    private final StackStateTracker<Void> stack = new StackStateTracker<>(false);

    public void moveTo(Node to) {
        if (!order.allows(current, to)) {
            val err = new IllegalStateException("Nonexistent graph edge: " + current.name() + " -> " + to.name());
            Share.log.fatal("StateGraph: ", err);
            throw err;
        }
        execMove(to);
    }

    public void moveToEither(Node... nodes) {
        assert nodes.length >= 1;
        for (val node : nodes) {
            if (order.allows(current, node)) {
                execMove(node);
                return;
            }
        }
        val sb = new StringBuilder("Nonexistent graph edges:\n");
        for (val node : nodes) {
            sb.append(current.name())
              .append(" -> ")
              .append(node.name())
              .append('\n');
        }
        val err = new IllegalStateException(sb.toString());
        Share.log.fatal("StateGraph: ", err);
        throw err;
    }

    private void execMove(Node to) {
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
        if (to != current) {
            current.exit(engine, to);
            to.enter(engine, current);
        }
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

    public static final class Order {
        private final EnumMap<Node, EnumSet<Node>> edges = new EnumMap<>(Node.class);

        public static Order frame() {
            return new Order().edge(Unmanaged, BeginFrame)
                              .edge(BeginFrame, ShadowBegin, RenderBegin)
                              .edge(ShadowLast, RenderBegin)
                              .edge(RenderLast, Unmanaged, RenderBegin);
        }

        public Order edge(Node from, Node... to) {
            edges.computeIfAbsent(from, f -> EnumSet.noneOf(Node.class))
                 .addAll(Arrays.asList(to));
            return this;
        }

        boolean allows(Node from, Node to) {
            val targets = edges.get(from);
            return targets != null && targets.contains(to);
        }
    }

    public enum Node {
        Unmanaged,

        BeginFrame,

        ShadowBegin {
            @Override
            void enter(ShaderEngine<?> e, Node from) {
                e.use(e.fixed.manager.shadow);
                e.lockShader();
            }
        },
        ShadowChunk0(MCRenderStage.TERRAIN_SOLID),
        ShadowEntities0(MCRenderStage.ENTITIES),
        ShadowBlockEntities0(MCRenderStage.BLOCK_ENTITIES),
        ShadowChunk1(MCRenderStage.TERRAIN_TRANSLUCENT),
        ShadowEntities1(MCRenderStage.ENTITIES),
        ShadowBlockEntities1(MCRenderStage.BLOCK_ENTITIES),
        ShadowLast {
            @Override
            void exit(ShaderEngine<?> e, Node to) {
                e.unlockShader();
            }
        },

        RenderBegin,
        RenderSkyBasic {
            @Override
            void enter(ShaderEngine<?> e, Node from) {
                enterSky(e, from);
                e.use(e.fixed.manager.skybasic);
            }

            @Override
            void exit(ShaderEngine<?> e, Node to) {
                exitSky(e, to);
            }
        },
        RenderSkyTextured {
            @Override
            void enter(ShaderEngine<?> e, Node from) {
                enterSky(e, from);
                e.use(e.fixed.manager.skytextured);
            }

            @Override
            void exit(ShaderEngine<?> e, Node to) {
                exitSky(e, to);
            }
        },
        RenderClouds {
            @Override
            void enter(ShaderEngine<?> e, Node from) {
                e.shaderData.pushEntity(ShaderEntityData.CLOUDS);
                e.state()
                 .updateRenderStage(MCRenderStage.CLOUDS);
                e.use(e.fixed.manager.clouds);
            }

            @Override
            void exit(ShaderEngine<?> e, Node to) {
                e.shaderData.popEntity();
            }
        },
        RenderChunk0(MCRenderStage.TERRAIN_SOLID, b -> b.terrain),
        RenderWeatherEntities0(MCRenderStage.ENTITIES, b -> b.entities),
        RenderEntities0,
        RenderBlockEntities0(MCRenderStage.BLOCK_ENTITIES, b -> b.block),
        RenderSelectionBox(MCRenderStage.NONE, b -> b.basic),
        RenderBlockDamage(MCRenderStage.TERRAIN_SOLID, b -> b.terrain),
        RenderParticlesLit(MCRenderStage.PARTICLES, b -> b.textured_lit),
        RenderParticles(null, b -> b.textured),
        RenderWeather(MCRenderStage.RAIN_SNOW, b -> b.weather),
        RenderHand0 {
            @Override
            void enter(ShaderEngine<?> e, Node from) {
                e.renderHand();
            }
        },
        RenderChunk1(MCRenderStage.TERRAIN_TRANSLUCENT, b -> b.water),
        RenderWeatherEntities1(MCRenderStage.ENTITIES, b -> b.entities),
        RenderEntities1,
        RenderBlockEntities1(MCRenderStage.BLOCK_ENTITIES, b -> b.block),
        RenderLast(MCRenderStage.NONE, b -> b.textured_lit) {
            @Override
            void exit(ShaderEngine<?> e, Node to) {
                if (to == RenderBegin) {
                    e.use(null);
                    e.clearColorBufs();
                }
            }
        };

        public final boolean isShadow = name().startsWith("Shadow");
        public final boolean isRender = name().startsWith("Render");
        public final boolean isSky = name().startsWith("RenderSky");

        private final @Nullable MCRenderStage stage;
        private final @Nullable Function<ShaderBinding, ManagedShader> program;

        Node() {
            this(null, null);
        }

        Node(MCRenderStage stage) {
            this(stage, null);
        }

        Node(@Nullable MCRenderStage stage, @Nullable Function<ShaderBinding, ManagedShader> program) {
            this.stage = stage;
            this.program = program;
        }

        void enter(ShaderEngine<?> e, Node from) {
            if (stage != null) {
                e.state()
                 .updateRenderStage(stage);
            }
            if (program != null) {
                e.use(program.apply(e.fixed.manager));
            }
        }

        void exit(ShaderEngine<?> e, Node to) {
        }

        private static void enterSky(ShaderEngine<?> e, Node from) {
            if (!from.isSky) {
                e.shaderData.pushEntity(ShaderEntityData.SKY);
                e.state()
                 .updateRenderStage(MCRenderStage.SKY);
            }
        }

        private static void exitSky(ShaderEngine<?> e, Node to) {
            if (!to.isSky) {
                e.shaderData.popEntity();
            }
        }
    }

    public abstract static class Stack {
        public static final Stack AABBOutline = new Stack("AABBOutline") {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.pushShader();
                    e.use(e.fixed.manager.basic);
                }
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                if (currentNode.isRender) {
                    e.popShader();
                }
            }
        };

        public static final Stack BlockDestroyProgress = new Stack("BlockDestroyProgress") {
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
        };

        public static final Stack SpiderEyes = new Stack("SpiderEyes") {
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
        };

        public static final Stack EntityParticle = new Stack("EntityParticle") {
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
        };

        public static final Stack Text = new Stack("Text") {
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
        };

        public static final Stack Leash = new Stack("Leash") {
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
        };

        public static final Stack ExternalShader = new Stack("ExternalShader") {
            @Override
            protected void push(ShaderEngine<?> e, Node currentNode) {
                e.pushShader();
                e.use(null);
            }

            @Override
            protected void pop(ShaderEngine<?> e, Node currentNode) {
                e.popShader();
            }
        };

        public static final Stack Beacon = new Stack("Beacon") {
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
        };

        public static final Stack BlockHighlightTextured = new Stack("BlockHighlightTextured") {
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
        };

        private final String name;
        private final StackStateTracker.Key key;

        protected Stack(String name) {
            this.name = name;
            this.key = new StackStateTracker.Key(name);
        }

        public String name() {
            return name;
        }

        protected void push(ShaderEngine<?> e, Node currentNode) {
        }

        protected void pop(ShaderEngine<?> e, Node currentNode) {
        }

        protected static void use(ShaderEngine<?> e, Function<ShaderBinding, ManagedShader> program) {
            e.use(program.apply(e.fixed.manager));
        }

        protected static void pushShader(ShaderEngine<?> e) {
            e.pushShader();
        }

        protected static void popShader(ShaderEngine<?> e) {
            e.popShader();
        }
    }
}
