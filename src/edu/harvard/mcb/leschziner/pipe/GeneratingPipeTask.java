package edu.harvard.mcb.leschziner.pipe;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import com.hazelcast.core.Hazelcast;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleGenerator;

public class GeneratingPipeTask extends ParticlePipeTask {
    private static final long               serialVersionUID = 638400970037283314L;

    private final Vector<ParticleGenerator> stages;

    public GeneratingPipeTask(Particle target,
                              Vector<ParticleGenerator> stages,
                              String processedQueueName,
                              String executorName) {
        super(target, processedQueueName, executorName);
        this.stages = stages;
    }

    @Override public void process() {
        BlockingQueue<Particle> processedParticles = Hazelcast.getQueue(processedQueueName);

        Collection<Particle> generated = null;
        for (ParticleGenerator stage : stages) {
            if (generated == null) {
                generated = stage.generate(target);
            } else {
                generated = stage.generate(generated);
            }
        }
        processedParticles.addAll(generated);
    }

}
