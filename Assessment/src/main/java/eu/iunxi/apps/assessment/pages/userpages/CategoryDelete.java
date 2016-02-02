package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Category;
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
public class CategoryDelete {
    
    @Property
    private Category category;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;  

    @Inject
    private AlertManager alertManager;
    
    @Inject
    private Session session;        
    
    
    Object[] onPassivate() {
        return new Object[]{ category };
    }

    public Object onActivate(Category category) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.CATEGORY_REMOVE)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om categorieën te verwijderen.");
            return CategoryOverview.class;
        }
        
        this.category = category;
        
        if(category == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze categorie bestaat niet.");
            return CategoryOverview.class;
        }
        
        return null;
    }
    
    @CommitAfter
    public Object onDelete(){
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.CATEGORY_REMOVE)){
            category.setDeleted(true);
        
            alertManager.alert(Duration.SINGLE, Severity.INFO, "Categorie succesvol verwijdert.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om categorieën te verwijderen.");
        }
        return CategoryOverview.class;
    }
}
