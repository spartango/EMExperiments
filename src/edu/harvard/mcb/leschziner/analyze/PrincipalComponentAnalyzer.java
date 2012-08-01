package edu.harvard.mcb.leschziner.analyze;

import java.io.Serializable;
import java.util.Collection;

import edu.harvard.mcb.leschziner.core.Particle;

public interface PrincipalComponentAnalyzer extends Serializable {
    public PrincipalComponents analyze(Collection<Particle> targets);
}
