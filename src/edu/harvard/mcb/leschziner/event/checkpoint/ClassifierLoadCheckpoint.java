package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.core.ParticleClassifier;
import edu.harvard.mcb.leschziner.event.Checkpoint;

public class ClassifierLoadCheckpoint extends Checkpoint {

    protected ParticleClassifier classifier;
    protected boolean            classified;

    public ClassifierLoadCheckpoint(ParticleClassifier classifier) {
        super();
        this.classifier = classifier;
        classified = false;
    }

    @Override public void onReached() {
        reached = true;
        System.out.println("[" + this + "]: Classifying " + this.completions);
        classifier.classifyAll();
        classified = true;
    }

    public boolean hasClassified() {
        return classified;
    }
}
