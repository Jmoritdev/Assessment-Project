package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.QuestionClosed;
import eu.iunxi.apps.assessment.model.QuestionClosedMultiple;
import eu.iunxi.apps.assessment.model.QuestionClosedSingle;
import eu.iunxi.apps.assessment.model.QuestionOpen;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import eu.iunxi.apps.assessment.util.QuestionComparator;
import eu.iunxi.apps.assessment.util.QuestionComparator.SortBy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
@Import(library = {"context:scripts/categoryDetailsFunctions.js"})
public class CategoryDetails {

    @Property
    private Category category;

    @Property
    private Question loopedQuestion;

    private Criteria query;

    @Inject
    private Session session;

    @Property
    @ActivationRequestParameter
    private String searchString;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Property
    private final Permission questionEditPermission = Permission.QUESTION_EDIT; //used in template
    
    @Property
    private final Permission questionRemovePermission = Permission.QUESTION_REMOVE; //used in template
    
    @Inject
    private AlertManager alertManager;
    

    Object[] onPassivate() {
        return new Object[]{ category };
    }

    public Object onActivate(Category category) {
        this.category = category;

        if (this.category == null) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze categorie bestaat niet.");
            return CategoryOverview.class; 
        }
        return null;
    }

    public String getTitle() {
        return "Details van de " + category.getTitle() + " categorie";
    }

    @SetupRender
    public void setupRender() {
        query = getQuery();
    }

    public Criteria getQuery() {
        Criteria query = this.session.createCriteria(Question.class);
        query.add(Restrictions.eq("deleted", false));
        query.add(Restrictions.eq("category", this.category));

        if (searchString != null) {
            query.add(Restrictions.or(Restrictions.ilike("description", searchString, MatchMode.ANYWHERE),
                                      Restrictions.ilike("title", searchString, MatchMode.ANYWHERE)));
        }

        return query;
    }

    public List<Question> getQuestions() {
        List<Question> list = new LinkedList<Question>();
        list.addAll(query.list());

        Collections.sort(list, new QuestionComparator(SortBy.TITLE));

        return list;
    }

    public String getTypeOfQuestion() {
        if (loopedQuestion instanceof QuestionOpen) {
            return "Open";
        }
        if (loopedQuestion instanceof QuestionClosed) {
            return "Gesloten";
        }
        return null;
    }

    public boolean isClosed() {
        return loopedQuestion instanceof QuestionClosed;
    }

    public boolean isClosedMultiple() {
        return loopedQuestion instanceof QuestionClosedMultiple;
    }

    public boolean isClosedSingle() {
        return loopedQuestion instanceof QuestionClosedSingle;
    }

    public void setThisCategory(Category category){
        this.category = category;
    }
    
    /**
     * @return the amount of questions in this category
     */
    public int getAmountOfQuestions() {
        return category.getQuestions().size();
    }
    
    public User getUser(){ // used in template
        return loggedInUser.getUser(session);
    }
}
