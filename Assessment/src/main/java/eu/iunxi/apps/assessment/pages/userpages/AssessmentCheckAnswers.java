package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.AssessmentAnswerOpen;
import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.QuestionOpen;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;

/**
 *
 * @author joey
 */
public class AssessmentCheckAnswers {

    @Property
    private Assessment assessment;

    @Property
    private Question question;

    @Property
    private AssessmentAnswer assessmentAnswer;

    @Property
    private int assignedPoints;

    @Property
    private String examinerAnswer;

    @Inject
    private AlertManager alertManager;
    
    @Inject
    private Session session;

    @InjectPage
    private AssessmentCheckAnswers coa;

    @Property
    private String userAnswer; //used in template

    @Property
    private String answerCheatSheet; //used in template
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;

    @InjectPage
    private AssessmentDetails assessmentDetails;

    Object[] onPassivate() {
        return new Object[]{ assessment, question };
    }

    public Object onActivate(Assessment assessment, Question question) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.ASSESSMENT_CHECK_ANSWERS)){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen na te kijken.");
            return AssessmentOverview.class;
        } 
        
        this.assessment = assessment;
        this.question = question;

        if (this.question != null) {
            this.assessmentAnswer = getAssessmentAnswer(this.question);
        } else {
            this.assessmentAnswer = getUncheckedAssessmentAnswer();
        }
        
        if(assessment == null){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Dit assessment bestaat niet.");
            return AssessmentOverview.class;
        }

        if (assessmentAnswer != null) {
            this.userAnswer = ((AssessmentAnswerOpen) assessmentAnswer).getUserAnswer();
            this.answerCheatSheet = ((QuestionOpen) assessmentAnswer.getQuestion()).getAnswerCheatSheet();
        }
        return null;
    }

    /**
     * returns an assessmentAnswer that has yet to be checked from this
     * assessment, or null if it doesn't have any.
     *
     * @return
     */
    public AssessmentAnswerOpen getUncheckedAssessmentAnswer() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion() instanceof QuestionOpen) {
                if (!((AssessmentAnswerOpen) aa).isChecked() && ((AssessmentAnswerOpen) aa).isAnswered()) {
                    return (AssessmentAnswerOpen) aa;
                }
            }
        }
        return null;
    }

    public void onValidateFromCheckForm() throws ValidationException {
        if (assignedPoints < 0 || assignedPoints > assessmentAnswer.getQuestion().getMaxScore()) {
            alertManager.alert(Duration.SINGLE, Severity.WARN, "Ongeldige waarde. "
                    + "De waarde mag niet lager zijn als 0 of hoger als het maximaal aantal punten van de vraag.");
            throw new ValidationException("Ongeldige waarde. De waarde mag niet lager zijn als 0 of hoger als het maximaal aantal punten van de vraag.");
        }
    }

    @CommitAfter
    Object onSuccessFromCheckForm() {
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.ASSESSMENT_CHECK_ANSWERS)){
            ((AssessmentAnswerOpen) assessmentAnswer).setAssignedPoints(assignedPoints);
            ((AssessmentAnswerOpen) assessmentAnswer).setExaminerAnswer(examinerAnswer);
            session.persist(assessmentAnswer);


            if (getUncheckedAssessmentAnswer() == null) {
                assessmentDetails.setThisAssessment(assessment);
                alertManager.alert(Duration.SINGLE, Severity.INFO, "Alle vragen zijn nagekeken.");
                return assessmentDetails;
            }
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen na te kijken.");
            return AssessmentOverview.class;
        }
        return coa;
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
}
