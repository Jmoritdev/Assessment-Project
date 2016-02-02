package eu.iunxi.apps.assessment.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.apache.tapestry5.beaneditor.NonVisual;


/**
 *
 * @author joey
 */
@Entity
public class Category implements Serializable{
    
    private String title;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean deleted;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonVisual
    private int id;
    
    @OneToMany(mappedBy = "category", cascade=CascadeType.ALL)
    private List<Question> questions;
    
    public Category() {
       this.questions = new LinkedList<Question>();
    }
    
    public Category(String title, Question question){
        this.title = title;
        this.questions = new LinkedList<Question>();
        addQuestion(question);
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    
    /**
     * Return a list of Questions in this category for a given difficulty
     * @param difficulty
     * @return 
     */
    public List<Question> getQuestions(Question.Difficulty difficulty) {
        List<Question> questionList = new LinkedList<Question>();
        
        for(Question q : questions){
            if(q.getDifficulty().equals(difficulty) && q.isDeleted() == false){
                questionList.add(q);
            }
        }
        
        return questionList;
    }

    /**
     * @return the questions in this category
     */
    public List<Question> getQuestions() {
        List<Question> questionList = new LinkedList<Question>();
        
        for(Question q : questions){
            if(q.isDeleted() == false){
                questionList.add(q);
            }
        }
        
        return questionList;
    }

    public Category addQuestion(Question question) {
        if (!this.questions.contains(question)) {
            this.questions.add(question);
            question.setCategory(this);
        }
        return this;
    }
    
    public List<QuestionClosedOption> getOptionsFromQuestion(QuestionClosed question){
        return (this.questions.contains(question)) ? question.getOptions() : null;
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
