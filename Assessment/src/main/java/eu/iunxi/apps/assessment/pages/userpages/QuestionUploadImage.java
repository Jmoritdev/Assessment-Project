package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.QuestionImage;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import eu.iunxi.apps.assessment.util.Picture;
import java.io.IOException;
import java.util.List;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
@Import(library = {"context:scripts/questionUploadScript.js"})
public class QuestionUploadImage {
    
    @Property
    private UploadedFile file;
    
    @Property
    private Question question;
    
    @Inject
    private Session session; 
    
    @Inject
    private ComponentResources componentResources;
    
    @Property
    private QuestionImage loopedImage; //used in template 
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject
    private AlertManager alertManager;
       
    @ActivationRequestParameter
    private String CKEditorFuncNum;//needed by ckeditor
    
    
    Object[] onPassivate(){
        return new Object[] { question };
    }
    
    Object onActivate(Question question){
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.QUESTION_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om plaatjes te uploaden.");
            return AssessmentOverview.class;
        }
        
        this.question = question;
        
        return null;
    }
    
    
    public void onValidateFromNewImageForm() throws IOException, ValidationException{
        if(!file.getContentType().equals("image/jpg") && !file.getContentType().equals("image/png")){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Het plaatje moet een .jpg of een .png zijn.");
            throw new ValidationException("");
        }
    }    
    
    @CommitAfter
    public Object onSuccessFromNewImageForm() throws IOException {
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.QUESTION_EDIT)){
            QuestionImage image = new QuestionImage(file.getStream(), question, file.getContentType(), file.getFileName());
            question.addImage(image);
            
            session.persist(image);
            session.persist(question);
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om plaatjes te uploaden.");
            return AssessmentOverview.class;
        }
        
        return this;
    }    
    
    public List<QuestionImage> getImages(){
        Criteria query = this.session.createCriteria(QuestionImage.class);
        query.add(Restrictions.eq("question", question));
        
        return query.list();
    }
    
    public Object onGet(QuestionImage image) throws IOException {
        return new Picture(image);
    }
    
    public String createLink(QuestionImage image){
        return componentResources.createEventLink("GET", new Object[]{ image }).toURI();
    }
}
