package edu.harvard.mcb.leschziner.event;

public class CompletionEvent extends ProcessingEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -7588235863943443906L;

    protected final long      runTime;

    public CompletionEvent(String className, long runTime) {
        super(className);
        this.runTime = runTime;
    }

    public long getRunTime() {
        return runTime;
    }

}
