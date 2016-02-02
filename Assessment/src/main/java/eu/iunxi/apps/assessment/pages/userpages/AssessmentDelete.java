package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
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
public class AssessmentDelete {
    
    @Property
    private Assessment assessment;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;  

    @Inject
    private AlertManager alertManager;
    
    @Inject
    private Session session;
    
    
    Object[] onPassivate() {
        return new Object[]{ assessment };
    }

    public Object onActivate(Assessment assessment) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.ASSESSMENT_REMOVE)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om assessments te verwijderen.");
            return AssessmentOverview.class;
        }
        
        this.assessment = assessment;

        if (this.assessment == null) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Dit assessment bestaat niet.");
            return AssessmentOverview.class;
        }
        
        return null;
    }
    
    @CommitAfter
    public Object onDelete(){
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.ASSESSMENT_REMOVE)){
            assessment.setDeleted(true);
        
            alertManager.alert(Duration.SINGLE, Severity.INFO, "Assessment succesvol verwijdert.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om assessments te verwijderen.");
        }
        return AssessmentOverview.class;
    }
    
}
