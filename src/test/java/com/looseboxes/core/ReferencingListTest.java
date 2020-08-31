package com.looseboxes.core;

import com.bc.jpa.controller.EntityController;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Productcomment;
import com.looseboxes.pu.entities.Productcomment_;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.jpa.context.JpaContext;

/**
 * @author Josh
 */
public class ReferencingListTest {
    
    private transient final Logger logger = Logger.getLogger(ReferencingListTest.class.getName());
    
    public ReferencingListTest() { }
    
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

    @Test
    public void testAll() throws Exception {
        this.testAddToList();
        this.testAddList();
    }
    
    public void printCommentList(Integer productId) {
        JpaContext cf = LbApp.getInstance().getJpaContext();
        EntityController<Productcomment, ?> ec = cf.getEntityController(Productcomment.class);
        Map where = Collections.singletonMap(Productcomment_.productid.getName(), productId);
        List<Productcomment> comments = ec.select(where, null);
logger.log(Level.INFO, "Product ID {0}, comments: {1}", new Object[]{productId, comments});        
    }
    
    public void testAddToList() {
logger.log(Level.INFO, "#testAddToList");
        Product product = this.getRandomProduct();
        
logger.log(Level.INFO, "#testAddToList. Selected product: {0}", product);
        if(product == null) {
            return;
        }
        
        EntityManager em = this.getEntityManager();
        
        em.getTransaction().begin();
        
// Get a managed instance        
//        
        product = em.merge(product);
        
        List<Productcomment> comments = product.getProductcommentList();
        if(comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(this.createProductcomment(product, 0));
        comments.add(this.createProductcomment(product, 1));

        em.getTransaction().commit();
        
this.printCommentList(product.getProductid());
    }

    public void testAddList() {
logger.log(Level.INFO, "#testAddList");
        Product product = this.getRandomProduct();
logger.log(Level.INFO, "#testAddList. Selected product: {0}", product);
        if(product == null) {
            return;
        }
        
        EntityManager em = this.getEntityManager();
        
        em.getTransaction().begin();
        
// Get a managed instance        
//        
        product = em.merge(product);
        
        List<Productcomment> comments = new ArrayList<>();
        comments.add(this.createProductcomment(product, 0));
        comments.add(this.createProductcomment(product, 1));
        product.setProductcommentList(comments);
        
        em.getTransaction().commit();
        
this.printCommentList(product.getProductid());
    }
    
    public Productcomment createProductcomment(Product product, int i) {
        Productcomment c = new Productcomment();
        c.setProductid(product);
        c.setCommentText("This is a sample comment "+i+" for product: "+product.getProductid()+", "+product.getProductName());
        c.setDatecreated(new Date());
        return c;
    }
    
    public Product getRandomProduct() {
        List<Product> products = this.getProducts();
        if(products == null || products.isEmpty()) {
System.out.println(this.getClass().getName()+". No products in database to test");            
            return null;
        }else{
            int random = com.bc.util.Util.randomInt(products.size());
            return products.get(random);
        }
    }

    public EntityManager getEntityManager() {
        JpaContext cf = LbApp.getInstance().getJpaContext();
        EntityController<Product, ?> ec = cf.getEntityController(Product.class);
        return ec.getEntityManager();
    }
    
    public List<Product> getProducts() {
        JpaContext cf = LbApp.getInstance().getJpaContext();
        EntityController<Product, ?> ec = cf.getEntityController(Product.class);
        List<Product> products = ec.find();
        return products;
    }
}
