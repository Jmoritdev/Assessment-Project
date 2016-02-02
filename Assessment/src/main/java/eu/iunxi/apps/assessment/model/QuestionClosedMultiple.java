package eu.iunxi.apps.assessment.model;

import java.util.LinkedList;
import java.util.List;
import javax.persistence.Entity;

/**
 *
 * @author joey
 */

@Entity 
public class QuestionClosedMultiple extends QuestionClosed {

    public QuestionClosedMultiple() {
        super();
    }
    
    public QuestionClosedMultiple(String title, Category category, String description, Difficulty difficulty) {
        super(title, category, description, difficulty);
    }
    
    
    @Override
    public int getMaxScore() {
        int maxScore = 0;
        for(QuestionClosedOption qco : this.getOptions()){
            if(qco.isCorrect()){
                maxScore += qco.getPointsWorth();
            }
        }
        return maxScore;
    }

    @Override
    AssessmentAnswer createAnswerForAssessment(Assessment assessment) {
        AssessmentAnswerClosedMultiple answer = new AssessmentAnswerClosedMultiple();
        answer.setQuestion(this);
        answer.setAssessment(assessment);
        return answer;
    } 
    
    public int getAmountOfCorrectAnswers(){
        int amount = 0;
        for(QuestionClosedOption qco : this.getOptions()){
            if(qco.isCorrect()){
                amount += 1;
            }
        }
        return amount;
    }
    
    public List<QuestionClosedOption> getCorrectOptions(){
        List<QuestionClosedOption> list = new LinkedList<QuestionClosedOption>();
        for(QuestionClosedOption qco : this.getOptions()){
            if(qco.isCorrect()){
                list.add(qco);
            }
        }
        return list;
    }
}
