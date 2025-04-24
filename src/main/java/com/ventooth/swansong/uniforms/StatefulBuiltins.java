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

import com.falsepattern.lib.util.MathUtil;
import lombok.SneakyThrows;
import lombok.val;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

public class StatefulBuiltins {
    public static final UniformFunctionRegistry REGISTRY = register();

    @SneakyThrows
    private static UniformFunctionRegistry register() {
        val klass = StatefulBuiltins.class;
        val reg = new UniformFunctionRegistry.Single();
        reg.statefulIndexed(klass.getDeclaredMethod("random", int.class));
        reg.statefulIndexed(klass.getDeclaredMethod("random", int.class, double.class, double.class));
        reg.statefulIndexed(klass.getDeclaredMethod("randomInt", int.class));
        reg.statefulIndexed(klass.getDeclaredMethod("randomInt", int.class, int.class, int.class));
        reg.statefulIndexed(klass.getDeclaredMethod("smooth", int.class, double.class, double.class, double.class));
        return reg;
    }

    private static final BitSet randDB = new BitSet();
    private static double[] randDD = new double[32];

    private static final BitSet randIB = new BitSet();
    private static int[] randID = new int[32];

    private static final BitSet smoothB = new BitSet();
    private static double[] smoothVal = new double[32];
    private static long[] smoothNs = new long[32];

    private static final Random rng = new Random();

    public static void update() {
        resetRands();
    }

    public static void reset() {
        resetRands();
        smoothB.clear();
    }

    public static double random(int index) {
        if (randDB.get(index)) {
            return randDD[index];
        }
        randDB.set(index);
        double value = rng.nextDouble();
        if (randDD.length <= index) {
            randDD = Arrays.copyOf(randDD, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
        }
        randDD[index] = value;
        return value;
    }

    public static double random(int index, double min, double max) {
        if (randDB.get(index)) {
            return randDD[index];
        }
        randDB.set(index);
        double value = min + (max - min) * rng.nextDouble();
        if (randDD.length <= index) {
            randDD = Arrays.copyOf(randDD, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
        }
        randDD[index] = value;
        return value;
    }

    public static int randomInt(int index) {
        if (randIB.get(index)) {
            return randID[index];
        }
        randIB.set(index);
        int value = rng.nextInt();
        if (randID.length <= index) {
            randID = Arrays.copyOf(randID, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
        }
        randID[index] = value;
        return value;
    }

    public static int randomInt(int index, int min, int max) {
        if (randIB.get(index)) {
            return randID[index];
        }
        randIB.set(index);
        int value = min + rng.nextInt(max - min);
        if (randID.length <= index) {
            randID = Arrays.copyOf(randID, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
        }
        randID[index] = value;
        return value;
    }

    public static double smooth(int index, double value, double fadeUpTime, double fadeDownTime) {
        long time = System.nanoTime();
        long timePrev;
        double valPrev;
        if (smoothB.get(index)) {
            timePrev = smoothNs[index];
            valPrev = smoothVal[index];
        } else {
            smoothB.set(index);
            if (smoothNs.length <= index) {
                smoothNs = Arrays.copyOf(smoothNs, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
            }
            if (smoothVal.length <= index) {
                smoothVal = Arrays.copyOf(smoothVal, MathUtil.smallestEncompassingPowerOfTwo(index + 1));
            }
            timePrev = time;
            valPrev = value;
        }
        long timeDeltaNanos = time - timePrev;
        double timeDeltaSec = (double) timeDeltaNanos / 1_000_000_000d;
        double timeFadeSec = value >= valPrev ? fadeUpTime : fadeDownTime;
        double valSmooth = getSmoothValue(valPrev, value, timeDeltaSec, timeFadeSec);
        smoothNs[index] = time;
        smoothVal[index] = valSmooth;
        return valSmooth;
    }

    private static void resetRands() {
        randDB.clear();
        randIB.clear();
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
                kTime = MathUtil.clamp(kTime, 0.0, 1.0);
                valSmooth = valPrev + valDelta * kTime;
            } else {
                valSmooth = value;
            }

            return valSmooth;
        }
    }
}
