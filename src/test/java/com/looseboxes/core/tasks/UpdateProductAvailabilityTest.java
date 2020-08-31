package com.looseboxes.core.tasks;

import com.looseboxes.core.LbApp;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class UpdateProductAvailabilityTest {
    
    public UpdateProductAvailabilityTest() {}
    @BeforeClass
    public static void setUpClass() { }
    @AfterClass
    public static void tearDownClass() { }
    @Before
    public void setUp() throws Exception { 
        LbApp.getInstance().init("file:/C:/Users/Josh/Documents/NetBeansProjects/looseboxespu/src/test/resources/META-INF/persistence.xml");
    }
    @After
    public void tearDown() { }

    /**
     * Test of run method, of class UpdateProductAvailability.
     */
    @Test
    public void testRun() {
        UpdateProductAvailability instance = new UpdateProductAvailability(
                LbApp.getInstance().getJpaContext()
        );
        instance.run();
    }
}
