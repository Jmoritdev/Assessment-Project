package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.Question.Difficulty;
import eu.iunxi.apps.assessment.model.QuestionClosed;
import eu.iunxi.apps.assessment.model.QuestionOpen;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.AssessmentAnswerComparator;
import eu.iunxi.apps.assessment.util.AssessmentAnswerComparator.SortBy;
import eu.iunxi.apps.assessment.util.CategoryComparator;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
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
public class AssessmentAddQuestionsBulk {

    @Property
    private Assessment assessment;

    @Property
    private Category loopedCategory;

    private Criteria questionQuery;

    @Inject
    private Session session;

    @Inject
    private AlertManager alertManager;

    private int assessmentSize;
    
    @InjectPage
    private AssessmentDetails assessmentDetails; 
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;

    
    Object[] onPassivate() {
        return new Object[]{assessment};
    }

    public Object onActivate(Assessment assessment) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen toe te voegen aan assessments.");
            return AssessmentOverview.class;
        }
        
        this.assessment = assessment;
        
        if(assessment == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Dit assessment bestaat niet.");
            return AssessmentOverview.class;
        }

        assessmentSize = this.assessment.getAssessmentAnswers().size();
        
        return null;
    }

    @SetupRender
    public void setupRender() {
        this.questionQuery = getQuestionQuery();
    }

    /**
     * returns all the categories
     *
     * @return
     */
    public List<Category> getCategories() {
        Criteria query = this.session.createCriteria(Category.class);
        query.add(Restrictions.eq("deleted", false));
        
        List<Category> list = new LinkedList<Category>();
        list.addAll(query.list());
        
        Collections.sort(list, new CategoryComparator());
        
        return list;
    }

    /**
     * returns all the questions
     *
     * @return
     */
    public Criteria getQuestionQuery() {
        Criteria query = this.session.createCriteria(Question.class);
        query.add(Restrictions.eq("deleted", false));
        return query;
    }

    @CommitAfter
    Object onSuccessFromBulkAddForm() {
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            List<AssessmentAnswer> list = new LinkedList<AssessmentAnswer>();
            list.addAll(this.assessment.getAssessmentAnswers());
            Collections.shuffle(list);

            Collections.sort(list, new AssessmentAnswerComparator(SortBy.DIFFICULTY));
            Collections.sort(list, new AssessmentAnswerComparator(SortBy.CATEGORY));

            assessment.setAssessmentAnswers(list);

            for (AssessmentAnswer aa : this.assessment.getAssessmentAnswers()) {
                session.persist(aa);
            }

            alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Succesvol " + (this.assessment.getAssessmentAnswers().size() - assessmentSize) + " vragen toegevoegd.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen toe te voegen aan assessments.");
        }
        assessmentDetails.setThisAssessment(assessment);
        return assessmentDetails;
    }

    public void setNumberJuniorOpenQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.JUNIOR, number, 0);
    }

    public int getNumberJuniorOpenQuestions() {
        return 0;
    }

    public void setNumberJuniorClosedQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.JUNIOR, 0, number);
    }

    public int getNumberJuniorClosedQuestions() {
        return 0;
    }

    public void setNumberMediorOpenQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.MEDIOR, number, 0);
    }

    public int getNumberMediorOpenQuestions() {
        return 0;
    }

    public void setNumberMediorClosedQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.MEDIOR, 0, number);
    }

    public int getNumberMediorClosedQuestions() {
        return 0;
    }

    public void setNumberSeniorOpenQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.SENIOR, number, 0);
    }

    public int getNumberSeniorOpenQuestions() {
        return 0;
    }

    public void setNumberSeniorClosedQuestions(int number) {
        this.assessment.addRandomQuestions(this.loopedCategory, Question.Difficulty.SENIOR, 0, number);
    }

    public int getNumberSeniorClosedQuestions() {
        return 0;
    }

    public int getTotalQuestionsJuniorOpen() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionOpen && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.JUNIOR)) {
                count++;
            }
        }

        return count;
    }

    public int getTotalQuestionsJuniorClosed() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionClosed && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.JUNIOR)) {
                count++;
            }
        }

        return count;
    }

    public int getTotalQuestionsMediorOpen() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionOpen && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.MEDIOR)) {
                count++;
            }
        }

        return count;
    }

    public int getTotalQuestionsMediorClosed() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionClosed && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.MEDIOR)) {
                count++;
            }
        }

        return count;
    }

    public int getTotalQuestionsSeniorOpen() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionOpen && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.SENIOR)) {
                count++;
            }
        }

        return count;
    }

    public int getTotalQuestionsSeniorClosed() {
        List<Question> list = questionQuery.list();

        int count = 0;
        for (Question q : list) {
            if (q instanceof QuestionClosed && q.getCategory().equals(loopedCategory) && q.getDifficulty().equals(Difficulty.SENIOR)) {
                count++;
            }
        }

        return count;
    }
    
    public void setThisAssessment(Assessment assessment){
        this.assessment = assessment;
    }
}
