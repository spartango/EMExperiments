package edu.harvard.mcb.leschziner.core;

import java.util.Vector;

public interface ParticleClassifier {
    
    public Vector<Particle>[] getClasses(); 
    public void classify(Particle target);
}
