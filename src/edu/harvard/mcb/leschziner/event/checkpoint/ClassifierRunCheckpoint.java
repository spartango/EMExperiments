package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.classify.DistributedClassifier;
import edu.harvard.mcb.leschziner.event.Checkpoint;
import edu.harvard.mcb.leschziner.load.ClassUploader;

public class ClassifierRunCheckpoint extends Checkpoint {
    private final ClassUploader         uploader;
    private final DistributedClassifier classifier;
    private boolean                     uploaded;

    public ClassifierRunCheckpoint(ClassUploader uploader,
                                   DistributedClassifier classifier) {
        super();
        this.uploader = uploader;
        this.classifier = classifier;
        uploaded = false;
    }

    @Override public void onReached() {
        reached = true;
        System.out.println("[" + this + "]: Uploading Classes");
        uploader.uploadAll();
        uploaded = true;
        System.out.println("[" + this + "]: Upload complete");

    }

    public boolean hasUploaded() {
        return uploaded;
    }

}
