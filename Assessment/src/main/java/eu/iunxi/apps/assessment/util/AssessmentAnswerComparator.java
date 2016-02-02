package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import java.util.Comparator;

/**
 *
 * @author joey
 */
public class AssessmentAnswerComparator implements Comparator<AssessmentAnswer>{
    
    public enum SortBy{
        CATEGORY, DIFFICULTY
    }
    
    public SortBy type;
    
    public AssessmentAnswerComparator(SortBy type){
        this.type = type;
    }
    
    @Override
    public int compare(AssessmentAnswer o1, AssessmentAnswer o2) {
        if(type.equals(SortBy.CATEGORY)){
            return o1.getQuestion().getCategoryName().toLowerCase().compareTo(o2.getQuestion().getCategoryName().toLowerCase());
        }
        if(type.equals(SortBy.DIFFICULTY)){
            return o1.getQuestion().getDifficulty().compareTo(o2.getQuestion().getDifficulty());
        }
        return 0;
    }
    
}
