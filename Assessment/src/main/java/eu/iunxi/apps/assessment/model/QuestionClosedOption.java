package eu.iunxi.apps.assessment.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import org.apache.tapestry5.beaneditor.NonVisual;

/**
 *
 * @author joey
 */
@Entity
public class QuestionClosedOption implements Serializable  {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonVisual
    private int id;
        
    private String description;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean correct;
    
    @ManyToOne
    private QuestionClosed question;
    
    private int pointsWorth;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean deleted;
    
    
    public QuestionClosedOption(){
    }
    
    public QuestionClosedOption(String description, boolean correct, int pointsWorth){
        this.description = description;
        this.correct = correct;
        this.pointsWorth = pointsWorth;
    }
    
    public QuestionClosedOption(QuestionClosed question) {
        this.setQuestion(question);
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the correct
     */
    public boolean isCorrect() {
        return correct;
    }

    /**
     * @param correct the correct to set
     */
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the question
     */
    public QuestionClosed getQuestion() {
        return question;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(QuestionClosed question) {
        if (this.question != question) {
            this.question = question;
            this.question.addOption(this);
        }
    }

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

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
}
