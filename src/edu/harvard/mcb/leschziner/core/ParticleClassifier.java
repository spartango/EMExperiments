package edu.harvard.mcb.leschziner.core;

import java.util.Vector;

public interface ParticleClassifier {
    public int countClasses();

    public Vector<Particle> getClass(int i);

    public Particle getClassAverage(int i);

    public Vector<Particle>[] getClasses();

    public Particle[] getClassAverages();
    
    public int classify(Particle target);
}
