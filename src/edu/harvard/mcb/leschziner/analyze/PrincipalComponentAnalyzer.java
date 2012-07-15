package edu.harvard.mcb.leschziner.analyze;

import java.io.Serializable;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;

public interface PrincipalComponentAnalyzer extends Serializable {
    public PrincipalComponents analyze(Vector<Particle> targets);
}
