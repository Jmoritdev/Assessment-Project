package eu.iunxi.apps.assessment.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 *
 * @author joey
 */

@Entity 
abstract public class QuestionClosed extends Question{
    
    @OneToMany(mappedBy = "question", cascade=CascadeType.ALL)
    private List<QuestionClosedOption> options;

    public QuestionClosed() {
        super();
        this.options = new LinkedList<QuestionClosedOption>();
    }
    
    public QuestionClosed(String title, Category category, String description, Difficulty difficulty) {
        super(title, category, description, difficulty);
        this.options = new LinkedList<QuestionClosedOption>();
    }

    /**
     * @return the options for this question
     */
    public List<QuestionClosedOption> getOptions() {
        List<QuestionClosedOption> list = new LinkedList<QuestionClosedOption>();
        list.addAll(options);
        
        //remove deleted questions from the list
        for (Iterator<QuestionClosedOption> iterator = list.iterator(); iterator.hasNext();) {
            QuestionClosedOption qco = iterator.next();
            if (qco.isDeleted()) {
                iterator.remove();
            }
        }
        
        return list;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(List<QuestionClosedOption> options) {
        this.options = options;
    }
    
    /**
     * add an option to this question and sets this question to that option
     * @param qco
     * @return 
     */
    public QuestionClosed addOption(QuestionClosedOption qco){
        if (qco == null) throw new IllegalArgumentException("qco may not be null!");

        if (!this.options.contains(qco)) {
            this.options.add(qco);
        }
        
        qco.setQuestion(this);
        
        return this;
    }
    
    /**
     * adds options to this question and sets this question to those options
     * @param qcoList
     * @return 
     */
    public QuestionClosed addOptions(List<QuestionClosedOption> qcoList){
        if (qcoList == null || qcoList.isEmpty()) throw new IllegalArgumentException("qcoList may not be null or empty!");
        
        for (QuestionClosedOption qco : qcoList) {
            if (!this.options.contains(qco)) {
                this.options.add(qco);
                qco.setQuestion(this);
            }
        }
        
        return this;
    }
}
