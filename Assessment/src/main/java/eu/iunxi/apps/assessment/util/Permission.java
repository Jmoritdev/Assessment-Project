package eu.iunxi.apps.assessment.util;

/**
 *
 * @author joey
 */
public enum Permission {

    /**
    * the permission to edit and make new questions
    */    
    QUESTION_EDIT{
        @Override
        public String getDescription(){
            return "Vragen toevoegen en aanpassen";
        }
    },
    
    /**
     * the permission to remove questions
     */
    QUESTION_REMOVE{
        @Override
        public String getDescription(){
            return "Vragen verwijderen";
        }
    },
    
    /**
     * the permission to edit categories
     */
    CATEGORY_EDIT{
        @Override
        public String getDescription(){
            return "Categorieën toevoegen en aanpassen";
        }
    },
    
    /**
     * the permission to remove categories
     */
    CATEGORY_REMOVE{
        @Override
        public String getDescription(){
            return "Categorieën verwijderen";
        }
    },
    
    /**
     * the permission to edit and make new users
     */
    USER_EDIT{
        @Override
        public String getDescription(){
            return "Gebruikers toevoegen en aanpassen";
        }
    },
    
    /**
     * the permission to remove users
     */
    USER_REMOVE{
        @Override
        public String getDescription(){
            return "Gebruikers verwijderen";
        }
    },
    
    /**
     * the permission to edit and make new assessments
     */
    ASSESSMENT_EDIT{
        @Override
        public String getDescription(){
            return "Assessments toevoegen en aanpassen";
        }
    },
    
    /**
     * the permission to remove assessments
     */
    ASSESSMENT_REMOVE{
        @Override
        public String getDescription(){
            return "Assessments verwijderen";
        }
    },
    
    
    /**
     * the permission to login
     */
    CAN_LOGIN{
        @Override
        public String getDescription(){
            return "Kan inloggen";
        }
    },
    
    
    /**
     * the permission to check the answers of candidates
     */
    ASSESSMENT_CHECK_ANSWERS{
        @Override
        public String getDescription(){
            return "Antwoorden nakijken";
        }
    }; 
      
    /**
     * @return the description
     */
    public abstract String getDescription();
    
}
