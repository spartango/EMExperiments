package edu.harvard.mcb.leschziner.event.checkpoint;

import edu.harvard.mcb.leschziner.event.Checkpoint;
import edu.harvard.mcb.leschziner.load.ClassUploader;

public class ClassifierRunCheckpoint extends Checkpoint {
    private final ClassUploader uploader;
    private boolean             uploaded;

    public ClassifierRunCheckpoint(ClassUploader uploader) {
        super();
        this.uploader = uploader;
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
