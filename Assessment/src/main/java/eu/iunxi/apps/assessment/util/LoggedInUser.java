package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.User;
import org.hibernate.Session;

/**
 *
 * @author joey
 */
public class LoggedInUser {
    
    private Integer userId;
    
    public LoggedInUser(){
        this.userId = null;
    }
    
    public void setUser(User user){
        this.userId = user.getId();
    }
    
    public User getUser(Session session){
        if(userId != null || userId != 0){
            return (User) session.get(User.class, userId);
        }
        return null;
    }
    
    public Integer getId(){
        return userId;
    }
}
