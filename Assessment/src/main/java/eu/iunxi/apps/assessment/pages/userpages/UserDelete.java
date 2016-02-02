package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;

/**
 *
 * @author joey
 */
public class UserDelete {
    
    @Property
    private User user;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject
    private Session session;
    
    Object[] onPassivate(){
        return new Object[] { user };
    }
    
    Object onActivate(User user){
        User loggedUser = loggedInUser.getUser(session);
        if(loggedUser == null || !loggedUser.hasPermissionTo(Permission.USER_REMOVE)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te verwijderen.");
            return AssessmentOverview.class;
        }
        
        this.user = user;
        
        if(this.user == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze gebruiker bestaat niet.");
            return UserOverview.class;
        }
        
        return null;
    }
    
    @CommitAfter
    public Object onDelete(){
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.USER_REMOVE)){
            this.user.setDeleted(true);
        
            alertManager.alert(Duration.SINGLE, Severity.INFO, "\""+ this.user.getName() +"\" is succesvol verwijdert.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te verwijderen.");
            return AssessmentOverview.class;
        }
        return UserOverview.class;
    }
}
