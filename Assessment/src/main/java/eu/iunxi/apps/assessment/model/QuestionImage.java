package eu.iunxi.apps.assessment.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import org.apache.commons.io.IOUtils;
import org.apache.tapestry5.beaneditor.NonVisual;

/**
 *
 * @author joey
 */
@Entity
public class QuestionImage implements Serializable {
    
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @NonVisual
    private int id;
    
    @Lob
    private byte[] image;
    
    @ManyToOne
    private Question question;
    
    private String contentType;
    
    private String fileName;

    
    public QuestionImage() {
    }
    
    public QuestionImage(InputStream imageStream, String contentType, String fileName) throws IOException {
        this.image = IOUtils.toByteArray(imageStream);
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public QuestionImage(InputStream imageStream, Question question, String contentType, String fileName) throws IOException {
        this.image = IOUtils.toByteArray(imageStream);
        this.question = question;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public void setImage(InputStream imageStream) throws IOException {
        this.setImage(IOUtils.toByteArray(imageStream));    // org.apache.commons.io.IOUtils.toByteArray
    }
    
    /**
     * @return  the image
     */
    public InputStream getImage() {
        return new ByteArrayInputStream(this.image);
    }

    /**
     * @param image the image to set
     */
    public void setImage(byte[] image) {
        this.image = image;
    }
    
    /**
     * @return the image in a byte[]
     */
    public byte[] getImageBytes(){
        return image;
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
    public Question getQuestion() {
        return question;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @param question the question to set
     */
    public void setQuestion(Question question) {
        this.question = question;
    }
    
}
