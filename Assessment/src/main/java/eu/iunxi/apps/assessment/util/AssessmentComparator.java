package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.Assessment;
import java.util.Comparator;

/**
 *
 * @author joey
 */
public class AssessmentComparator implements Comparator<Assessment>{
    
    @Override
    public int compare(Assessment o1, Assessment o2) {
        return o1.getCreationDate().compareTo(o2.getCreationDate());
    }
    
}
