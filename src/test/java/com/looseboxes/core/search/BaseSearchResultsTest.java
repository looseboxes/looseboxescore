package com.looseboxes.core.search;

import com.bc.jpa.dao.search.BaseSearchResults;
import com.bc.jpa.dao.search.SearchResults;
import com.looseboxes.core.LbApp;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Product_;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.Select;


/**
 * @(#)ParamSearchResultsTest.java   27-Jun-2015 20:51:55
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class BaseSearchResultsTest {

    public BaseSearchResultsTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testAll() throws Exception {
        
        LbApp.getInstance().init("file:/C:/Users/Josh/Documents/NetBeansProjects/looseboxespu/src/test/resources/META-INF/persistence.xml");
        
        final JpaContext cf = LbApp.getInstance().getJpaContext();
        
        final Class<Product> entityType = Product.class;
        
        Select<Object[]> qb = cf.getDaoForSelect(entityType, Object[].class);
        
        final Object id = cf.getEnumReferences().getId(References.availability.InStock);
        
        qb.from(entityType)
            .select(Product_.productid.getName(), Product_.productName.getName())
            .where(Product_.availabilityid.getName(), Select.EQUALS, id);
        
        BaseSearchResults<Object[]> results = new BaseSearchResults<>(qb);
        
log("Results: "+results.getSize());

printResults(results);

    }
    
    public void printResults(SearchResults<Object[]> results) {
        StringBuilder builder = new StringBuilder(results.getSize() * 20);
        for(int i=0; i<results.getPageCount(); i++) {
            
            List<Object[]> batch = results.getPage(i);
            
            for(Object[] row:batch) {
                builder.append('\n').append(row[0]).append('=').append(row[1]);
            }
        }
log(builder);        
    }

    private void log(Object msg) {
System.out.println(this.getClass().getName()+". "+msg);        
    }
}
