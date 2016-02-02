package eu.iunxi.apps.assessment.model;


import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author Willems
 */
public class BaseTest {

    protected static SessionFactory sessionFactory;
    protected static Session session;

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.err.println("------- BaseTest.setUpClass -------");
        /*
         POM:
         <dependency>
         <groupId>org.hsqldb</groupId>
         <artifactId>hsqldb</artifactId>
         <version>2.3.1</version>
         </dependency>
         */
        Configuration configuration = new Configuration()
                .setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
                .setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver")
                .setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:assessment")
                .setProperty("hibernate.connection.username", "sa")
                .setProperty("hibernate.connection.password", "")
                .setProperty("hibernate.connection.pool_size", "1")
                .setProperty("hibernate.cache.provider_class", "org.hibernate.cache.HashtableCacheProvider")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.max_fetch_depth", "0")
                .setProperty("hibernate.show_sql", "true");
        
        configuration.addAnnotatedClass(Assessment.class);
        configuration.addAnnotatedClass(AssessmentAnswer.class);
        configuration.addAnnotatedClass(AssessmentAnswerClosed.class);
        configuration.addAnnotatedClass(AssessmentAnswerClosedMultiple.class);
        configuration.addAnnotatedClass(AssessmentAnswerClosedSingle.class);
        configuration.addAnnotatedClass(AssessmentAnswerOpen.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(Question.class);
        configuration.addAnnotatedClass(QuestionClosed.class);
        configuration.addAnnotatedClass(QuestionClosedMultiple.class);
        configuration.addAnnotatedClass(QuestionClosedOption.class);
        configuration.addAnnotatedClass(QuestionClosedSingle.class);
        configuration.addAnnotatedClass(QuestionImage.class);
        configuration.addAnnotatedClass(QuestionOpen.class);
        configuration.addAnnotatedClass(User.class);

        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        
        try {
            session = sessionFactory.openSession();
        } catch (ConstraintViolationException v) {
            for (ConstraintViolation constraintViolation : v.getConstraintViolations()) {
                System.err.println(String.format("Invalid value '%s' for %s:%s", constraintViolation.getInvalidValue(), constraintViolation.getPropertyPath(), constraintViolation.getMessage()));
            }
            throw v;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        
        // Fresh session
        session.close();
        session = sessionFactory.openSession();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.err.println("------- BaseTest.tearDownClass -------");
        try {
            session.close();
            sessionFactory.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
