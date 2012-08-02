package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.classify.DistributedClassifier;
import edu.harvard.mcb.leschziner.event.Checkpoint;

public class ClassifierLoadCheckpoint extends Checkpoint {

    protected DistributedClassifier classifier;
    protected boolean               classifying;

    public ClassifierLoadCheckpoint(DistributedClassifier classifier) {
        super();
        this.classifier = classifier;
        classifying = false;
    }

    @Override public void onReached() {
        reached = true;
        setDependentExpectations(1);
        classifier.getEventQueue().removeItemListener(this);
        System.out.println("[" + this + "]: Classifying " + this.completions);
        // Start the classification
        classifier.classifyAll();
        classifying = true;

    }

    public boolean isClassifying() {
        return classifying;
    }

}
