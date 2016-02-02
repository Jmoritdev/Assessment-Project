package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.pages.userpages.AssessmentOverview;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import java.security.NoSuchAlgorithmException;
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
public class ChangePassword {
    
    @Inject
    private Session session;
    
    @Property
    private String oldPassword;
    
    @Property
    private String newPassword;
    
    @Property
    private String confirmNewPassword;
    
    @Inject
    private AlertManager alertManager;

    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    
    
    
    void onValidateFromChangePasswordForm() throws NoSuchAlgorithmException, ValidationException{
        User user = loggedInUser.getUser(session);
        if(!user.checkPassword(oldPassword)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Het oude wachtwoord is incorrect.");
            throw new ValidationException("");
        }
        if(!newPassword.equals(confirmNewPassword)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Het nieuwe wachtwoord kwam niet overeen met de bevestiging.");
            throw new ValidationException("");
        }
        if(newPassword.length() < 6 || newPassword.length() > 20){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Het nieuwe wachtwoord moet tussen de 6 en de 20 karakters bevatten.");
            throw new ValidationException("");
        }
    }
    
    @CommitAfter
    Object onSuccessFromChangePasswordForm() throws NoSuchAlgorithmException{
        User user = loggedInUser.getUser(session);
        if(user != null){
            user.setPassword(newPassword);
            
            if(user.hasTempPassword()){
                user.setTempPassword(false);
            }
            
            alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Wachtwoord succesvol gewijzigd!");
        }
        return AssessmentOverview.class;
    }
    
    public boolean isTempPassword(){
        return loggedInUser.getUser(session).hasTempPassword();
    }
}
