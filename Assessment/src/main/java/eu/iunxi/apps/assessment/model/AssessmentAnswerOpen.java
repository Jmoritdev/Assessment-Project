package eu.iunxi.apps.assessment.model;

import javax.persistence.Entity;


/**
 *
 * @author joey
 */

@Entity 
public class AssessmentAnswerOpen extends AssessmentAnswer{
    
    /**
     * the answer that the examiner gave in response to the answer of the testsubject
     */
    private String examinerAnswer;
    
    /**
     * the amount of points given by the examiner for the answer that the testsubject gave
     */ 
    private Integer assignedPoints;
    
    /**
     * the answer the testsubject gave
     */
    private String userAnswer;   
    
    /**
     * @return the examinerAnswer
     */
    public String getExaminerAnswer() {
        return examinerAnswer;
    }

    /**
     * @param examinerAnswer the examinerAnswer to set
     */
    public void setExaminerAnswer(String examinerAnswer) {
        this.examinerAnswer = examinerAnswer;
    }

    /**
     * @return the assignedPoints
     */
    public Integer getAssignedPoints() {
        return assignedPoints;
    }

    /**
     * @param assignedPoints the assignedPoints to set
     */
    public void setAssignedPoints(Integer assignedPoints) {
        this.assignedPoints = assignedPoints;
    }
    
    /**
     * @return the userAnswer
     */
    public String getUserAnswer() {
        return userAnswer;
    }

    /**
     * @param userAnswer the userAnswer to set
     */
    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    @Override
    public boolean isAnswered() {
        return !(userAnswer == null);
    }
    
    public boolean isChecked() {
        return !(assignedPoints == null);
    }

    @Override
    public int getPointsScored() {
        return (assignedPoints != null) ? assignedPoints : 0;
    }
    
}
