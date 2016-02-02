package eu.iunxi.apps.assessment.model;

import javax.persistence.Entity;

/**
 *
 * @author joey
 */

@Entity 
public class QuestionOpen extends Question {
    
    public QuestionOpen(){
        super();
    }
    
    public QuestionOpen(String title, Category category, String description, Difficulty difficulty, int pointsWorth, String answerCheatSheet) {
        super(title, category, description, difficulty);
        this.pointsWorth = pointsWorth;
        this.answerCheatSheet = answerCheatSheet;
    }
    
    /**
     * the maximum amount of points this question is worth
     */
    private int pointsWorth;
    
    /**
     * a cheat String intended for easier checking of answers
     */
    private String answerCheatSheet;

    /**
     * @return the pointsWorth
     */
    public int getPointsWorth() {
        return pointsWorth;
    }

    /**
     * @param pointsWorth the pointsWorth to set
     */
    public void setPointsWorth(int pointsWorth) {
        this.pointsWorth = pointsWorth;
    }

    @Override
    public int getMaxScore() {
        return this.pointsWorth;
    }

    @Override
    AssessmentAnswer createAnswerForAssessment(Assessment assessment) {
        AssessmentAnswerOpen answer = new AssessmentAnswerOpen();
        answer.setQuestion(this);
        answer.setAssessment(assessment);
        return answer;
    }

    /**
     * @return the answerCheatSheet
     */
    public String getAnswerCheatSheet() {
        return answerCheatSheet;
    }

    /**
     * @param answerCheatSheet the answerCheatSheet to set
     */
    public void setAnswerCheatSheet(String answerCheatSheet) {
        this.answerCheatSheet = answerCheatSheet;
    }
}
