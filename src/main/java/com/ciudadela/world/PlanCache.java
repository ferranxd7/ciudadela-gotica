package com.ciudadela.world;

import com.ciudadela.gen.CastlePlan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/** Cache de planos (cada plano se calcula una sola vez y se comparte entre hilos de generacion). */
public final class PlanCache {
    private static final int MAX = 6;
    private static final Map<String, CastlePlan> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CastlePlan> e) {
            return size() > MAX;
        }
    };

    private PlanCache() {}

    public static synchronized CastlePlan get(long seed, int x, int z, IntBinaryOperator ground) {
        String k = seed + ":" + x + ":" + z;
        CastlePlan p = CACHE.get(k);
        if (p == null) {
            p = CastlePlan.create(seed ^ ((long) x * 31L + z), x, z, ground);
            CACHE.put(k, p);
        }
        return p;
    }
}
