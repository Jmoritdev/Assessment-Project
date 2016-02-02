package eu.iunxi.apps.assessment.model;

import javax.persistence.Entity;
import javax.persistence.OneToOne;


/**
 *
 * @author joey
 */
@Entity 
public class AssessmentAnswerClosedSingle extends AssessmentAnswerClosed{
    
    @OneToOne
    private QuestionClosedOption selectedOption;

    /**
     * @return the selectedOption
     */
    public QuestionClosedOption getSelectedOption() {
        return selectedOption;
    }
    
    /**
     * 
     * @param selectedOption the selectedOptions to set
     */
    public void setSelectedOption(QuestionClosedOption selectedOption){
        this.selectedOption = selectedOption;
    }

    @Override
    public boolean isAnswered() {
        return !(selectedOption == null);
    }

    @Override
    public int getPointsScored() {
        return (selectedOption != null) ? selectedOption.getPointsWorth() : 0;
    }
    
}
