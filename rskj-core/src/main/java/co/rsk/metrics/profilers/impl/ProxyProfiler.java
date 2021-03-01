package co.rsk.metrics.profilers.impl;


import co.rsk.metrics.profilers.Metric;
import co.rsk.metrics.profilers.Profiler;

/**
 * A ProxyProfiler has no logic,
 * if present, it delegates the logic to the inner profiler
 */
public class ProxyProfiler implements Profiler {
    private Profiler innerProfiler;

    @Override
    public Metric start(PROFILING_TYPE type) {
        if (this.innerProfiler != null) {
            return this.innerProfiler.start(type);
        }

        return null;
    }

    @Override
    public void stop(Metric metric) {
        if (this.innerProfiler != null) {
            this.innerProfiler.stop(metric);
        }
    }

    public void setInnerProfiler(Profiler innerProfiler) {
        this.innerProfiler = innerProfiler;
    }
}
