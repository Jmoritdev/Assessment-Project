package eu.iunxi.apps.assessment.pages;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.pages.userpages.ChangePassword;
import eu.iunxi.apps.assessment.pages.userpages.AssessmentOverview;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.PasswordDigester;
import eu.iunxi.apps.assessment.util.Permission;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;



public class Index {
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject 
    private Session session;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    @Validate("required")
    private String loginEmail;
    
    @Property
    @Validate("required")
    private String loginPword;
    
    @Persist
    private Link previousPageLink;
    
    @Inject
    private ComponentResources componentResources;
    
    @Property
    private String name;
    
    @Property
    private String email;
    
    @Property
    private String password;
    
    @Property
    private String confirmPassword;      
   
    
    Object onSuccessFromLoginForm() throws NoSuchAlgorithmException{
        User loginTry = findUserWithUserEmail(this.loginEmail);

        if(loginTry != null){
            if(loginTry.getPassword().equals(PasswordDigester.getDigest(loginPword)) && loginTry.hasPermissionTo(Permission.CAN_LOGIN)){
                loggedInUser = new LoggedInUser();
                loggedInUser.setUser(loginTry);
                alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Je bent ingelogd!");
                if(loginTry.hasTempPassword()){
                    alertManager.alert(Duration.SINGLE, Severity.INFO, "Je wachtwoord is tijdelijk, en moet veranderd worden.");
                    return ChangePassword.class;
                }
                return toPreviousPage();
            } else if(!loginTry.getPassword().equals(PasswordDigester.getDigest(loginPword))){
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Ongeldige login; Probeer het opnieuw.");
            } else if(!loginTry.hasPermissionTo(Permission.CAN_LOGIN)){
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om in te loggen.");
            }
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Ongeldige login; Probeer het opnieuw.");
        }
        return this;
    }
    
    
    void onValidateFromFirstUserForm() throws ValidationException {
        if(!password.equals(confirmPassword)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "De ingevoerde wachtwoorden komen niet overeen.");
            throw new ValidationException("");
        }
    }
    
    @CommitAfter
    Object onSuccessFromFirstUserForm() throws NoSuchAlgorithmException {
        if(noUsers()){
            User user = new User(name, email, password, new HashSet<Permission>(Arrays.asList(Permission.values())));
            user.setTempPassword(false);
            
            session.persist(user);
        }
        
        alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De eerste gebruiker is succesvol aangemaakt.");
        
        return Index.class;
    }
    
    public User findUserWithUserEmail(String email){
        Criteria query = this.session.createCriteria(User.class);
        
        query.add(Restrictions.eq("deleted", false));
        query.add(Restrictions.eq("email", email));
        
        return (User) query.setMaxResults(1).uniqueResult();
    }

    public void setPreviousPage(Link previousPage) {
        this.previousPageLink = previousPage;
    }
    
    public Object toPreviousPage(){
        if(previousPageLink != null){
            String basePath = previousPageLink.getBasePath();
            Link linkBackToCallerTmp = previousPageLink.copyWithBasePath(basePath);
        
            componentResources.discardPersistentFieldChanges();
            return linkBackToCallerTmp;
        }
        return AssessmentOverview.class;
    }
    
    public boolean noUsers(){
        Criteria query = this.session.createCriteria(User.class);
        query.add(Restrictions.eq("deleted", false));
        
        return query.list().isEmpty();
    }
    
    public String getTitle(){
        return (noUsers()) ? "Eerste gebruiker aanmaken" : "";
    }
    
}
