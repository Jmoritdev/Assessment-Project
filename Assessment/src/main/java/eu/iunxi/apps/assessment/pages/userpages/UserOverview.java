package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import eu.iunxi.apps.assessment.util.UserComparator;
import java.util.Collections;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
public class UserOverview {
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Property
    private User loopedUser; //used in template
    
    @Property
    private final Permission userRemovePermission = Permission.USER_REMOVE; //used in template
    
    @Property
    private final Permission userEditPermission = Permission.USER_EDIT; //used in template
    
    @Inject
    private Session session;
    
    @Inject
    private AlertManager alertManager;
    
    
    Object onActivate(){
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.USER_EDIT) && !user.hasPermissionTo(Permission.USER_REMOVE) ){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te bekijken.");
            return AssessmentOverview.class;
        }
        
        return null;
    }
    
    
    public List<User> getUsers(){
        Criteria query = this.session.createCriteria(User.class);
        query.add(Restrictions.eq("deleted", false));
        List<User> list = query.list();
        
        Collections.sort(list, new UserComparator());
        
        return list;
    }
    
    public User getUser(){ //used in template
        return loggedInUser.getUser(session);
    }

}
