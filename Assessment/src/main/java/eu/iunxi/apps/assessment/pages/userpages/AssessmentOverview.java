package eu.iunxi.apps.assessment.pages.userpages;

import eu.iunxi.apps.assessment.model.Assessment;
import eu.iunxi.apps.assessment.model.AssessmentAnswer;
import eu.iunxi.apps.assessment.model.User;
import eu.iunxi.apps.assessment.util.LoggedInUser;
import eu.iunxi.apps.assessment.util.Permission;
import java.util.LinkedList;
import java.util.List;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author joey
 */
public class AssessmentOverview {

    @Inject
    private Session session;

    @Property
    private Assessment loopedAssessment;

    @Property
    @ActivationRequestParameter
    private String searchString;
    
    @InjectPage
    private AssessmentAddQuestionsBulk assessmentAddQuestionsBulk;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    @SessionAttribute
    private LoggedInUser loggedInUser;
    
    @Property
    private final Permission assessmentEditPermission = Permission.ASSESSMENT_EDIT; //used in template
    
    @Property
    private Integer page;
    
    private final Integer maxResultSetSize = 20;
    
    @Property
    private List<Assessment> resultSet;
    
    @Property
    private int totalAmountOfAssessments;    
    
    
    Object[] onPassivate(){
        return new Object[] { page };
    }    
    
    void onActivate(Integer page){
        this.page = page;
        
        if(page == null){
            this.page = 1;
        }
        
    }

    @SetupRender
    public void setupRender() {
        if(page == null) page = 1;
        resultSet = new LinkedList<Assessment>();
        resultSet.addAll(getQuery().list());
    }

    public Criteria getQuery() {
        Criteria query = this.session.createCriteria(Assessment.class);
        query.add(Restrictions.eq("deleted", false));
        
        totalAmountOfAssessments = query.list().size();
        
        query.addOrder(Order.desc("creationDate"));
        
        if (searchString != null) {
            query.add(Restrictions.or(Restrictions.ilike("personName", searchString, MatchMode.ANYWHERE),
                      Restrictions.ilike("personEmail", searchString, MatchMode.ANYWHERE)));
        }
        
        query.setMaxResults(maxResultSetSize).setFirstResult(page * maxResultSetSize - maxResultSetSize);
        
        return query;
    }



    @CommitAfter
    Object onSuccessFromNewAssessmentForm() {
        User user = loggedInUser.getUser(session);
        if(user != null && user.hasPermissionTo(Permission.ASSESSMENT_EDIT)){
            Assessment assessment = new Assessment();

            session.persist(assessment);
        
            assessmentAddQuestionsBulk.setThisAssessment(assessment);
        
            alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Met succes een nieuw assessment gegenereerd.");
            
            return assessmentAddQuestionsBulk;
        } else {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Je hebt geen toestemming om assessments aan te maken.");
        }
        return this;
    }

    public String getStatus() {
        if(loopedAssessment.isClosed()){
            return "Gesloten";
        } else if(amountOfQuestionsAnswered() > 0 && loopedAssessment.getPersonEmail() != null){
            return "Bezig";
        } 
        return "Nog niet gemaakt";
    }

    public int amountOfQuestionsAnswered() {
        int amount = 0;
        for (AssessmentAnswer aa : loopedAssessment.getAssessmentAnswers()) {
            if (aa.isAnswered()) {
                amount++;
            }
        }
        return amount;
    }
    
    public User getUser(){ // used in template
        return loggedInUser.getUser(session);
    }   
    
    public int getNextPage(){
        return page + 1;
    }
    
    public int getPreviousPage(){
        return page - 1;
    }
    
    public boolean isAllowedNext(){
        return page != getAmountOfPages();
    }
    
    public boolean isAllowedPrevious(){
        return page > 1;
    }
    
    public int getAmountOfPages(){
        int pages = 0;
        
        
        if(maxResultSetSize != null){
            if(totalAmountOfAssessments % maxResultSetSize == 0){
                pages = (int)totalAmountOfAssessments / maxResultSetSize;
            } else {
                pages = (totalAmountOfAssessments <= maxResultSetSize) ? 1 : (int) totalAmountOfAssessments / maxResultSetSize + 1;
            }
        }
        
        return pages;
    }  
}
