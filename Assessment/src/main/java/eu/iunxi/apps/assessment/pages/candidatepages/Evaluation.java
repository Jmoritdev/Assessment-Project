package eu.iunxi.apps.assessment.pages.candidatepages;

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
import eu.iunxi.apps.assessment.util.AssessmentAnswerComparator;
import eu.iunxi.apps.assessment.util.AssessmentAnswerComparator.SortBy;
import eu.iunxi.apps.assessment.util.MyOptionModel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 *
 * @author joey
 */
public class Evaluation {

    @Property
    private QuestionClosedOption loopedOption;

    @Property
    private Assessment assessment;

    @Property
    private Question question;

    @Inject
    private Session session;

    @InjectPage
    private Evaluation evaluation;

    @Property
    @SessionAttribute
    private DateTime startTime;

    @Property
    private boolean newPerson;

    //used in an open question; otherwise null;
    @Property
    private String userAnswer;

    @Property
    private QuestionClosedOption selectedOption;

    @Property
    private List<QuestionClosedOption> selectedOptions;

    @Inject
    private AlertManager alertManager;

    @SessionAttribute
    private List<AssessmentAnswer> assessmentAnswers;

    @InjectPage
    private EvaluationOverview evaluationOverview;
    
    @Property
    private int questionProgress; //used in template

    //==================================================
    
    public String getTitle() {
        if (question != null && !assessment.isClosed()) {
            return /*question.getTitle()*/ "";
        } else if (assessment.isClosed()) {
            return /*"gesloten"*/ "";
        } else if(assessment.getPersonName() == null || assessment.getPersonEmail() == null) {
            return "Welkom!";
        }
        return "";
    }

    Object[] onPassivate() {
        return new Object[]{ assessment.getUid(), question };
    }

    public void onActivate(String uid, Question question) {
        this.assessment = Assessment.getByUid(uid, session);
        this.question = question;

        assessmentAnswers = assessment.getAssessmentAnswers();
        Collections.sort(assessmentAnswers, new AssessmentAnswerComparator(SortBy.DIFFICULTY));
        Collections.sort(assessmentAnswers, new AssessmentAnswerComparator(SortBy.CATEGORY));

        if (assessment.getPersonName() == null || assessment.getPersonEmail() == null) {
            newPerson = true;
        }

        if (question instanceof QuestionOpen) {
            for (AssessmentAnswer aa : assessmentAnswers) {
                if (aa.getQuestion().equals(this.question)) {
                    userAnswer = ((AssessmentAnswerOpen) aa).getUserAnswer();
                } else if (userAnswer == null) {
                    userAnswer = "";
                }
            }
        }

        if (question instanceof QuestionClosedMultiple) {
            for (AssessmentAnswer aa : assessmentAnswers) {
                if (aa.getQuestion().equals(this.question)) {
                    for (QuestionClosedOption qco : ((AssessmentAnswerClosedMultiple) aa).getSelectedOptions()) {
                        if (selectedOptions == null) {
                            selectedOptions = new LinkedList<QuestionClosedOption>();
                        }
                        selectedOptions.add(qco);
                    }
                }
            }
        }

        //if this is after the testperson has filled in its name and email
        if (assessment.getPersonName() != null && question == null && !assessmentAnswers.isEmpty()) {
            this.question = assessmentAnswers.get(0).getQuestion();
        }
        
        questionProgress = assessmentAnswers.indexOf(getAssessmentAnswer(this.question)) + 1;
    }

    @CommitAfter
    Object onSuccessFromPersonForm() {
        newPerson = !newPerson;
        return evaluation;
    }

