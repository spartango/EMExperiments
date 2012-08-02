package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.classify.DistributedClassifier;
import edu.harvard.mcb.leschziner.event.Checkpoint;

public class ClassifierLoadCheckpoint extends Checkpoint {

    protected DistributedClassifier classifier;
    protected Checkpoint            runCheckpoint;
    protected boolean               classifying;

    public ClassifierLoadCheckpoint(DistributedClassifier classifier) {
        super();
        this.classifier = classifier;
        classifying = false;
    }

    @Override public void onReached() {
        reached = true;
        setDependentExpectations(1);

        if (runCheckpoint != null) {
            // Tell the run checkpoint to start listening for completion events
            runCheckpoint.setEventSource(classifier);
        }

        System.out.println("[" + this + "]: Classifying " + this.completions);
        // Start the classification
        classifier.classifyAll();
        classifying = true;

    }

    public boolean isClassifying() {
        return classifying;
    }

    public void setNextCheckpoint(Checkpoint runCheckpoint) {
        this.runCheckpoint = runCheckpoint;
    }
}
