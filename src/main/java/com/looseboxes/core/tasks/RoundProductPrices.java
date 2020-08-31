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

import com.bc.jpa.dao.util.EntityReference;
import com.bc.jpa.dao.util.EntityReferenceImpl;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.dao.Select;
import com.bc.jpa.dao.search.BaseSearchResults;
import com.bc.util.Util;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Availability;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Product_;
import com.looseboxes.pu.query.SelectProduct;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 11, 2018 9:42:26 PM
 */
public class RoundProductPrices implements Runnable, Serializable {

    private transient static final Logger LOG = Logger.getLogger(RoundProductPrices.class.getName());

    private final PersistenceUnitContext puContext;
    
    private final int batchSize;
    
    private final int toNearest;

    public RoundProductPrices(PersistenceUnitContext puContext) {
        this(puContext, 100);
    }
    
    public RoundProductPrices(PersistenceUnitContext puContext, int batchSize) {
        this.puContext = Objects.requireNonNull(puContext);
        this.batchSize = batchSize;
        this.toNearest = 100;
    }
    
    @Override
    public void run() {
        try{
            
            final EntityReference entityRef = new EntityReferenceImpl(this.puContext);
            
            final String [] names = {
                References.availability.InStock.getLabel(),
                References.availability.LimitedAvailability.getLabel()
            };
            
            for(String name : names) {
                
                final Availability av = entityRef.find(Availability.class, name);
                
                this.update(av);
            }
        }catch(RuntimeException e) {
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), e);
            }
        }
    }
    
    public int update(Availability availability) {
        
        if(LOG.isLoggable(Level.FINE)){
            LOG.log(Level.FINE, "Availability: {0}", availability.getAvailability());
        }

        final long mb4 = com.bc.util.Util.availableMemory();
        final long tb4 = System.currentTimeMillis();

        int updateCount = 0;
        
        final Select<Product> select = new SelectProduct<Product>(this.puContext)
                .where(Product.class, Product_.availabilityid, availability);
        
        final Round round = new Round();

        final Predicate<BigDecimal> endsWith100 = new GreaterThanThousandEndsWithHundredTest();
        
        final BigDecimal hundred = new BigDecimal("100");
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(select, batchSize, true)) {
            
            final EntityManager em = select.getEntityManager();
            
            this.begin(em);
            
            for(int i=0; i<results.getPageCount(); i++) {

                List<Product> products = results.getPage(i);

                for(Product product : products) {
                    
                    final BigDecimal discount = product.getDiscount();
                    
                    BigDecimal discount_updated = this.roundDown(round, "Discount", discount);
                    
                    final BigDecimal price = product.getPrice();

                    BigDecimal price_updated = this.roundDown(round, "Price", price);
                    
                    if(!this.isUpdated(discount, discount_updated, price, price_updated)) {
                        continue;
                    }
                    
                    final BigDecimal sub = discount_updated == null ? price_updated :
                            price_updated.subtract(discount_updated);

                    if(endsWith100.test(sub)) {
                        if(discount_updated != null) {
//                            System.out.println(">>> Adding 100 to: " + discount_updated);
                            discount_updated = discount_updated.add(hundred);
                        }else{
//                            System.out.println(">>> Subtracting 100 from: " + price_updated);
                            price_updated = price_updated.subtract(hundred);
                        }
                    }
                    
                    System.out.println("Updates for ID: " + product.getProductid() + 
                            "\tdiscount: " + discount + " = " + discount_updated + 
                            "\tprice: " + price + " = " + price_updated);

                    if(discount_updated != null) {
                        product.setDiscount(discount_updated);
                    }

                    if(price_updated != null) {
                        product.setPrice(price_updated);
                    }

                    this.persist(em, product); 

                    ++updateCount;
                }
                
                if(LOG.isLoggable(Level.FINE)){
                    LOG.log(Level.FINE, "Spent:: memory: {0}, time: {1} updating {2} of {3} product's availabiltiy", 
                        new Object[]{Util.usedMemory(mb4), (System.currentTimeMillis()-tb4),  updateCount,  results.getSize()});
                }
            }
            
            this.commit(em);
        }
        
        return updateCount;
    }
    
    public boolean isUpdated(BigDecimal discount, BigDecimal discount_updated,
            BigDecimal price, BigDecimal price_updated) {
        final boolean updated;
        if(discount != null) {
            updated = !valueEquals(discount, discount_updated) || !valueEquals(price, price_updated);
        }else{
            updated = !valueEquals(price, price_updated); 
        }
        
        return updated;
    }
    
    public boolean valueEquals(BigDecimal lhs, BigDecimal rhs) {
        return lhs.doubleValue() == rhs.doubleValue();
    }
    
    public void begin(EntityManager em) {
        em.getTransaction().begin();
    }
    
    public void persist(EntityManager em, Object entity) {
        em.persist(entity);
    }
    
    public void commit(EntityManager em) {
        this.puContext.commit(em.getTransaction());
    }
    
    private BigDecimal roundDown(Round round, String name, BigDecimal value) {
        
        if(value != null) {
            
            final int output = round.down(value.intValue(), toNearest);

            LOG.finer(() -> name + ": "+value+", updated to: "+output);                        

            return new BigDecimal(Integer.toString(output));
            
        }else{
            
            return null;
        }
    }
}
