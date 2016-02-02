package eu.iunxi.apps.assessment.model;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;


/**
 *
 * @author joey
 */

@Entity 
public class AssessmentAnswerClosedMultiple extends AssessmentAnswerClosed{
    
    @ManyToMany(cascade = CascadeType.ALL)
    private List<QuestionClosedOption> selectedOptions;
    
    public AssessmentAnswerClosedMultiple() {
        this(new LinkedList<QuestionClosedOption>());
    }

    public AssessmentAnswerClosedMultiple(List<QuestionClosedOption> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }
    
    /**
     * 
     * @return selectedOptions the selectedOptions to get 
     */
    public List<QuestionClosedOption> getSelectedOptions() {
        return selectedOptions;
    }
    
    /**
     * 
     * @param selectedOptions the selectedOptions to set
     */
    public void setSelectedOptions(List<QuestionClosedOption> selectedOptions){
        this.selectedOptions = selectedOptions;
    }
    
    public void addSelectedOption(QuestionClosedOption qco){
        this.selectedOptions.add(qco);
    }
    
    public void removeSelectedOption(QuestionClosedOption qco){
        this.selectedOptions.remove(qco);
    }

    @Override
    public boolean isAnswered() {
        return (!selectedOptions.isEmpty());
    }

    @Override
    public int getPointsScored() {
        int scored = 0;
        for(QuestionClosedOption qco : selectedOptions){
            scored += qco.getPointsWorth();
        }
        return scored;
    }
    
}
