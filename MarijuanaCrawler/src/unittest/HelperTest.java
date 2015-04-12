package unittest;

import static org.junit.Assert.*;

import org.junit.Test;

import commonlib.Helper;

public class HelperTest {

    @Test
    public void test() {
        TestCleanNonCharacterDigit();
        TestGetTime();
    }
    
    public void TestCleanNonCharacterDigit() {
        String test1 = "Light Eastern BMX Bike - $250 (Fontana)";
        String cleanUpTest1 = Helper.cleanNonCharacterDigit(test1);
        assertEquals(cleanUpTest1, "Light Eastern BMX Bike  $250 Fontana");
    }
    
    public void TestGetTime() {
    	String currentDate = Helper.getCurrentDate();
    	assertEquals(currentDate.length(), 10);
    	
    	String currentTime = Helper.getCurrentTime();
    	assertEquals(currentTime.length(), 8);
    }
}
