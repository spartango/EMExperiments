package edu.harvard.mcb.leschziner.pipe;

import java.io.Serializable;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleFilter;

public class ProcessingPipeTask implements Runnable, Serializable {

    private Particle               target;
    private Vector<ParticleFilter> stages;

    @Override
    public void run() {
        Particle processed = target;
        // Apply each filter
        for (ParticleFilter stage : stages) {
            processed = stage.filter(processed);
        }
    }

}
