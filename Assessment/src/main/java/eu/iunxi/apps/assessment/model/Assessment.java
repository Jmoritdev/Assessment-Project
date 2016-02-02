package eu.iunxi.apps.assessment.model;

import eu.iunxi.apps.assessment.util.QuestionComparator;
import java.io.Serializable;
import java.util.Collections;
import static java.util.Collections.shuffle;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.hibernate.Session;
import org.hibernate.annotations.Type;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import static java.util.Collections.shuffle;

/**
 *
 * @author joey
 */
@Entity
public class Assessment implements Serializable {
    
    //Unique identifier
    private final String uid;
    
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @NonVisual
    private int id;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean closed;
    
    private String personName;
    
    private String personEmail;
    
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private final DateTime creationDate;
    
    @Column(nullable = false, columnDefinition = "BIT", length = 1)
    private boolean deleted;
    
    @OneToMany(mappedBy="assessment",  cascade = CascadeType.ALL)
    private List<AssessmentAnswer> assessmentAnswers;
    
    private long duration;
    
    public Assessment() {
        this.uid = UUID.randomUUID().toString();
        assessmentAnswers = new LinkedList<AssessmentAnswer>();
        creationDate = new DateTime().plusHours(1);
    }
    
    public static Assessment getByUid(String uid, Session session) {
        return (Assessment) session.createCriteria(Assessment.class).add(Restrictions.eq("uid", uid)).setMaxResults(1).uniqueResult();
    }
    
    /**
     * Adds random questions to this Assessment
     * @param category From which category to add
     * @param difficulty The level of difficulty
     * @param amountOfOpen The amount of open questions to add (regarding category and difficulty)
     * @param amountOfClosed The amount of closed questions to add (regarding category and difficulty)
     * @return 
     */
    public Assessment addRandomQuestions(Category category, Question.Difficulty difficulty, int amountOfOpen, int amountOfClosed) {   
        //TODO make this method shorter and more efficient
        List<Question> filteredOpenQuestions = new LinkedList<Question>();
        List<Question> filteredClosedQuestions = new LinkedList<Question>();
        
        //the blacklist contains questions that are already in this assessment
        //so they dont get added again
        List<Question> blacklist = new LinkedList<Question>();
        
        for(AssessmentAnswer aa : assessmentAnswers){
            blacklist.add(aa.getQuestion());
        }
        
        //add some questions that qualify and are not in the blacklist to the lists above
        for(Question q : category.getQuestions(difficulty)){
            if(q instanceof QuestionOpen && filteredClosedQuestions.size() < amountOfOpen && !blacklist.contains(q)){
                filteredOpenQuestions.add(q);
            }
            if(q instanceof QuestionClosed && filteredClosedQuestions.size() < amountOfClosed && !blacklist.contains(q)){
                filteredClosedQuestions.add(q);
            }
        }
        
        //add random questions from the above lists to this assessment
        if(!filteredOpenQuestions.isEmpty()){
            //i's task is to make sure the loop always loops as much as the original size of the list, even if an object gets removed.
            int i = 0;
            for(int x = 0; x < filteredOpenQuestions.size() + i ; x++) {
                int randomNumber = new Random().nextInt(filteredOpenQuestions.size());
                Question question = filteredOpenQuestions.get(randomNumber);
                assessmentAnswers.add(question.createAnswerForAssessment(this));
                //remove it so it can't be picked again
                filteredOpenQuestions.remove(randomNumber);
                i++;
            }
        }
        if(!filteredClosedQuestions.isEmpty()){
            //i's task is to make sure the loop always loops as much as the original size of the list, even if an object gets removed.
            int i = 0;
            for(int x = 0; x < filteredClosedQuestions.size() + i; x++) {
                int randomNumber = new Random().nextInt(filteredClosedQuestions.size());
                Question question = filteredClosedQuestions.get(randomNumber);
                assessmentAnswers.add(question.createAnswerForAssessment(this));
                //remove it so it can't be picked again
                filteredClosedQuestions.remove(randomNumber);
                i++;
            }
        }
        
        shuffle(assessmentAnswers);
        
        return this;
    }
    
    /**
     * returns all the questions that the subject answered correctly
     * @return 
     */
    public List<QuestionClosed> getCorrectQuestions(){
        List<QuestionClosed> correctQuestions = new LinkedList<QuestionClosed>();
        
        for(AssessmentAnswer aa : assessmentAnswers) {
            if (aa != null && aa instanceof AssessmentAnswerClosedMultiple) {
                for(QuestionClosedOption qco : ((AssessmentAnswerClosedMultiple)aa).getSelectedOptions()){
                    if(qco != null && qco.isCorrect()){
                        correctQuestions.add(qco.getQuestion());
                    }
                }
            } 
            
            if (aa != null && aa instanceof AssessmentAnswerClosedSingle) {
                QuestionClosedOption qco = ((AssessmentAnswerClosedSingle)aa).getSelectedOption();
                if(qco != null && qco.isCorrect()){
                    correctQuestions.add(qco.getQuestion());
                }
            } 
        }
        
        return correctQuestions;
    }
    
