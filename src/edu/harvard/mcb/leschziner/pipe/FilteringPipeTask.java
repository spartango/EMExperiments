package edu.harvard.mcb.leschziner.pipe;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class FilteringPipeTask extends ParticlePipeTask {

    private static final long            serialVersionUID = 2219341028285498238L;

    private final Vector<ParticleFilter> stages;

    public FilteringPipeTask(Particle target,
                             Vector<ParticleFilter> stages,
                             String processedQueueName,
                             String executorName) {
        super(target, processedQueueName, executorName);
        this.stages = stages;
    }

    @Override public void process() {
        BlockingQueue<Particle> processedParticles = Hazelcast.getQueue(processedQueueName);

        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
        processedParticles.add(processed);
    }

}
