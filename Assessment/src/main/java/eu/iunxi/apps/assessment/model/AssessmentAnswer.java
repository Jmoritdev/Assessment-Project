package eu.iunxi.apps.assessment.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import org.apache.tapestry5.beaneditor.NonVisual;


/**
 *
 * @author joey
 */
@Entity 
@Inheritance(strategy=InheritanceType.JOINED)
abstract public class AssessmentAnswer implements Serializable {
    
    @ManyToOne
    private Assessment assessment;
    
    @ManyToOne
    private Question question;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonVisual
    private int id;
    
    /**
     * @return the assessment
     */
    public Assessment getAssessment() {
        return assessment;
    }

    /**
     * @param assessment the assessment to set
     */
    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    /**
     * @return the question
     */
    public Question getQuestion() {
        return question;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(Question question) {
        this.question = question;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    public abstract boolean isAnswered();
    
    public abstract int getPointsScored();
    
}
