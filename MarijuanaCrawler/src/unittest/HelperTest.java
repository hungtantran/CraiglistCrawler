package unittest;

import static org.junit.Assert.*;

import org.junit.Test;

import commonlib.Helper;

public class HelperTest {

    @Test
    public void test() {
        TestCleanNonCharacterDigit();
        TestGetTime();
        TestCleanTag();
        TestCleanPostingBody();
    }
    
    public void TestCleanNonCharacterDigit() {
        String test1 = "Light Eastern BMX Bike - $250 (Fontana)";
        String cleanUpTest1 = Helper.cleanNonCharacterDigit(test1);
        assertEquals(cleanUpTest1, "Light Eastern BMX Bike  $250 Fontana");
    }
    
    public void TestGetTime() {
    	String currentDate = Helper.getCurrentDate();
    	System.out.println(currentDate);
    	assertEquals(currentDate.length(), 10);
    	
    	String currentTime = Helper.getCurrentTime();
    	System.out.println(currentTime);
    	assertEquals(currentTime.length(), 8);
    }
    
    public void TestCleanTag() {
    	String rawString = "One Of The Best OG's In Town!!! " +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> " +
    			"<br /> " +
    			"<br /> BY RESPONDING TO THIS AD YOU AGREE THAT: 1) i am a Arizona resident age 18 or older. 2) I have a written recommendation for the use of medical cannabis from my doctor. 3) I am not a law enforcement officer of any kind, or operating under an assumed name or in cooperation with any criminal investigation; nor am I seeking out evidence which may serve as the basis for any charge of violating federal, state, or local laws. 4) I will not use the information provided for any non-medicinal purposes. 5) Anyone who uses the provided information for any purposes what so ever, will be assuming their own liability, and are responsible for their own actions. 6) This medicine will be consumed only by me and/or other Prop 203 patients. *** This notice is intended for Arizona medical cannabis patients in accordance with Prop 203. This information is not intended for any other purpose illegal or otherwise. This is a legal advertisement for Medicinal marijuana in compliance with Arizona Prop 203 have extra high quality flower.. for MMJ CARDHOLDERS only. ." +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> " +
    			"<a href=\"/fb/phx/grd/4982804231\" class=\"showcontact\" title=\"click to show contact info\" rel=\"nofollow\">show contact info</a> " +
    			"<br /> " +
    			"<br /> best time after 3pm " +
    			"<br /> " +
    			"<br /> suggested donations" +
    			"<br /> " +
    			"<br /> 20 gs " +
    			"<br /> " +
    			"<br /> 55 8th" +
    			"<br /> " +
    			"<br /> 100 qtr" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />";
    	
    	String cleanedString = Helper.cleanTags("a", rawString);
    	
    	String expectedCleanedString = "One Of The Best OG's In Town!!! " +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> " +
    			"<br /> " +
    			"<br /> BY RESPONDING TO THIS AD YOU AGREE THAT: 1) i am a Arizona resident age 18 or older. 2) I have a written recommendation for the use of medical cannabis from my doctor. 3) I am not a law enforcement officer of any kind, or operating under an assumed name or in cooperation with any criminal investigation; nor am I seeking out evidence which may serve as the basis for any charge of violating federal, state, or local laws. 4) I will not use the information provided for any non-medicinal purposes. 5) Anyone who uses the provided information for any purposes what so ever, will be assuming their own liability, and are responsible for their own actions. 6) This medicine will be consumed only by me and/or other Prop 203 patients. *** This notice is intended for Arizona medical cannabis patients in accordance with Prop 203. This information is not intended for any other purpose illegal or otherwise. This is a legal advertisement for Medicinal marijuana in compliance with Arizona Prop 203 have extra high quality flower.. for MMJ CARDHOLDERS only. ." +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br /> " +
    			"<br /> " +
    			" " +
    			"<br /> " +
    			"<br /> best time after 3pm " +
    			"<br /> " +
    			"<br /> suggested donations" +
    			"<br /> " +
    			"<br /> 20 gs " +
    			"<br /> " +
    			"<br /> 55 8th" +
    			"<br /> " +
    			"<br /> 100 qtr" +
    			"<br /> " +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />";
    	
    	System.out.println(expectedCleanedString);
    	System.out.println(cleanedString);
    	assertEquals(expectedCleanedString, cleanedString);
    }
    
