package com.looseboxes.core.tasks;

import com.looseboxes.core.LbApp;
import com.looseboxes.pu.References;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Josh
 */
public class UpdateProductPricesTest {
    
    public UpdateProductPricesTest() { }
    
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
     * Test of run method, of class UpdateProductPrices.
     */
//    @Test
    public void testRun() {
        final BigDecimal factor = BigDecimal.valueOf(485).divide(BigDecimal.valueOf(420), new MathContext(3, RoundingMode.HALF_UP));
        final UpdateProductPrices instance = new UpdateProductPrices(
                LbApp.getInstance().getJpaContext(), factor.floatValue()
        ){
            @Override
            public void begin(EntityManager em) { }
            @Override
            public void persist(EntityManager em, Object entity) { }
            @Override
            public void commit(EntityManager em) { }
        };
        instance.update(References.availability.LimitedAvailability);
        instance.update(References.availability.InStock);
    }
}
