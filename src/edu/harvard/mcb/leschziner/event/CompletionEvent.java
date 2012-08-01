package edu.harvard.mcb.leschziner.event;

public class CompletionEvent extends ProcessingEvent {

    /**
     * 
     */
    private static final long serialVersionUID = -7588235863943443906L;

    protected final long      runTime;
    protected final long      outputCount;

    public CompletionEvent(String className, long runTime) {
        this(className, runTime, 1);
    }

    public CompletionEvent(String className, long runTime, long outputCount) {
        super(className);
        this.runTime = runTime;
        this.outputCount = outputCount;
    }

    public long getRunTime() {
        return runTime;
    }

    public long getOutputCount() {
        return outputCount;
    }

}
