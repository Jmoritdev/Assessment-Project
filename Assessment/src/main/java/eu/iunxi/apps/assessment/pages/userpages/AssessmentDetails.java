package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.AssessmentAnswerClosedMultiple;
import eu.iunxi.apps.assessment.model.AssessmentAnswerClosedSingle;
import eu.iunxi.apps.assessment.model.AssessmentAnswerOpen;
import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.QuestionClosed;
import eu.iunxi.apps.assessment.model.QuestionClosedMultiple;
import eu.iunxi.apps.assessment.model.QuestionClosedOption;
import eu.iunxi.apps.assessment.model.QuestionClosedSingle;
import eu.iunxi.apps.assessment.model.QuestionOpen;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.pages.candidatepages.Evaluation;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.List;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * @author joey
 */
@Import(library = {"context:scripts/assessmentDetailsFunctions.js"})
public class AssessmentDetails {

    @Property
    private Assessment assessment;

    @Property
    private Question question;

    @Property
    private QuestionClosedOption option;

    @InjectPage
    private AssessmentOverview assessmentOverview;

    @Inject
    private AlertManager alertManager;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Inject
    private Session session;
    
    @Property
    private final Permission assessmentDeletePermission = Permission.ASSESSMENT_REMOVE; //used in template
    
    @Property
    private final Permission assessmentEditPermission = Permission.ASSESSMENT_EDIT; //used in template
    
    @Property
    private final Permission checkAssessmentAnswersPermission = Permission.ASSESSMENT_CHECK_ANSWERS; //used in template
    
    final static String COLOR_CORRECT = "#9ffa9e"; //green
    final static String COLOR_PARTIALLY_CORRECT = "#fef79a"; //yellow
    final static String COLOR_INCORRECT = "#ff8080"; //red
    final static String COLOR_ATTENTION = "#99ccff"; //blue
    

    Object[] onPassivate() {
        return new Object[]{ assessment };
    }
    
    Object onActivate(Assessment assessment) {
        this.assessment = assessment;
        
        if (this.assessment == null) {
            alertManager.alert(Duration.SINGLE, Severity.INFO, "Dit assessment bestaat niet.");
            return assessmentOverview;
        }

        if (this.assessment.isDeleted()) {
            alertManager.alert(Duration.SINGLE, Severity.INFO, "Dit assessment is verwijderd en is niet meer toegankelijk.");
            return assessmentOverview;
        }

        return null;
    }

    public String getTypeOfQuestion() {
        return (question instanceof QuestionOpen) ? "Open" : "Gesloten";
    }

    public boolean isClosed() {
        return question instanceof QuestionClosed;
    }

    public boolean isClosedMultiple() {
        return question instanceof QuestionClosedMultiple;
    }

    public boolean isClosedSingle() {
        return question instanceof QuestionClosedSingle;
    }

    public List<QuestionClosedOption> getOptionsFromQuestion() {
        return ((QuestionClosed) question).getOptions();
    }

