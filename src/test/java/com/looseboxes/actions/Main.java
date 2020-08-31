/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.looseboxes.actions;

import com.bc.jpa.context.JpaContext;
import com.looseboxes.pu.LbJpaContext;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 4:00:16 PM
 */
public class Main {
    
    private transient static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String [] args) {

        int exitCode = 1;
        
        final String userHome = System.getProperty("user.home");
        
        final URI persistenceUri = Paths.get(userHome, 
                "Documents", "NetBeansProjects", "looseboxespu", 
                "src", "test", "resources", "META-INF", "persistence.xml").toUri();
        
        try(JpaContext jpa = new LbJpaContext(persistenceUri)) {

            final Path path = Paths.get(userHome, 
                "Documents", "uploads_to_buzzwears", "price_updates", 
                "Book1_prices_2017_08_26.csv");
            
            final Callable<Map<Integer, BigDecimal>> inputSupplier = new ImportPrices(path);

            new UpdatePrices(jpa, inputSupplier).call();
            
            exitCode = 0;
            
        }catch(Exception e) {

            logger.log(Level.WARNING, "", e);
            
        }finally{
            
            System.exit(exitCode);
        }        
    }
}
