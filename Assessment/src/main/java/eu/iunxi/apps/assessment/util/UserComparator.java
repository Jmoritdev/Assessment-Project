package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.User;
import java.util.Comparator;

/**
 *
 * @author joey
 */
public class UserComparator implements Comparator<User>{

    @Override
    public int compare(User o1, User o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
    
}
