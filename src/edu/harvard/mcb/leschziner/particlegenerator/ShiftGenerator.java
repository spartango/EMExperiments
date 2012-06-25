package edu.harvard.mcb.leschziner.particlegenerator;

import java.util.Collection;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;
import edu.harvard.mcb.leschziner.core.ParticleGenerator;
import edu.harvard.mcb.leschziner.particlefilter.Shifter;

public class ShiftGenerator implements ParticleGenerator {

    private final Vector<Shifter> shifters;

    public ShiftGenerator(int maxShift, int deltaShift) {
        shifters = new Vector<Shifter>();

        for (int x = -maxShift; x <= maxShift; x += deltaShift) {
            for (int y = -maxShift; y <= maxShift; y += deltaShift) {
                shifters.add(new Shifter(x, y));
            }
        }
    }

    @Override
    public Collection<Particle> generate(Particle seed) {
        // Apply each rotator to the particle
        Vector<Particle> shifted = new Vector<Particle>(shifters.size());
        for (Shifter shifter : shifters) {
            shifted.add(shifter.filter(seed));
        }
        return shifted;
    }

    @Override
    public Collection<Particle> generate(Collection<Particle> seeds) {
        Vector<Particle> shifted = new Vector<Particle>(seeds.size()
                                                        * shifters.size());
        for (Particle seed : seeds) {
            shifted.addAll(generate(seed));
        }
        return shifted;
    }
}
