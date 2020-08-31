/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.looseboxes.core.tasks;

import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.eclipselink.PersistenceContextEclipselinkOptimized;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import java.net.URI;
import java.nio.file.Paths;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class RoundProductPricesTest {
    
    private static PersistenceUnitContext puContext;
    
    public RoundProductPricesTest() { }

    @BeforeClass
    public static void setUpClass() {
        puContext = createPersistenceUnitContext();
    }
    
    /**
     * Test of run method, of class RoundProductPrices.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        
        final RoundProductPrices instance = new RoundProductPrices(puContext){
//            @Override
//            public void begin(EntityManager em) { }
//            @Override
//            public void persist(EntityManager em, Object entity) { }
//            @Override
//            public void commit(EntityManager em) { }
        };
        
        instance.run();
    }

    /**
     * Test of is method, of class RoundProductPrices.
     */
    private static PersistenceUnitContext createPersistenceUnitContext() {
        final URI uri = Paths.get(System.getProperty("user.home"), "Documents", 
                "NetBeansProjects", "looseboxespu", "src", "test", "resources", 
                "META-INF", "persistence.xml").toUri();
        final PersistenceContext jpa = new PersistenceContextEclipselinkOptimized(
                uri, new MySQLDateTimePatterns());
        return jpa.getContext("looseboxesPU");
    }
}
