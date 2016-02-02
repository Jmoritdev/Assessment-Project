package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.CategoryComparator;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
@Import(library = {"context:scripts/categoryOverviewFunctions.js"})
public class CategoryOverview {
    
    @Property
    private Category loopedCategory; //used in template

    @Inject 
    private Session session;
    
    @Property
    private String newCategoryTitle;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Property
    private List<Category> categoryList; //used in template
    
    @Property
    private final Permission categoryEditPermission = Permission.CATEGORY_EDIT; //used in template
    
    @Property
    private final Permission categoryRemovePermission = Permission.CATEGORY_REMOVE; //used in template
        
    
    @SetupRender
    public void setupRender() {
        categoryList = new LinkedList<Category>(); 
        categoryList = getQuery().list();
    }
    
    private Criteria getQuery(){
        Criteria query = this.session.createCriteria(Category.class);
        query.add(Restrictions.eq("deleted", false));
        return query;
    }
    
    @CommitAfter
    Object onSuccessFromNewCategoryForm(){
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.CATEGORY_EDIT)){
            Category cat = new Category();
            cat.setTitle(newCategoryTitle);
            session.persist(cat);

            alertManager.alert(Duration.SINGLE, Severity.INFO, "De \""+newCategoryTitle+"\" categorie is succesvol aangemaakt.");

            newCategoryTitle = null;
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om een nieuwe categorie aan te maken.");
        }
        return CategoryOverview.class;
    }
    
    public User getUser(){ //used in template
        return loggedInUser.getUser(session);
    }
}
