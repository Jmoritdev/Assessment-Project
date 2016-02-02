package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.Question;
import java.util.Comparator;

/**
 *
 * @author joey
 */
public class QuestionComparator implements Comparator<Question>{
    
    public enum SortBy{
       CATEGORY, DIFFICULTY, TITLE
    }
    
    public SortBy sortBy;
    
    public QuestionComparator(SortBy sortBy){
        this.sortBy = sortBy;
    }
    
    @Override
    public int compare(Question o1, Question o2) {
        switch(sortBy){
            case CATEGORY:
                return o1.getCategoryName().toLowerCase().compareTo(o2.getCategoryName().toLowerCase());
            case DIFFICULTY:
                return o1.getDifficulty().compareTo(o2.getDifficulty());
            case TITLE:
                return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
        }
        
        return 0;
    }
    
}
