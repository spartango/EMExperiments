package edu.harvard.mcb.leschziner.analyze;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import edu.harvard.mcb.leschziner.core.Particle;

public class ClassAncestry {

    /**
     * Calculates the fractional overlap between two classes by ancestry
     * 
     * @param firstClass
     * @param secondClass
     * @return
     */
    public static double calculateOverlap(Collection<Particle> firstClass,
                                          Collection<Particle> secondClass) {
        int matches = 0;
        for (Particle first : firstClass) {
            for (Particle second : secondClass) {
                if (first.getSourceId() == second.getSourceId()) {
                    matches++;
                    break;
                }
            }
        }

        return (double) (matches) / (firstClass.size() + secondClass.size());
    }

    /**
     * Finds the intersection of the two classes based on ancestry, biasing
     * selection such that the resulting set is of aligned particles
     * 
     * @return
     */
    public static Collection<Particle>
            intersection(Collection<Particle> firstClass,
                         Collection<Particle> secondClass) {
        LinkedList<Particle> intersect = new LinkedList<>();

        for (Particle first : firstClass) {
            for (Particle second : secondClass) {
                if (first.getSourceId() == second.getSourceId()) {
                    intersect.add(first);
                    break;
                }
            }
        }
        return intersect;
    }

    /**
     * Find particles with unique ancestry between the two sets. Note that the
     * resulting set is NOT aligned.
     * 
     * @param firstClass
     * @param secondClass
     * @return
     */
    public static Collection<Particle>
            difference(Collection<Particle> firstClass,
                       Collection<Particle> secondClass) {
        HashSet<Particle> intersect = new HashSet<>();
        for (Particle first : firstClass) {
            for (Particle second : secondClass) {
                if (first.getSourceId() == second.getSourceId()) {
                    intersect.add(first);
                    break;
                }
            }
        }

        Vector<Particle> diff = new Vector<>(firstClass.size()
                                             + secondClass.size()
                                             - intersect.size());
        for (Particle first : firstClass) {
            if (!intersect.contains(first)) {
                diff.add(first);
            }
        }

        for (Particle second : secondClass) {
            if (!intersect.contains(second)) {
                diff.add(second);
            }
        }
        return diff;
    }
}
