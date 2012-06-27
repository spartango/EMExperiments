package edu.harvard.mcb.leschziner.pipe;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class ProcessingPipeTask implements Runnable, Serializable {

    public ProcessingPipeTask(Particle target,
                              Vector<ParticleFilter> stages,
                              String processedQueueName,
                              String executorName) {
        super();
        this.target = target;
        this.stages = stages;
        this.processedQueueName = processedQueueName;
        this.executorName = executorName;
    }

    private final Particle               target;
    private final Vector<ParticleFilter> stages;
    private final String                 processedQueueName;
    private final String                 executorName;

    @Override
    public void run() {
        BlockingQueue<Particle> processedParticles = Hazelcast.getQueue(processedQueueName);

        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
        processedParticles.add(processed);
        Hazelcast.getAtomicNumber(executorName).decrementAndGet();

    }

}
