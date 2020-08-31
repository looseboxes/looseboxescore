package com.looseboxes.core.search;

import com.looseboxes.pu.query.SelectProduct;
import com.bc.jpa.dao.util.DatabaseFormat;
import com.bc.jpa.controller.EntityController;
import com.bc.jpa.dao.search.SearchResults;
import java.util.logging.Logger;
import com.looseboxes.core.LbApp;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Availability;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Product_;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.search.BaseSearchResults;
import com.bc.jpa.dao.Select;
import java.util.logging.LogManager;

/**
 * @author Josh
 */
public class ProductSearchResultsTest {
    private transient static final Logger LOG = Logger.getLogger(ProductSearchResultsTest.class.getName());
    
    public ProductSearchResultsTest() { }
    
    @BeforeClass
    public static void setUpClass() throws Exception { 
        
        LbApp.getInstance().init("file:/C:/Users/Josh/Documents/NetBeansProjects/looseboxespu/src/test/resources/META-INF/persistence.xml");

        LogManager.getLogManager().readConfiguration(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/logging.properties"));
    }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testAll() throws Exception {
//        this.testTemp();
        this.testSearchTerm();
    }

    private void testTemp() throws Exception {

        JpaContext cf = LbApp.getInstance().getJpaContext();
        
        EntityController<Product, Integer> ec = cf.getEntityController(Product.class, Integer.class);

        int ID = 69;
        
        Product beforeProduct = ec.find(ID);
        
        Map map = ec.toMap(beforeProduct);
        
log("Map:\n"+map);        
     
        Availability soldOut = (Availability)cf.getEnumReferences().getEntity(References.availability.SoldOut);

log("SoldOut = "+soldOut);

        Availability beforeAvail = beforeProduct.getAvailabilityid();

        if(!beforeAvail.equals(soldOut)) {
            
            beforeProduct.setAvailabilityid(soldOut);

            ec.merge(beforeProduct);
            
            Product afterProduct = ec.find(ID);
            
            Availability afterAvail = afterProduct.getAvailabilityid();
            
log("Before: "+beforeAvail.getAvailability()+"="+beforeAvail.getAvailabilityid()+", After: "+afterAvail.getAvailability()+"="+afterAvail.getAvailabilityid());            
        }else{
log("Availability == SoldOut");            
        }
    }
    
    private void testSearchTerm() throws Exception {
        
        Select<Product> qb = new SelectProduct("baby", 
                LbApp.getInstance().getJpaContext(), Product.class);
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(qb)) {
    //log("Printing "+results0.getSize()+" results");

            int batchCount = results.getPageCount();
            batchCount = 1;

            for(int i=0; i<batchCount; i++) {

log("Printing batch: "+i);

                List<Product> batch = results.getPage(i); 

                for(Product product:batch) {

log(product);            
                }
            }
        }
    }
    
    public void printResults(SearchResults<Product> results) {
        
        StringBuilder builder = new StringBuilder(results.getSize() * 3);
        
        for(int i=0; i<results.getPageCount(); i++) {
            
            List<Product> batch = results.getPage(i);
            
            for(Product product:batch) {

                builder.append(product.getProductid()).append(',').append(' ');
            }
        }
log(builder);        
    }

    private static class MyQueryBuilder extends SelectProduct {

        public MyQueryBuilder(JpaContext jpaContext) {
            super(jpaContext);
        }

        public MyQueryBuilder(String query, JpaContext jpaContext, Class resultType) {
            super(query, jpaContext, resultType);
        }

        public MyQueryBuilder(EntityManager em, DatabaseFormat databaseFormat) {
            super(em, databaseFormat);
        }

        public MyQueryBuilder(String query, EntityManager em, Class resultType, DatabaseFormat databaseFormat) {
            super(query, em, resultType, databaseFormat);
        }

        @Override
        protected void format(String query) {
            super.format(query); 
            where(Product.class, Product_.availabilityid.getName(), References.availability.SoldOut);
        }
    }
    
    private void log(Object msg) {
System.out.println(this.getClass().getName()+". "+msg);        
    }
    private void log(Product msg) {
System.out.println(this.getClass().getName()+". "+msg.getProductid()+": "+msg.getAvailabilityid().getAvailability());        
    }
}