    //can only be called if question is an instance of QuestionOpen
    public String getUserAnswer() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa instanceof AssessmentAnswerOpen && aa.getQuestion().equals(question)) {
                return ((AssessmentAnswerOpen) aa).getUserAnswer();
            }
        }
        return null;
    }

    //can only be called if question is an instance of QuestionOpen
    public Integer getAssignedPointsAlt() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa instanceof AssessmentAnswerOpen && aa.getQuestion().equals(question)) {
                return ((AssessmentAnswerOpen) aa).getAssignedPoints();
            }
        }
        return 0;
    }
    
    /**
     * returns a string that represents a color based on the answer of the
     * question.
     *
     * @return
     */
    public String getQuestionStatusColor() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion().equals(question)) {
                
                if (aa instanceof AssessmentAnswerOpen) {
                    if (((AssessmentAnswerOpen) aa).getUserAnswer() == null) {
                        return "";//not answered
                    } else if (((AssessmentAnswerOpen) aa).getAssignedPoints() == null) {
                        return COLOR_ATTENTION;
                    } else if(((AssessmentAnswerOpen)aa).getAssignedPoints() == aa.getQuestion().getMaxScore()){
                        return COLOR_CORRECT;
                    } else if (((AssessmentAnswerOpen)aa).getAssignedPoints() < aa.getQuestion().getMaxScore() 
                            && ((AssessmentAnswerOpen)aa).getAssignedPoints() != 0 ){
                        return COLOR_PARTIALLY_CORRECT;
                    } else if ( ((AssessmentAnswerOpen)aa).getAssignedPoints() == 0 ){
                        return COLOR_INCORRECT;
                    }
                }

                if (aa instanceof AssessmentAnswerClosedSingle) {
                    if (((AssessmentAnswerClosedSingle) aa).getSelectedOption() == null) {
                        return "";//not answered
                    } else if (!((AssessmentAnswerClosedSingle) aa).getSelectedOption().isCorrect()) {
                        return COLOR_INCORRECT;
                    } else {
                        return COLOR_CORRECT;
                    }
                }

                if (aa instanceof AssessmentAnswerClosedMultiple) {
                    if (((AssessmentAnswerClosedMultiple) aa).getSelectedOptions().isEmpty()) {
                        return "";//not answered
                    } else if (((AssessmentAnswerClosedMultiple) aa).getSelectedOptions().containsAll(((QuestionClosedMultiple) (((AssessmentAnswerClosedMultiple) aa).getQuestion())).getCorrectOptions())) {
                        //TODO make above else if statement smaller if possible
                        return COLOR_CORRECT;
                    } else {
                        for (QuestionClosedOption qco : ((AssessmentAnswerClosedMultiple) aa).getSelectedOptions()) {
                            if (qco.isCorrect()) {
                                return COLOR_PARTIALLY_CORRECT;
                            }
                        }
                    }
                    return COLOR_INCORRECT;
                }

            }
        }
        return null;
    }

    //has "Alt" behind its name so it doesnt interfere with tapestry's @Property automatic getter's and setter's
    public String getExaminerAnswerAlt() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion().equals(question)) {
                return ((AssessmentAnswerOpen) aa).getExaminerAnswer();
            }
        }
        return "Geen";
    }

    /**
     * returns if an option is selected or not
     *
     * @return
     */
    public boolean isSelected() {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion().equals(question) && aa instanceof AssessmentAnswerClosedSingle) {
                if (option.equals(((AssessmentAnswerClosedSingle) aa).getSelectedOption())) {
                    return true;
                }
            }
            if (aa.getQuestion().equals(question) && aa instanceof AssessmentAnswerClosedMultiple) {
                if (((AssessmentAnswerClosedMultiple) aa).getSelectedOptions().contains(option)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Inject
    private PageRenderLinkSource pageRenderLinkSource;
    
    @Inject
    private Request request;
    
    public String getAssessmentLink() {
//        return "localhost:8080/Assessment/candidatepages/Evaluation/" + assessment.getUid() + "/" + assessment.getAssessmentAnswers().get(0).getQuestion().getId();
        return request.getServerName()+":"+
               request.getServerPort()+pageRenderLinkSource.createPageRenderLinkWithContext(Evaluation.class, assessment.getUid()).toString()
               +"/"+assessment.getAssessmentAnswers().get(0).getQuestion().getId();
    }

    public String getStatus() {
        if (assessment.isClosed()) {
            return "Gesloten";
        } else if (amountOfQuestionsAnswered() > 0 && assessment.getPersonEmail() != null) {
            return "Bezig";
        }
        return "Nog niet gemaakt";
    }

    public int amountOfQuestionsAnswered() {
        int amount = 0;
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.isAnswered()) {
                amount++;
            }
        }
        return amount;
    }

    public void setThisAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public int getNumberWithQuestion() {
        List<Question> list = assessment.getQuestions();
        return list.indexOf(question) + 1;
    }

    /**
     * returns the assessmentAnswer that belongs to a question
     *
     * @param question the question to get the assessmentAnswer from
     * @return the assessmentAnswer
     */
    public AssessmentAnswer getAssessmentAnswer(Question question) {
        for (AssessmentAnswer aa : assessment.getAssessmentAnswers()) {
            if (aa.getQuestion().equals(question)) {
                return aa;
            }
        }
        return null;
    }

    /**
     * returns the amount of points the test subject has scored for a specific
     * question.
     *
     * @return
     */
    public String getPointsScored() {
        int aa = getAssessmentAnswer(this.question).getPointsScored();
       
        if(getAssessmentAnswer(this.question) instanceof AssessmentAnswerOpen){
            AssessmentAnswerOpen aao = ((AssessmentAnswerOpen)getAssessmentAnswer(this.question));
            if(aao.isChecked()){
                return aao.getPointsScored()+"";
            } else if(aao.isAnswered()) {
                return "Moet nagekeken worden";
            } else {
                return "Niet beantwoord";
            }
        } else {
            return (getAssessmentAnswer(this.question).isAnswered()) ? getAssessmentAnswer(this.question).getPointsScored()+"" : "Niet beantwoord" ;
        }
    }
    
    @CommitAfter
    public void onSuccessFromNewSubjectForm(){
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            assessment.setPersonEmail(assessment.getPersonEmail());
            assessment.setPersonName(assessment.getPersonName());
        }
    }
    
    public User getUser(){ // used in template
        return loggedInUser.getUser(session);
    }
    
    public int getFirstQuestionId(){
        return assessment.getAssessmentAnswers().get(0).getQuestion().getId();
    }
    
    /**
     * 
     * @return a timestamp that indicates how long it took the participant to complete the assessment 
     */
    public String getDurationTimestamp(){
        Period p = new Period(assessment.getDuration());
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendHours().appendSuffix(" uur").appendSeparator(", ", " en ")
                .appendMinutes().appendSuffix(" minuut", " minuten").appendSeparator(" en ")
                .appendSeconds().appendSuffix(" seconde", " seconden")
                .toFormatter();
        return formatter.print(p);
    }
}
