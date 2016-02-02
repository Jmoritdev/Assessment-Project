package eu.iunxi.apps.assessment.util;

import eu.iunxi.apps.assessment.model.Category;
import java.util.Comparator;

/**
 *
 * @author joey
 */
public class CategoryComparator implements Comparator<Category>{
    
    @Override
    public int compare(Category o1, Category o2) {
        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
    }
    
}
