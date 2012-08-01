package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.event.Checkpoint;

public class ClassifierLoadCheckpoint extends Checkpoint {

    protected ParticleClassifier classifier;

    public ClassifierLoadCheckpoint(ParticleClassifier classifier) {
        super();
        this.classifier = classifier;
    }

    @Override public void onReached() {
        classifier.classifyAll();
    }
}
