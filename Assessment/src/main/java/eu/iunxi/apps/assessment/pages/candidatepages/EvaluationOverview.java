package eu.iunxi.apps.assessment.pages.candidatepages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.Question;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * @author joey
 */
@Import(library = {"context:scripts/evaluationOverviewFunctions.js"})
public class EvaluationOverview {

    @Property
    private Assessment assessment;

    @InjectPage
    private Evaluation evaluation;

    @Inject
    private Session session;

    private Criteria query;

    @Property
    private Category loopedCategory;

    @Property
    private Question loopedQuestion;

    @Property
    @Persist
    private Question previousQuestion;

    @Property
    @SessionAttribute
    private DateTime startTime;

    @InjectPage
    private EvaluationOverview evaluationOverview;
    
    @Inject
    private AlertManager alertManager;
        

    Object[] onPassivate() {
        return new Object[]{assessment.getUid()};
    }

    public void onActivate(String uid) {
        this.assessment = Assessment.getByUid(uid, session);
    }

    @SetupRender
    public void setupRender() {
        this.query = getQuery();
    }

    public List<Category> getCategoriesFromAssessment() {
        List<Category> list = new LinkedList<Category>();

        for (Question q : assessment.getQuestions()) {
            if (!list.contains(q.getCategory())) {
                list.add(q.getCategory());
            }
        }

        return list;
    }

    private Criteria getQuery() {
        Criteria query = this.session.createCriteria(Category.class);
        query.add(Restrictions.eq("deleted", false));
        return query;
    }

    /**
     * returns the questions from this assessment that also are in the specified
     * category
     *
     * @return a list with questions
     */
    public List<Question> getQuestionsFromCategory() {
        List<Question> list = new LinkedList<Question>();

        for (Question q : assessment.getQuestions()) {
            if (q.getCategory().equals(loopedCategory)) {
                list.add(q);
            }
        }

        return list;
    }

    /**
     * returns the assessmentAnswer that belongs to a question
     *
     * @param question the question to get the assessmentAnswer from
     * @return the assessmentAnswer
     */
    private AssessmentAnswer getAssessmentAnswer(Question question) {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion().equals(question)) {
                return aa;
            }
        }
        return null;
    }

    public boolean isAnswered() {
        return (getAssessmentAnswer(loopedQuestion).isAnswered());
    }

    public int getNumberWithQuestion() {
        List<Question> list = assessment.getQuestions();
        return list.indexOf(loopedQuestion) + 1;
    }

    public void setParameters(Assessment assessment, Question question) {
        this.assessment = assessment;
        this.previousQuestion = question;
    }

    /**
     * returns the amount of time the test subject is taking. this is called
     * every second
     *
     * @return
     */
    public String getDuration() {
        if (startTime == null) {
            startTime = new DateTime();
        }
        Period p = new Period(startTime, new DateTime());
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours().appendSeparator(":")
                .appendMinutes().appendSeparator(":")
                .appendSeconds()
                .toFormatter();
        return formatter.print(p);
    }

    /**
     * returns the amount of questions the test subject has answered
     *
     * @return
     */
    public int getAmountOfQuestionsAnswered() {
        int answered = 0;
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.isAnswered()) {
                answered += 1;
            }
        }
        return answered;
    }

    /**
     * returns the progress of the evaluation in percentages
     *
     * @return
     */
    public int getPercentageDone() {
        int amountOfQuestionsAnswered = getAmountOfQuestionsAnswered();

        if (amountOfQuestionsAnswered == 0) {
            return 0;
        } else if (amountOfQuestionsAnswered == assessment.getAssessmentAnswers().size()
                || amountOfQuestionsAnswered > assessment.getAssessmentAnswers().size()) {
            return 100;
        } else {
            return (int) (100.0 / assessment.getAssessmentAnswers().size() * amountOfQuestionsAnswered);
        }
    }

    public boolean isAllQuestionsAnswered() {
        return getAmountOfQuestionsAnswered() == assessment.getAssessmentAnswers().size();
    }

    @CommitAfter
    Object onCloseAssessment() {
        assessment.setClosed(true);
        assessment.setDuration(new DateTime().getMillis() - startTime.getMillis());
        startTime = null;
        session.persist(assessment);
        alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Je assessment is succesvol ingeleverd en gesloten.");
        return evaluationOverview;
    }
}
