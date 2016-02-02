package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Category;
import eu.iunxi.apps.assessment.model.Question;
import eu.iunxi.apps.assessment.model.QuestionClosed;
import eu.iunxi.apps.assessment.model.QuestionClosedMultiple;
import eu.iunxi.apps.assessment.model.QuestionClosedOption;
import eu.iunxi.apps.assessment.model.QuestionClosedSingle;
import eu.iunxi.apps.assessment.model.QuestionOpen;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.CategoryComparator;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.MyOptionModel;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
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
public class QuestionNew {

    public enum QuestionType {
        OPEN, CLOSEDSINGLE, CLOSEDMULTIPLE
    }

    @Property
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Property
    private Question question;

    @Inject
    private Session session;

    @Property
    private Category category;

    @Property
    private int pointsWorth;

    @Property
    private String cheatSheet;

    @Inject
    private AlertManager alertManager;

    @Property
    @ActivationRequestParameter
    private boolean isNewQuestion;

    @InjectPage
    private CategoryDetails categoryDetails;

    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    private boolean stayOnThisPage; //keeps track of whether to stay on this page after submitting or not

    private boolean inFormSubmission = false;

    Object[] onPassivate() {
        return new Object[]{questionType, question, category};
    }

    Object onActivate(QuestionType type, Question question, Category category) {
        User user = loggedInUser.getUser(session);
        if(user == null || !user.hasPermissionTo(Permission.QUESTION_EDIT)){
            if (isNewQuestion){
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen aan te maken.");
            } else {
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen te bewerken.");
            }
            return CategoryOverview.class;
        }
        
        this.question = question;
        this.questionType = type;
        this.category = category;

        if (this.question == null && type != null) {
            switch (type) {
                case OPEN:
                    this.question = new QuestionOpen();
                    isNewQuestion = true;
                    break;
                case CLOSEDSINGLE:
                    this.question = new QuestionClosedSingle();
                    isNewQuestion = true;
                    break;
                case CLOSEDMULTIPLE:
                    this.question = new QuestionClosedMultiple();
                    isNewQuestion = true;
                    break;
                default:
                    break;
            }
        }

        if (this.question instanceof QuestionOpen && this.question != null) {
            this.pointsWorth = ((QuestionOpen) this.question).getPointsWorth();
            this.cheatSheet = ((QuestionOpen) this.question).getAnswerCheatSheet();
        }
        
        if(this.question instanceof QuestionClosed && this.finalOptions == null){
            this.finalOptions = new LinkedList<QuestionClosedOption>();
        }
        
        return null;
    }

    public void onPrepareForSubmitFromNewQuestionForm() {
        this.inFormSubmission = true;
    }
    
    public void onValidateFromNewQuestionForm() throws ValidationException{
        if(this.question instanceof QuestionClosed){
            if(finalOptions == null && finalOptions.isEmpty() || finalOptions.size() < 2){
                alertManager.alert(Duration.SINGLE, Severity.ERROR, "Er moeten minimaal 2 opties toegevoegd worden.");
                throw new ValidationException("");
            }
        }
    }
    
    @CommitAfter
    Object onSuccessFromNewQuestionForm() {
        User user = loggedInUser.getUser(session);
        if (user != null && user.hasPermissionTo(Permission.QUESTION_EDIT)) {
            if (question instanceof QuestionClosed) {

                for (QuestionClosedOption existingOption : ((QuestionClosed) this.question).getOptions()) {
                    if (!this.finalOptions.contains(existingOption)) {
                        existingOption.setDeleted(true);
                    }
                }

                for (QuestionClosedOption qco : ((QuestionClosed) this.question).getOptions()) {
                    session.persist(qco);
                }

            }

            if (question instanceof QuestionOpen) {
                ((QuestionOpen) question).setPointsWorth(pointsWorth);
                ((QuestionOpen) question).setAnswerCheatSheet(cheatSheet);
            }

            //set separatly because the category parameter may have been preset from another page
            question.setCategory(category);

            session.persist(this.question);

            if (isNewQuestion) {
                alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De vraag met de titel \"" + question.getTitle() + "\" is succesvol aangemaakt.");
            } else {
                alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "De vraag met de titel \"" + question.getTitle() + "\" is succesvol bewerkt.");
            }
            
        } else if(isNewQuestion){
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen aan te maken.");
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om vragen aan te bewerken.");
        }
        
        if (stayOnThisPage) return this;
        
        categoryDetails.setThisCategory(category);

        return categoryDetails;
    }

    public boolean isClosed() {
        return question instanceof QuestionClosed;
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

    public ValueEncoder<QuestionClosedOption> getOptionEncoder() {

        final QuestionClosed question = (QuestionClosed) this.question;

        return new ValueEncoder<QuestionClosedOption>() {
            @Override
            public String toClient(QuestionClosedOption v) {
                return v == null || v.getId() == 0 ? null : String.valueOf(v.getId());
            }

            @Override
            public QuestionClosedOption toValue(String string) {
                return string == null ? new QuestionClosedOption(question) : (QuestionClosedOption) session.get(QuestionClosedOption.class, Integer.valueOf(string));
            }
        };
    }

    public String getTitle() {
        return (this.question.getId() == 0) ? "Maak een nieuwe vraag aan" : "Pas een vraag aan";
    }

    private List<QuestionClosedOption> finalOptions;
    private QuestionClosedOption option;

    public List<QuestionClosedOption> getOptions() {
        List<QuestionClosedOption> list = new LinkedList<QuestionClosedOption>();

        if (this.question != null && ((QuestionClosed) this.question).getOptions() != null && !(((QuestionClosed) this.question).getOptions()).isEmpty()) {
            for (QuestionClosedOption qco : ((QuestionClosed) this.question).getOptions()) {
                list.add(qco);
            }
        }

        return list;
    }

    public void setOption(QuestionClosedOption qco) {
        this.option = qco;
        this.option.setQuestion((QuestionClosed) this.question);

        if (this.inFormSubmission) {
            if (this.finalOptions == null) {
                this.finalOptions = new LinkedList<QuestionClosedOption>();
            }
            this.finalOptions.add(qco);
        }
    }

    public QuestionClosedOption getOption() {
        return this.option;
    }

    public QuestionClosedOption onAddRow() {
        QuestionClosedOption option = new QuestionClosedOption();

        option.setQuestion(((QuestionClosed) this.question));
        
        return option;
    }

    void onRemoveRow(QuestionClosedOption qco) {
        qco.setDeleted(true);
    }
    
    void onSelectedFromStaySubmit(){
        stayOnThisPage = true;
    }
}
