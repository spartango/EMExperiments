package edu.harvard.mcb.leschziner.event;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

import edu.harvard.mcb.leschziner.distributed.DistributedTaskHandler;

/**
 * Represents a point in a pipeline's progression, as measured by the posting of
 * specific events in particular numbers
 * 
 * @author spartango
 * 
 */
public class Checkpoint implements ItemListener<ProcessingEvent> {
    protected long                           completions;
    protected long                           expectedCompletions;
    protected long                           totalOutput;
    protected long                           totalRuntime;
    protected final Collection<ErrorEvent>   errors;
    protected final Collection<Checkpoint>   dependents;
    protected BufferedQueue<ProcessingEvent> eventQueue;
    protected boolean                        reached;

    public Checkpoint() {
        expectedCompletions = 0;
        totalRuntime = 0;
        completions = 0;
        totalOutput = 0;
        dependents = new Vector<>();
        errors = new LinkedList<>();
        reached = false;
    }

    public long getExpectedCompletions() {
        return expectedCompletions;
    }

    public void setExpectedCompletions(long expectedCompletions) {
        this.expectedCompletions = expectedCompletions;
    }

    public long getCompletions() {
        return completions;
    }

    public long getTotalRuntime() {
        return totalRuntime;
    }

    public Collection<ErrorEvent> getErrors() {
        return errors;
    }

    public Collection<Checkpoint> getDependents() {
        return dependents;
    }

    public void addDependent(Checkpoint c) {
        dependents.add(c);
    }

    public void removeDependent(Checkpoint c) {
        dependents.remove(c);
    }

    public double getProgress() {
        if (expectedCompletions > 0) {
            return (double) (completions) / expectedCompletions;
        } else {
            return 0;
        }
    }

    public double getCompletionRate() {
        if (totalRuntime > 0)
            return 1000.0 * completions / totalRuntime;
        else
            return 0;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public void setEventSource(DistributedTaskHandler source) {
        this.eventQueue = source.getEventQueue();
        source.getEventQueue().addItemListener(this, true);
    }

    @Override public void itemAdded(ItemEvent<ProcessingEvent> e) {
        ProcessingEvent event = eventQueue.poll();
        if (event == null) {
            return;
        }

        if (event instanceof CompletionEvent) {
            CompletionEvent completeEvent = ((CompletionEvent) event);
            completions++;
            totalRuntime += completeEvent.getRunTime();
            totalOutput += completeEvent.getOutputCount();
            if (expectedCompletions != 0 && completions >= expectedCompletions) {
                onReached();
            }
        } else if (event instanceof ErrorEvent) {
            // Save this event
            errors.add((ErrorEvent) event);
        }
    }

    @Override public void itemRemoved(ItemEvent<ProcessingEvent> e) {
        // Not all that interested to know when items are removed
    }

    public void onReached() {
        reached = true;
        System.out.println("["
                           + this
                           + "]: Reached Checkpoint, producing "
                           + totalOutput
                           + " / "
                           + expectedCompletions
                           + " at "
                           + getCompletionRate()
                           + "/ms with "
                           + getErrorCount()
                           + " errors");
        for (Checkpoint dependent : dependents) {
            dependent.setExpectedCompletions(totalOutput);
        }
    }

    public long getTotalOutput() {
        return totalOutput;
    }

    public BufferedQueue<ProcessingEvent> getEventQueue() {
        return eventQueue;
    }

    public boolean isReached() {
        return reached;
    }
}
