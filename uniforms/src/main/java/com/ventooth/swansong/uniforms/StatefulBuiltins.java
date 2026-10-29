/*
 * Swansong
 *
 * Copyright 2025 Ven, FalsePattern
 *
 * This software is licensed under the Open Software License version
 * 3.0. The full text of this license can be found in https://opensource.org/licenses/OSL-3.0
 * or in the LICENSES directory which is distributed along with the software.
 */

package com.ventooth.swansong.uniforms;

import com.ventooth.swansong.util.MathUtils;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

// See: https://shaders.properties/current/reference/shadersproperties/custom_uniforms/#functions
@NoArgsConstructor
public class StatefulBuiltins implements UniformFunctionProvider {
    public static final String NAME = "stateful_builtins";

    private final BitSet randDB = new BitSet();
    private double[] randDD = new double[32];

    private final BitSet randIB = new BitSet();
    private int[] randID = new int[32];

    private final SmoothBank smoothImplicit = new SmoothBank();
    private final SmoothBank smoothExplicit = new SmoothBank();

    private final Random rng = new Random();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    @SneakyThrows
    public void init(UniformFunctionRegistry cfg) {
        val klass = StatefulBuiltins.class;
        cfg.idxImplicit(klass.getDeclaredMethod("random", int.class));
        cfg.idxImplicit(klass.getDeclaredMethod("random", int.class, double.class, double.class));
        cfg.idxImplicit(klass.getDeclaredMethod("randomInt", int.class));
        cfg.idxImplicit(klass.getDeclaredMethod("randomInt", int.class, int.class, int.class));
        cfg.idxImplicit(klass.getDeclaredMethod("smooth", int.class, double.class));
        cfg.idxImplicit(klass.getDeclaredMethod("smooth", int.class, double.class, double.class));
        cfg.idxImplicit(klass.getDeclaredMethod("smooth", int.class, double.class, double.class, double.class));
        cfg.idxExplicit(klass.getDeclaredMethod("smoothExplicit", int.class, double.class, double.class, double.class), "smooth");
    }

    @Override
    public void update() {
        resetRands();
    }

    @Override
    public void reset() {
        resetRands();
        smoothImplicit.clear();
        smoothExplicit.clear();
    }

    public double random(int index) {
        if (randDB.get(index)) {
            return randDD[index];
        }
        randDB.set(index);
        double value = rng.nextDouble();
        if (randDD.length <= index) {
            randDD = Arrays.copyOf(randDD, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
        }
        randDD[index] = value;
        return value;
    }

    public double random(int index, double min, double max) {
        if (randDB.get(index)) {
            return randDD[index];
        }
        randDB.set(index);
        double value = min + (max - min) * rng.nextDouble();
        if (randDD.length <= index) {
            randDD = Arrays.copyOf(randDD, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
        }
        randDD[index] = value;
        return value;
    }

    public int randomInt(int index) {
        if (randIB.get(index)) {
            return randID[index];
        }
        randIB.set(index);
        int value = rng.nextInt();
        if (randID.length <= index) {
            randID = Arrays.copyOf(randID, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
        }
        randID[index] = value;
        return value;
    }

    public int randomInt(int index, int min, int max) {
        if (randIB.get(index)) {
            return randID[index];
        }
        randIB.set(index);
        int value = min + rng.nextInt(max - min);
        if (randID.length <= index) {
            randID = Arrays.copyOf(randID, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
        }
        randID[index] = value;
        return value;
    }

    public double smooth(int index, double value) {
        return smoothImplicit.eval(index, value, 1.0, 1.0);
    }

    public double smooth(int index, double value, double fadeTime) {
        return smoothImplicit.eval(index, value, fadeTime, fadeTime);
    }

    public double smooth(int index, double value, double fadeUpTime, double fadeDownTime) {
        return smoothImplicit.eval(index, value, fadeUpTime, fadeDownTime);
    }

    public double smoothExplicit(int index, double value, double fadeUpTime, double fadeDownTime) {
        return smoothExplicit.eval(index, value, fadeUpTime, fadeDownTime);
    }

    private void resetRands() {
        randDB.clear();
        randIB.clear();
    }

    private static final class SmoothBank {
        private final BitSet init = new BitSet();
        private double[] val = new double[32];
        private long[] ns = new long[32];

        void clear() {
            init.clear();
        }

        double eval(int index, double value, double fadeUpTime, double fadeDownTime) {
            long time = System.nanoTime();
            long timePrev;
            double valPrev;
            if (init.get(index)) {
                timePrev = ns[index];
                valPrev = val[index];
            } else {
                init.set(index);
                if (ns.length <= index) {
                    ns = Arrays.copyOf(ns, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
                }
                if (val.length <= index) {
                    val = Arrays.copyOf(val, MathUtils.smallestEncompassingPowerOfTwo(index + 1));
                }
                timePrev = time;
                valPrev = value;
            }
            long timeDeltaNanos = time - timePrev;
            double timeDeltaSec = (double) timeDeltaNanos / 1_000_000_000d;
            double timeFadeSec = value >= valPrev ? fadeUpTime : fadeDownTime;
            double valSmooth = getSmoothValue(valPrev, value, timeDeltaSec, timeFadeSec);
            ns[index] = time;
            val[index] = valSmooth;
            return valSmooth;
        }
    }

    private static double getSmoothValue(double valPrev, double value, double timeDeltaSec, double timeFadeSec) {
        if (timeDeltaSec <= 0.0) {
            return valPrev;
        } else {
            double valDelta = value - valPrev;
            double valSmooth;
            if (timeFadeSec > 0.0 && timeDeltaSec < timeFadeSec && Math.abs(valDelta) > 1.0E-6) {
                double countUpdates = timeFadeSec / timeDeltaSec;
                double k1 = 4.61;
                double k2 = 0.13;
                double k3 = 10.0;
                double kCorr = k1 - 1.0 / (k2 + countUpdates / k3);
                double kTime = timeDeltaSec / timeFadeSec * kCorr;
                kTime = MathUtils.clamp(kTime, 0.0, 1.0);
                valSmooth = valPrev + valDelta * kTime;
            } else {
                valSmooth = value;
            }

            return valSmooth;
        }
    }
}
