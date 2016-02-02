package eu.iunxi.apps.assessment.model;

import java.util.LinkedList;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author joey
 */
public class AssessmentTest extends BaseTest {
    private Category category;
    
    private Assessment assessment;
    
    
    public AssessmentTest() {
        System.out.println("Constructed");
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseTest.setUpClass();
    }
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseTest.tearDownClass();
    }
    
    
    public void installAssessment(){
        assessment = new Assessment();
        assessment.setPersonName("Henk");
        assessment.setPersonEmail("henk@hotmail.nl");
        
        assessment
            .addRandomQuestions(category, Question.Difficulty.JUNIOR, 1, 1)
            .addRandomQuestions(category, Question.Difficulty.MEDIOR, 0, 3)
            .addRandomQuestions(category, Question.Difficulty.SENIOR, 1, 0);
    
    }
    
    public void installCategory() {
        
        category = new Category();
        category.setTitle("Sport");
        
        QuestionOpen question1 = new QuestionOpen("Voetbal", category, 
                "Hoeveel punten zijn er maximaal te halen in een voetbalwedstrijd?", Question.Difficulty.JUNIOR, 1, "");
        
        QuestionOpen question2 = new QuestionOpen("Badminton", category,
                "Wat is het maximaal aantal punten wat er bij badminton in 1 match gehaald kan worden?", Question.Difficulty.SENIOR, 3, "");

        QuestionClosedMultiple question3 = (QuestionClosedMultiple) new QuestionClosedMultiple("Triatlon", category, 
                "Welke sporten worden er gecombineerd bij een triatlon?", Question.Difficulty.MEDIOR)
                .addOption(new QuestionClosedOption("Zwemmen", true, 1))
                .addOption(new QuestionClosedOption("Skiën", false, 0))
                .addOption(new QuestionClosedOption("Hardlopen", true, 1))
                .addOption(new QuestionClosedOption("Wielrennen", true, 1))
                .addOption(new QuestionClosedOption("Schietsport", false, 0));
        
        QuestionClosedSingle question4 = (QuestionClosedSingle) new QuestionClosedSingle("Atletiek", category,
                "Hoeveel meter is een atletiekbaan?", Question.Difficulty.MEDIOR)
                .addOption(new QuestionClosedOption("300 meter", false, 0))
                .addOption(new QuestionClosedOption("400 meter", true, 1))
                .addOption(new QuestionClosedOption("500 meter", false, 0));
        
        QuestionClosedSingle question5 = (QuestionClosedSingle) new QuestionClosedSingle("Golf", category, 
                "Hoe wordt de afslagplaats bij golf genoemd?", Question.Difficulty.MEDIOR)
                .addOption(new QuestionClosedOption("Green", false, 0))
                .addOption(new QuestionClosedOption("Tee", true, 1))
                .addOption(new QuestionClosedOption("Fairway", false, 0));
        
        QuestionClosedSingle question6 = (QuestionClosedSingle) new QuestionClosedSingle("Bowlen", category,
                "Hoeveel kegels moet je bij bowlen omgooien om een strike te scoren?", Question.Difficulty.JUNIOR)
                .addOption(new QuestionClosedOption("8", false, 0))
                .addOption(new QuestionClosedOption("10", true, 1))
                .addOption(new QuestionClosedOption("12", false, 0));
        
        category
                .addQuestion(question1)
                .addQuestion(question2)
                .addQuestion(question3)
                .addQuestion(question4)
                .addQuestion(question5)
                .addQuestion(question6);
        
        session.persist(category);
        session.flush();
    }
    
    //sets a number of selected answers to simulate user input
    public void installSelectedAnswers(){
            
        for(AssessmentAnswer aa : assessment.getAssessmentAnswers()){
            
            switch(aa.getQuestion().getTitle()){
                case "Triatlon":
                    List<QuestionClosedOption> options3 =  category.getOptionsFromQuestion((QuestionClosed)aa.getQuestion());
                    List<QuestionClosedOption> selectedOptions = new LinkedList<QuestionClosedOption>();
                    selectedOptions.add(options3.get(0));
                    selectedOptions.add(options3.get(1));
                    selectedOptions.add(options3.get(2));
                    ((AssessmentAnswerClosedMultiple)aa).setSelectedOptions(selectedOptions);
                    break;
                case "Atletiek":
                    List<QuestionClosedOption> options4 =  category.getOptionsFromQuestion((QuestionClosed)aa.getQuestion());
                    QuestionClosedOption selectedOption4 = options4.get(1);
                    ((AssessmentAnswerClosedSingle)aa).setSelectedOption(selectedOption4);
                    break;
                case "Golf":
                    List<QuestionClosedOption> options5 =  category.getOptionsFromQuestion((QuestionClosed)aa.getQuestion());
                    QuestionClosedOption selectedOption5 = options5.get(0);
                    ((AssessmentAnswerClosedSingle)aa).setSelectedOption(selectedOption5);
                    break;
                case "Bowlen":
                    List<QuestionClosedOption> options6 =  category.getOptionsFromQuestion((QuestionClosed)aa.getQuestion());
                    QuestionClosedOption selectedOption6 = options6.get(2);
                    ((AssessmentAnswerClosedSingle)aa).setSelectedOption(selectedOption6);
                    break;
            }        
        }
    }
    
    public void installOpenQuestions(){
        //set the answer of the testsubject
        for(AssessmentAnswer aa : assessment.getAssessmentAnswers()){
            if(aa.getQuestion() instanceof QuestionOpen && aa.getQuestion().getTitle().equals("Voetbal")){
                //changed, might cause issues in test
                ((AssessmentAnswerOpen)aa).setUserAnswer("oneindig");
            }
            if(aa.getQuestion() instanceof QuestionOpen && aa.getQuestion().getTitle().equals("Badminton")){
                //changed, might cause issues in test
                ((AssessmentAnswerOpen)aa).setUserAnswer("150");
            }
        }
        //set the response from the examiner
        for(AssessmentAnswer aa : assessment.getAssessmentAnswers()){
            if(aa.getQuestion() instanceof QuestionOpen && aa.getQuestion().getTitle().equals("Voetbal")){
                ((AssessmentAnswerOpen)aa).setExaminerAnswer("Das helemaal goed :D");
                ((AssessmentAnswerOpen)aa).setAssignedPoints(1);
            }
            if(aa.getQuestion() instanceof QuestionOpen && aa.getQuestion().getTitle().equals("Badminton")){
                ((AssessmentAnswerOpen)aa).setExaminerAnswer("das fout :(");
                ((AssessmentAnswerOpen)aa).setAssignedPoints(0);
            }
        }
    }
    
    @Test
    public void testAssessment(){
        
        installCategory();
        
        assertEquals("Total Junior in category", 2, category.getQuestions(Question.Difficulty.JUNIOR).size());
        assertEquals("Total Medior in category", 3, category.getQuestions(Question.Difficulty.MEDIOR).size());
        assertEquals("Total Senior in category", 1, category.getQuestions(Question.Difficulty.SENIOR).size());
        assertEquals("Total questions in category", 6, category.getQuestions().size());
        
        installAssessment();
        
        assertEquals("Total questions in assessment", 6, assessment.getAssessmentAnswers().size());
        assertEquals("Maximum Score", 10, assessment.getMaxScore());
        
        installSelectedAnswers();
        
        assertEquals("Correct questions", 3, assessment.getCorrectQuestions().size());
        assertEquals("Incorrect questions", 3, assessment.getIncorrectQuestions().size());
        assertEquals("Unchecked questions", 2, assessment.getUncheckedQuestions().size());
        assertEquals("Achieved points", 3, assessment.getAchievedPoints());
        
        installOpenQuestions();
        
        assertEquals("Correct questions", 3, assessment.getCorrectQuestions().size());
        assertEquals("Incorrect questions", 3, assessment.getIncorrectQuestions().size());
        assertEquals("Unchecked questions", 0, assessment.getUncheckedQuestions().size());
        assertEquals("Achieved points", 4, assessment.getAchievedPoints());
        
    }
    
}
