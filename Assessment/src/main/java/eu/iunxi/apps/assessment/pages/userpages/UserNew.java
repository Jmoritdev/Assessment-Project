package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.MyOptionModel;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.util.EnumValueEncoder;
import org.hibernate.Session;

/**
 *
 * @author joey
 */
public class UserNew {
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Property
    private User user;
    
    @Inject
    private AlertManager alertManager;
    
    @Inject 
    private Session session;
    
    private List<Permission> selectedPermissions;   
    
    private boolean isNewUser;
    
    
    Object[] onPassivate(){
        return new Object[] { user };
    }
    
    public Object onActivate(User user){
        this.user = user;
        
        if(user == null){
            this.user = new User();
            isNewUser = true;
        }
        
        User userAlt = loggedInUser.getUser(session);
        if(userAlt == null || !userAlt.hasPermissionTo(Permission.USER_EDIT)){
            if(isNewUser){
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers aan te maken.");
            } else {
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te wijzigen.");
            }
            return AssessmentOverview.class;
        }
        
        return null;
    }
    
    public void onValidateFromUserForm() throws ValidationException{
        User user = loggedInUser.getUser(session);
        if(user != null && !user.hasPermissionTo(Permission.USER_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te wijzigen of aan te maken.");
            throw new ValidationException("");
        }
    }
    
    @CommitAfter
    public Object onSuccessFromUserForm(){
        User loggedUser = loggedInUser.getUser(session);
        if(loggedUser != null && loggedUser.hasPermissionTo(Permission.USER_EDIT)){
            user.setPermissions(new HashSet<Permission>(selectedPermissions));
            user.setTempPassword(isNewUser);
            session.persist(user);
            if (isNewUser){
                alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De gebruiker \""+this.user.getName()+"\" is succesvol aangemaakt.");
            } else {
                alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De gebruiker \""+this.user.getName()+"\" is succesvol aangepast.");
            }
        } else if(isNewUser){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers aan te maken.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om gebruikers te wijzigen.");
        }
        
        return UserOverview.class;
    }
    
    @Inject
    private TypeCoercer typeCoercer;
    
    public SelectModel getPermissionModel() {
        List<OptionModel> options = new LinkedList<OptionModel>();
        
        for(Permission p : Arrays.asList(Permission.values())){
            options.add(new MyOptionModel(p.getDescription(), p, false));
        }

        return new SelectModelImpl(null, options);
    }
    
    public ValueEncoder<Permission> getPermissionEncoder() {
        return new EnumValueEncoder<Permission>(typeCoercer, Permission.class);
    }
    
    public List<Permission> getSelectedPermissions(){
        List<Permission> list = new LinkedList<Permission>();
        
        for(Permission p : Arrays.asList(Permission.values())){
            if(user.hasPermissionTo(p)) list.add(p);
        }
        
        return list;
    }
    
    public void setSelectedPermissions(List<Permission> permissions){
        this.selectedPermissions = permissions;
    }
    
    public String getTitle(){
        return (user.getName() == null) ? "Maak een nieuwe gebruiker" : "Bewerk een gebruiker";
    } 
}
