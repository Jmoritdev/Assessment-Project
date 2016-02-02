package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.Question.Difficulty;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.AssessmentAnswerComparator;
import eu.iunxi.apps.assessment.util.CategoryComparator;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.MyOptionModel;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
//TODO this page needs a better name
public class AssessmentAddQuestions {

    @Property
    private Assessment assessment;

    @Property
    private int openQuestionsPerCategory;

    @Property
    private int closedQuestionsPerCategory;

    @Inject
    private Session session;

    @Property
    private Difficulty difficulty;

    @Property
    private List<Category> categoryList;

    @Property
    private Category category;

    @Inject
    private AlertManager alertManager;
    
    @InjectPage
    private AssessmentDetails assessmentDetails; 
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;


    Object[] onPassivate() {
        return new Object[]{ assessment };
    }

    public Object onActivate(Assessment assessment) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen toe te voegen aan assessments.");
            return AssessmentOverview.class;
        }
        
        this.assessment = assessment;

        if (categoryList == null) {
            categoryList = new LinkedList<Category>();
        }
        
        if(assessment == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Deze assessment bestaat niet.");
            return AssessmentOverview.class;
        }
        
        return null;
    }

    public SelectModel getCategoryModel() {
        List<OptionModel> options = new LinkedList<OptionModel>();

        Criteria query = this.session.createCriteria(Category.class);
        query.add(Restrictions.eq("deleted", false));
        List<Category> categories = query.list();
        
        Collections.sort(categories, new CategoryComparator());

        for (Category category : categories) {
            options.add(new MyOptionModel(category.getTitle(), category, false));
        }
        return new SelectModelImpl(null, options);
    }

    public SelectModel getDifficultyModel() {
        List<OptionModel> options = new LinkedList<OptionModel>();

        options.add(new MyOptionModel(difficulty.JUNIOR.toString(), difficulty.JUNIOR, false));
        options.add(new MyOptionModel(difficulty.MEDIOR.toString(), difficulty.MEDIOR, false));
        options.add(new MyOptionModel(difficulty.SENIOR.toString(), difficulty.SENIOR, false));

        return new SelectModelImpl(null, options);
    }

    @CommitAfter
    Object onSuccessFromAddQuestionsForm() {
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            int beforeCount = assessment.getAssessmentAnswers().size();

            //add questions
            assessment.addRandomQuestions(category, difficulty, openQuestionsPerCategory, closedQuestionsPerCategory);

            //shuffle then sort everything
            List<AssessmentAnswer> list = new LinkedList<AssessmentAnswer>();
            list.addAll(this.assessment.getAssessmentAnswers());
            Collections.shuffle(list);
            Collections.sort(list, new AssessmentAnswerComparator(AssessmentAnswerComparator.SortBy.DIFFICULTY));
            Collections.sort(list, new AssessmentAnswerComparator(AssessmentAnswerComparator.SortBy.CATEGORY));
            assessment.setAssessmentAnswers(list);


            for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
                session.persist(aa);
            }

            session.persist(assessment);

            alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Succesvol " + (this.assessment.getAssessmentAnswers().size() - beforeCount) + " vragen toegevoegd");

        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen toe te voegen aan assessments.");
        }
        assessmentDetails.setThisAssessment(assessment);
        return assessmentDetails;
    }
    
}
