package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;

/**
 *
 * @author joey
 */
public class QuestionDelete {
    
    @Property
    private Question question;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @InjectPage
    private CategoryDetails categoryDetails;    

    @Inject
    private AlertManager alertManager;
    
    @Inject
    private Session session;
    
    
    Object[] onPassivate() {
        return new Object[]{ question };
    }

    public Object onActivate(Question question) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.QUESTION_REMOVE)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen te verwijderen.");
            return CategoryOverview.class;
        }
        
        this.question = question;
        
        if(this.question == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze vraag bestaat niet.");
            return CategoryOverview.class;
        }
        
        return null;
    }
    
    @CommitAfter
    public Object onDelete(){
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.QUESTION_REMOVE)){
            question.setDeleted(true);
        
            alertManager.alert(Duration.SINGLE, Severity.INFO, "De vraag met de titel \""+question.getTitle()+"\" is succesvol verwijdert.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen te verwijderen.");
        }
        categoryDetails.setThisCategory(question.getCategory());
        return categoryDetails;
    }
}