    /**
     * returns all the questions that the subject answered incorrectly
     * @return 
     */
    public List<QuestionClosed> getIncorrectQuestions(){
        List<QuestionClosed> incorrectQuestions = new LinkedList<QuestionClosed>();
        
        for(AssessmentAnswer aa : assessmentAnswers) {
            if (aa instanceof AssessmentAnswerClosedMultiple) {
                for(QuestionClosedOption qco : ((AssessmentAnswerClosedMultiple)aa).getSelectedOptions()){
                    if(qco != null && !qco.isCorrect()){
                        incorrectQuestions.add(qco.getQuestion());
                    }
                }
            } 
            
            if (aa instanceof AssessmentAnswerClosedSingle) {
                QuestionClosedOption qco = ((AssessmentAnswerClosedSingle)aa).getSelectedOption();
                if(qco != null && !qco.isCorrect()){
                    incorrectQuestions.add(qco.getQuestion());
                }
            } 
        }
        
        return incorrectQuestions;
    }
    
    /**
     * returns a list with open questions that have yet to be checked
     * @return 
     */
    public List<QuestionOpen> getUncheckedQuestions(){
        List<QuestionOpen> uncheckedQuestions = new LinkedList<QuestionOpen>();
        
        for(AssessmentAnswer aa : assessmentAnswers){
            if(aa instanceof AssessmentAnswerOpen){
                //check if the assessmentAnswer has an ExaminerAnswer string attached to it, or it doesnt have any points assigned
                if(((AssessmentAnswerOpen)aa).getExaminerAnswer() == null || ((AssessmentAnswerOpen)aa).getAssignedPoints() == null){
                    uncheckedQuestions.add( (QuestionOpen) ((AssessmentAnswerOpen)aa) .getQuestion() );
                }
            }
        }
        
        return uncheckedQuestions;
    }
    
    /**
     * returns the achieved points by the subject
     * @return 
     */
    public int getAchievedPoints(){
        int points = 0;
        
        for(AssessmentAnswer aa : assessmentAnswers){
            
            if(aa instanceof AssessmentAnswerClosedMultiple && ((AssessmentAnswerClosedMultiple)aa).getSelectedOptions() != null){ 
                for(QuestionClosedOption qco : ((AssessmentAnswerClosedMultiple)aa).getSelectedOptions()){
                    points += qco.getPointsWorth();
                }
            }
            
            if(aa instanceof AssessmentAnswerClosedSingle && ((AssessmentAnswerClosedSingle)aa).getSelectedOption() != null){
                points += ((AssessmentAnswerClosedSingle)aa).getSelectedOption().getPointsWorth();
            }
            
            if(aa instanceof AssessmentAnswerOpen && ((AssessmentAnswerOpen) aa).getAssignedPoints() != null){
                points += ((AssessmentAnswerOpen) aa).getAssignedPoints();
            }
        }
        
        return points;
    }
    
    /**
     * Returns the maximum amount of points to score for all questions
     * @return 
     */
    public int getMaxScore() {
        int maxScore = 0;
        for(AssessmentAnswer s: assessmentAnswers){
            maxScore += s.getQuestion().getMaxScore();
        }
        return maxScore;
    }
    
    public List<Question> getQuestions(){
        List<Question> questions = new LinkedList<Question>();
        for(AssessmentAnswer aa : assessmentAnswers){
            questions.add(aa.getQuestion());
        }
        
        Collections.sort(questions, new QuestionComparator(QuestionComparator.SortBy.DIFFICULTY));
        Collections.sort(questions, new QuestionComparator(QuestionComparator.SortBy.CATEGORY));
        
        return questions;
    }
    
    /**
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * @return the personName
     */
    public String getPersonName() {
        return personName;
    }

    /**
     * @param personName the personName to set
     */
    public void setPersonName(String personName) {
        this.personName = personName;
    }

    /**
     * @return the assessmentAnswers
     */
    public List<AssessmentAnswer> getAssessmentAnswers() {
        return assessmentAnswers;
    }

    /**
     * @param assessmentAnswers the assessmentAnswers to set
     */
    public void setAssessmentAnswers(List<AssessmentAnswer> assessmentAnswers) {
        this.assessmentAnswers = assessmentAnswers;
    }

    /**
     * @return the personEmail
     */
    public String getPersonEmail() {
        return personEmail;
    }

    /**
     * @param personEmail the personEmail to set
     */
    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return the closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed the closed to set
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the creationDate
     */
    public DateTime getCreationDate() {
        return creationDate;
    }
    
    /**
     * creates a timestamp for the date on which this assessment was made
     * @return 
     */
    public String getTimeStamp(){
        
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendDayOfMonth(2).appendLiteral("-")
                .appendMonthOfYear(2).appendLiteral("-")
                .appendYear(4, 4).appendLiteral(" ")
                .appendHourOfDay(2).appendLiteral(":")
                .appendMinuteOfHour(2).appendLiteral(":")
                .appendSecondOfMinute(2)
                .toFormatter();

        return formatter.print(creationDate.minusHours(1));
    }

    /**
     * @return the amount of time it took the participant to complete the assessment in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * set the amount of time it took the participant to complete the assessment in milliseconds
     * @param assessmentDuration the duration to set
     */
    public void setDuration(long assessmentDuration) {
        this.duration = assessmentDuration;
    }
}