package edu.harvard.mcb.leschziner.pipe;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;

public class ProcessingPipeTask extends DistributedProcessingTask {

    private static final long            serialVersionUID = 2219341028285498238L;

    private final Particle               target;
    private final Vector<ParticleFilter> stages;
    private final String                 processedQueueName;

    public ProcessingPipeTask(Particle target,
                              Vector<ParticleFilter> stages,
                              String processedQueueName,
                              String executorName) {
        super(executorName);
        this.target = target;
        this.stages = stages;
        this.processedQueueName = processedQueueName;
    }

    @Override
    public void process() {
        BlockingQueue<Particle> processedParticles = Hazelcast.getQueue(processedQueueName);

        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
        processedParticles.add(processed);
    }

}
