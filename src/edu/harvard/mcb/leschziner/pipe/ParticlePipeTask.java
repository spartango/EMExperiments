package edu.harvard.mcb.leschziner.pipe;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;

public abstract class ParticlePipeTask extends DistributedProcessingTask {
    /**
     * 
     */
    private static final long serialVersionUID = -6142835352890034222L;
    protected final Particle  target;
    protected final String    processedQueueName;

    public ParticlePipeTask(Particle target,
                            String processedQueueName,
                            String executorName) {
        super(executorName);
        this.target = target;
        this.processedQueueName = processedQueueName;
    }
}
