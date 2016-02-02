package eu.iunxi.apps.assessment.components;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.pages.Index;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.*;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.hibernate.Session;

/**
 * Layout component for pages of application test-project.
 */
@Import(library = {"context:jquery/jquery-1.11.3.min.js"}, 
        module = {"bootstrap/collapse", "bootstrap/dropdown", "bootstrap/modal"},
        stylesheet={"context:font-awesome-4.5.0/css/font-awesome.min.css"})
public class Layout {

    @Inject
    private ComponentResources resources;

    @Property
    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String title;
    
    @Property
    @Parameter(required = false)
    private boolean hideNav;
    
    @Property
    @Parameter(required = false)
    private boolean isPublic;
    
    @Property
    private String pageName;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject
    private Request request;

    @Inject
    private AlertManager alertManager;
    
    @InjectPage
    private Index index;
    
    @Inject
    private PageRenderLinkSource pageRenderLinkSource;
    
    @Inject
    @Property
    private Session session;
    
    @Property
    private final Permission userEditPermission = Permission.USER_EDIT; //used in template
    

    
    /**
     * checks if the current page equals the page in the header
     * @return 
     */
    public String getClassForPageName() {
        return resources.getPageName().equalsIgnoreCase(pageName)
                ? "active"
                : null;
    }

    public String[] getPageNames() {
        return new String[]{"userpages/AssessmentOverview", "userpages/CategoryOverview"};
    }
    
    /**
     * checks if there is a user logged in
     * @return 
     */
    public boolean isLoggedIn(){
        return loggedInUser != null;
    }
    
    public Object onLogout(){
        loggedInUser = null;
        alertManager.alert(Duration.SINGLE, Severity.INFO, "Je bent succesvol uitgelogd.");
        return Index.class;
    }
    
    Object onLogin(){
        Link thisPage = pageRenderLinkSource.createPageRenderLink(resources.getPage().getClass());
        index.setPreviousPage(thisPage);
        return index;
    }
    
    public boolean canViewUserOverview(){
        User user = loggedInUser.getUser(session);
        return (user.hasPermissionTo(Permission.USER_EDIT) || user.hasPermissionTo(Permission.USER_REMOVE));
    }
    

}
