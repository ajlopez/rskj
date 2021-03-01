package co.rsk.metrics.profilers.impl.full;

import co.rsk.metrics.profilers.Metric;
import co.rsk.metrics.profilers.Profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ExecutionProfiler implements Profiler {

    private ThreadMXBean thread;

    public ExecutionProfiler() {
        thread = ManagementFactory.getThreadMXBean();
        if(thread.isThreadCpuTimeSupported()){
            thread.setThreadCpuTimeEnabled(true);
        }
        //TODO: Add flag to avoid CPU time calculation when not supported
    }

    @Override
    public synchronized Metric start(Profiler.PROFILING_TYPE type) {

        MetricImpl metric = new MetricImpl(type, thread);
        return metric;
    }

    @Override
    public synchronized void stop(Metric metric) {
        ((MetricImpl)metric).setDelta(thread);
    }
}
