package edu.harvard.mcb.leschziner.particlesource;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.distributed.DistributedProcessingTask;

public abstract class DistributedPickingTask extends DistributedProcessingTask {
    /**
     * 
     */
    private static final long serialVersionUID = -5192990321242474077L;
    protected final Particle  target;
    protected final int       boxSize;
    protected final String    particleQueueName;

    public DistributedPickingTask(Particle target,
                                  int boxSize,
                                  String particleQueueName,
                                  String executorName) {
        super(executorName);
        this.target = target;
        this.boxSize = boxSize;
        this.particleQueueName = particleQueueName;
    }

}
