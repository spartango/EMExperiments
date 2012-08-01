package edu.harvard.mcb.leschziner.pipe;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleGenerator;
import edu.harvard.mcb.leschziner.storage.DefaultStorageEngine;

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
        BlockingQueue<Particle> processedParticles = DefaultStorageEngine.getStorageEngine()
                                                                         .getQueue(processedQueueName);

        Collection<Particle> generated = new LinkedList<Particle>();
        generated.add(target);
        for (ParticleGenerator stage : stages) {
            generated = stage.generate(generated);
        }
        processedParticles.addAll(generated);

        outputCount = generated.size();
    }

}
