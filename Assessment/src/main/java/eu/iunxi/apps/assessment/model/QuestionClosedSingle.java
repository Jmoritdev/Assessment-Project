package eu.iunxi.apps.assessment.model;

import javax.persistence.Entity;

/**
 *
 * @author joey
 */

@Entity 
public class QuestionClosedSingle extends QuestionClosed{

    public QuestionClosedSingle() {
        super();
    }
    
    public QuestionClosedSingle(String title, Category category, String description, Difficulty difficulty) {
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
        AssessmentAnswerClosedSingle answer = new AssessmentAnswerClosedSingle();
        answer.setQuestion(this);
        answer.setAssessment(assessment);
        return answer;
    }
   
}
