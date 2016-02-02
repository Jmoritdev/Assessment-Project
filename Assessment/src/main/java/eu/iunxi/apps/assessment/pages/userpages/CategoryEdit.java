package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import org.apache.tapestry5.ValidationException;
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
public class CategoryEdit {
    
    @Property
    private Category category;
    
    @Inject
    private AlertManager alertManager;  
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject
    private Session session;        
    
    
    Object[] onPassivate(){
        return new Object[] { category };
    }
    
    Object onActivate(Category category){
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.CATEGORY_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om categorieën aan te passen.");
            return CategoryOverview.class;
        }
        
        this.category = category;
                
        if(category == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze categorie bestaat niet.");
            return CategoryOverview.class;
        }   
        
        return null;
    }
    
    public void onValidateFromCategoryEditForm() throws ValidationException {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.CATEGORY_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om categorieën aan te passen.");
            throw new ValidationException("");
        }
    }
    
    @CommitAfter
    public Object onSuccessFromCategoryEditForm(){
        alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De category met de titel "+category.getTitle()+" is succesvol aangepast.");
        return CategoryOverview.class;
    }
    
}