    public void onValidateFromAnswerForm() throws ValidationException {
        if (question instanceof QuestionClosedMultiple) {
            if (selectedOptions.size() > ((QuestionClosedMultiple) question).getAmountOfCorrectAnswers() && selectedOptions.size() != 1) {
                //TODO make this alert unnessecary
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je mag niet meer dan "
                        + ((QuestionClosedMultiple) question).getAmountOfCorrectAnswers()
                        + " antwoorden selecteren in deze vraag.");
                throw new ValidationException("");
            }
        }
    }

    @CommitAfter
    Object onSuccessFromAnswerForm() {
        if (selectedOptions != null) {
            for (AssessmentAnswer aa : assessmentAnswers) {
                if (aa.getQuestion().equals(question)) {

                    //empty the previous list of selected answers
                    ((AssessmentAnswerClosedMultiple) aa).setSelectedOptions(new LinkedList<QuestionClosedOption>());

                    for (QuestionClosedOption qco : selectedOptions) {
                        ((AssessmentAnswerClosedMultiple) aa).addSelectedOption(qco);
                    }
                    for (QuestionClosedOption qco : ((QuestionClosed) question).getOptions()) {
                        if (!selectedOptions.contains(qco)) {
                            ((AssessmentAnswerClosedMultiple) aa).removeSelectedOption(qco);
                        }
                    }
                    session.persist(aa);
                    return setNextQuestion();

                }
            }
        }
        if (selectedOption != null) {
            for (AssessmentAnswer aa : assessmentAnswers) {
                if (aa.getQuestion().equals(question)) {
                    ((AssessmentAnswerClosedSingle) aa).setSelectedOption(selectedOption);
                    session.persist(aa);
                    return setNextQuestion();
                }
            }
        }
        if (userAnswer != null) {
            for (AssessmentAnswer aa : assessmentAnswers) {
                if (aa.getQuestion().equals(question)) {
                    ((AssessmentAnswerOpen) aa).setUserAnswer(userAnswer);
                    ((AssessmentAnswerOpen) aa).setAssignedPoints(null);
                    ((AssessmentAnswerOpen) aa).setExaminerAnswer(null);
                    session.persist(aa);
                    return setNextQuestion();

                }
            }
        }
        return evaluation;
    }

    /**
     * returns the options that belong to the current question. Can only be
     * called if the question is an instance of QuestionClosed.
     *
     * @return the list with options
     */
    public List<QuestionClosedOption> getOptions() {
        for (AssessmentAnswer aa : assessmentAnswers) {
            if (aa.getQuestion().equals(question) && !isOpen()) {
                return ((QuestionClosed) question).getOptions();
            }
        }
        return null;
    }

    public boolean isOpen() {
        return (question instanceof QuestionOpen);
    }

    public boolean isClosedSingle() {
        return (question instanceof QuestionClosedSingle);
    }

    public boolean isClosedMultiple() {
        return (question instanceof QuestionClosedMultiple);
    }

    public SelectModel getOptionModel() {
        List<OptionModel> options = new LinkedList<OptionModel>();

        for (QuestionClosedOption qco : getOptions()) {
            if (!qco.isDeleted()) {
                options.add(new MyOptionModel(qco.getDescription(), qco, false));
            }
        }

        return new SelectModelImpl(null, options);
    }

    public ValueEncoder<QuestionClosedOption> getOptionEncoder() {
        final QuestionClosed question = (QuestionClosed) this.question;

        return new ValueEncoder<QuestionClosedOption>() {
            @Override
            public String toClient(QuestionClosedOption v) {
                return v == null || v.getId() == 0 ? null : String.valueOf(v.getId());
            }

            @Override
            public QuestionClosedOption toValue(String string) {
                return string == null ? new QuestionClosedOption(question)
                        : (QuestionClosedOption) session.get(QuestionClosedOption.class, Integer.valueOf(string));
            }
        };
    }

    /**
     * sets the next question the test subject has to answer. 
     * It prioritizes the current category it's in if there is a question left to answer in that category. 
     * @return
     */
    public Object setNextQuestion() {

        for (AssessmentAnswer aa : assessmentAnswers) {
            if (aa.getQuestion().getCategory().equals(this.question.getCategory()) && !aa.isAnswered()) { //check if there is a question still 
                this.question = aa.getQuestion();                                                       //unanswered that is in the same category
                return evaluation;
            }
        }

        for (AssessmentAnswer aa : assessmentAnswers) { //if there still hasn't been found a question left to answer...
            if (!aa.isAnswered()) {
                this.question = aa.getQuestion();
                return evaluation;
            }
        }

        //if all questions have been answered...
        return onNavigateToOverview();
    }
    

    
    /**
     * returns true if the radio was previously selected by the test subject.
     *
     * @return
     */
    public boolean isSelected() {
        for (AssessmentAnswer aa : assessmentAnswers) {
            if (aa.getQuestion().equals(question) && aa.getQuestion() instanceof QuestionClosedSingle) {
                if (((AssessmentAnswerClosedSingle) aa).getSelectedOption() != null) {
                    return ((AssessmentAnswerClosedSingle) aa).getSelectedOption().equals(loopedOption);
                }
            }
        }

        return false;
    }

    /**
     * returns the amount of questions the test subject has answered
     *
     * @return
     */
    public int getAmountOfQuestionsAnswered() {
        int answered = 0;
        for (AssessmentAnswer aa : assessmentAnswers) {
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
        } else if (amountOfQuestionsAnswered == assessmentAnswers.size()
                || amountOfQuestionsAnswered > assessmentAnswers.size()) {
            return 100;
        } else {
            return (int) (100.0 / assessmentAnswers.size() * amountOfQuestionsAnswered);
        }
    }

    /**
     * returns the assessmentAnswer that belongs to a question
     *
     * @param question the question to get the assessmentAnswer from
     * @return the assessmentAnswer
     */
    private AssessmentAnswer getAssessmentAnswer(Question question) {
        for (AssessmentAnswer aa : assessmentAnswers) {
            if (aa.getQuestion().equals(question)) {
                return aa;
            }
        }
        return null;
    }

    /**
     * checks if the evaluation isFinished() yet.
     *
     * @return
     */
    public boolean isFinished() {
        int amountAnswered = 0;

        for (AssessmentAnswer aa : assessmentAnswers) {
            if (aa.isAnswered()) {
                amountAnswered++;
            }
        }

        return (amountAnswered == assessmentAnswers.size());
    }
    
    Object onNavigateToOverview() {
        evaluationOverview.setParameters(assessment, question);
        return evaluationOverview;
    }

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
}
