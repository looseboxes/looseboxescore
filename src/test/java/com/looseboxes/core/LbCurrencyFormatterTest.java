/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.looseboxes.core;

import com.bc.util.CurrencyFormatter;
import java.util.Date;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class LbCurrencyFormatterTest {
    
    public LbCurrencyFormatterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of various methods, of class LbCurrencyFormatter.
     */
    @Test
    public void test() {
        System.out.println("getRate");
        final Date date = new Date();
        final CurrencyFormatter fmt = new LbCurrencyFormatter();
        
        Object num = "34,000.50";
        Object obj = fmt.numberToPrice(num, Locale.getDefault());
System.out.println(num+" to price: "+obj);        

        num = fmt.priceToNumber(obj, Locale.getDefault());
System.out.println(obj+" to number: "+num);        
        
        obj = fmt.numberToPrice(num, Locale.getDefault(), Locale.US);
System.out.println("Nigeria "+num+" to US price: "+obj);        
        
        num = fmt.priceToNumber(obj, Locale.US, Locale.getDefault());
System.out.println("US "+obj+" to Nigeria number: "+num);        
    }
}
