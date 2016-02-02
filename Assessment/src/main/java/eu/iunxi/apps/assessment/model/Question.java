package eu.iunxi.apps.assessment.model;


import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.apache.tapestry5.beaneditor.NonVisual;


/**
 *
 * @author joey
 */
@Entity 
@Inheritance(strategy=InheritanceType.JOINED)
abstract public class Question implements Serializable {

    public Question() {
    }
    
    public Question(String title, Category category, String description, Difficulty difficulty){
        this.title = title;
        this.category = category;
        this.description = description;
        this.difficulty = difficulty;
    }
    
    public enum Difficulty{
        JUNIOR, MEDIOR, SENIOR
    }
   
    private String title;
    
    @Lob
    @Column(length = 10000)
    private String description;
    
    @ManyToOne
    private Category category;
    
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean deleted;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @NonVisual
    private int id;
    
    @OneToMany(mappedBy="question", cascade=CascadeType.ALL)
    private List<QuestionImage> images;
    
    
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
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * @param difficulty the difficulty to set
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * @return the category
     */
    public Category getCategory() {
        return category;
    }
    
    /**
     * @return the name of the category 
     */
    public String getCategoryName(){
        return category.getTitle();
    }

    /**
     * @param category the category to set
     */
    public void setCategory(Category category) {
        this.category = category;
        if (this.category != null) {
            this.category.addQuestion(this);
        }
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
    
    /**
     * @return the images
     */
    public List<QuestionImage> getImages() {
        return images;
    }

    /**
     * @param images the images to set
     */
    public void setImages(List<QuestionImage> images) {
        this.images = images;
    }
    
    /**
     * @param image the image to add 
     */
    public void addImage(QuestionImage image){
        images.add(image);
    }
    
    /**
     * Get the maximum score for this question
     * @return 
     */
     public abstract int getMaxScore();
    
    /**
     * Create a new AssessmentAnswer for holding the answer to this question for given Assessment
     * @param assessment The assessment 
     * @return 
     */
    abstract AssessmentAnswer createAnswerForAssessment(Assessment assessment);
    
}
