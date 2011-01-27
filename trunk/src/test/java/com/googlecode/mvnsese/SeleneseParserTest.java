
package com.googlecode.mvnsese;

import com.googlecode.mvnsese.model.SeleneseTest;
import com.googlecode.mvnsese.model.SeleneseSuite;
import com.googlecode.mvnsese.model.SeleneseParser;
import com.googlecode.mvnsese.model.Command;
import com.googlecode.mvnsese.model.Selenese;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


public class SeleneseParserTest {

    public SeleneseParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

 
   @Test
   public void parseTest() throws Exception{
       File f = new File("target/test-classes/google.html");
       Selenese val = SeleneseParser.parse(f);
       assertNotNull(val);
       assertTrue(val instanceof SeleneseTest);
       SeleneseTest test = (SeleneseTest)val;
       assertEquals("Google", test.getTitle());
       assertEquals("http://www.google.com/",test.getBaseURL());
       assertEquals("target"+File.separator+"test-classes"+File.separator+"google.html", test.getFileName());
       assertNotNull(test.getCommands());
       assertEquals(5,test.getCommands().size());
       Command command = test.getCommands().get(0);
       assertEquals("open", command.getName());
       assertEquals("/", command.getTarget());
       assertEquals("", command.getValue());
       command = test.getCommands().get(1);
       assertEquals("typeKeys", command.getName());
       assertEquals("q", command.getTarget());
       assertEquals("selenium", command.getValue());

   }

    @Test
   public void parseSuite() throws Exception{
       File f = new File("target/test-classes/googleSuite.html");
       Selenese val = SeleneseParser.parse(f);
       assertNotNull(val);
       assertTrue(val instanceof SeleneseSuite);
       SeleneseSuite suite = (SeleneseSuite)val;
       assertEquals("Google Test Suite", suite.getTitle());
       assertEquals("target"+File.separator+"test-classes"+File.separator+"googleSuite.html", suite.getFileName());
       assertNotNull(suite.getTests());
       assertEquals(1,suite.getTests().size());
       SeleneseTest test = suite.getTests().get(0);

       assertEquals("Google Test", test.getTitle());
       assertEquals("http://www.google.com/",test.getBaseURL());
       assertEquals("google.html", test.getFileName());
       assertNotNull(test.getCommands());
       assertEquals(5,test.getCommands().size());
       Command command = test.getCommands().get(0);
       assertEquals("open", command.getName());
       assertEquals("/", command.getTarget());
       assertEquals("", command.getValue());
       command = test.getCommands().get(1);
       assertEquals("typeKeys", command.getName());
       assertEquals("q", command.getTarget());
       assertEquals("selenium", command.getValue());

   }

}