    public void TestCleanPostingBody() {
    	String rawString = "One Of The Best OG's In Town!!! \n" +
    			"<br /> \n" +
    			"<br /> Text OR email ONLY DO NOT CALL!\n" +
    			"<br /> \n" +
    			"<br /> \n" +
    			"<br /> \n" +
    			"<br /> BY RESPONDING TO THIS AD YOU AGREE THAT: 1) i am a Arizona resident age 18 or older. 2) I have a written recommendation for the use of medical cannabis from my doctor. 3) I am not a law enforcement officer of any kind, or operating under an assumed name or in cooperation with any criminal investigation; nor am I seeking out evidence which may serve as the basis for any charge of violating federal, state, or local laws. 4) I will not use the information provided for any non-medicinal purposes. 5) Anyone who uses the provided information for any purposes what so ever, will be assuming their own liability, and are responsible for their own actions. 6) This medicine will be consumed only by me and/or other Prop 203 patients. *** This notice is intended for Arizona medical cannabis patients in accordance with Prop 203. This information is not intended for any other purpose illegal or otherwise. This is a legal advertisement for Medicinal marijuana in compliance with Arizona Prop 203 have extra high quality flower.. for MMJ CARDHOLDERS only. .\n" +
    			"<br /> \n" +
    			"<br /> Text OR email ONLY DO NOT CALL!\n" +
    			"<br /> \n" +
    			"<br /> Text OR email ONLY DO NOT CALL!\n" +
    			"<br /> \n" +
    			"<br /> Text OR email ONLY DO NOT CALL!\n" +
    			"<br /> \n" +
    			"<br /> \n" +
    			"<a href=\"/fb/phx/grd/4982804231\" class=\"showcontact\" title=\"click to show contact info\" rel=\"nofollow\">show contact info</a> \n" +
    			"<br /> \n" +
    			"<br /> best time after 3pm \n" +
    			"<br /> \n" +
    			"<br /> suggested donations\n" +
    			"<br /> \n" +
    			"<br /> 20 gs \n" +
    			"<br /> \n" +
    			"<br /> 55 8th\n" +
    			"<br /> \n" +
    			"<br /> 100 qtr\n" +
    			"<br /> \n" +
    			"<br /> Text OR email ONLY DO NOT CALL!\n" +
    			"<br />\n";
    	
    	String cleanedString = Helper.cleanPostingBody(rawString);
    	
    	String expectedCleanedString = "One Of The Best OG's In Town!!!" +
    			"<br />" +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />" +
    			"<br />" +
    			"<br /> BY RESPONDING TO THIS AD YOU AGREE THAT: 1) i am a Arizona resident age 18 or older. 2) I have a written recommendation for the use of medical cannabis from my doctor. 3) I am not a law enforcement officer of any kind, or operating under an assumed name or in cooperation with any criminal investigation; nor am I seeking out evidence which may serve as the basis for any charge of violating federal, state, or local laws. 4) I will not use the information provided for any non-medicinal purposes. 5) Anyone who uses the provided information for any purposes what so ever, will be assuming their own liability, and are responsible for their own actions. 6) This medicine will be consumed only by me and/or other Prop 203 patients. *** This notice is intended for Arizona medical cannabis patients in accordance with Prop 203. This information is not intended for any other purpose illegal or otherwise. This is a legal advertisement for Medicinal marijuana in compliance with Arizona Prop 203 have extra high quality flower.. for MMJ CARDHOLDERS only. ." +
    			"<br />" +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />" +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />" +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />" +
    			"<br />" +
    			"<br /> best time after 3pm" +
    			"<br />" +
    			"<br /> suggested donations" +
    			"<br />" +
    			"<br /> 20 gs" +
    			"<br />" +
    			"<br /> 55 8th" +
    			"<br />" +
    			"<br /> 100 qtr" +
    			"<br />" +
    			"<br /> Text OR email ONLY DO NOT CALL!" +
    			"<br />";
    	
    	System.out.println(expectedCleanedString);
    	System.out.println(cleanedString);
    	assertEquals(expectedCleanedString, cleanedString);
    }
}
