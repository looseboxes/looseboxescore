package com.looseboxes.core.tasks;

import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.search.BaseSearchResults;
import java.util.logging.Logger;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.query.SelectProductByAvailability;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import com.bc.jpa.dao.Select;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Dec 15, 2016 10:34:37 PM
 */
public class UpdateProductPrices implements Runnable, Serializable {
    
    private transient static final Logger LOG = Logger.getLogger(UpdateProductPrices.class.getName());
    
    private final JpaContext jpa;
    
    private final float factor;
    
    private int batchSize;
    
    private final MathContext mathContext;

    public UpdateProductPrices(JpaContext jpa, float factor) {
        this(jpa, factor, 100);
    }
    
    public UpdateProductPrices(JpaContext jpa, float factor, int batchSize) {
        this.jpa = Objects.requireNonNull(jpa);
        this.factor = factor;
        this.batchSize = batchSize;
        this.mathContext = new MathContext(14, RoundingMode.HALF_UP);
    }
    
    @Override
    public void run() {
        try{
            
            Enum [] arr = this.jpa.getEnumReferences().getValues(References.availability.class);

            for(Enum en:arr) {
                this.update((References.availability)en);
            }
        }catch(RuntimeException e) {
            if(LOG.isLoggable(Level.WARNING)){
                LOG.log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), e);
            }
        }
    }
    
    public int update(References.availability availability) {

        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Availability: {0}", availability);
        }

        final long mb4 = com.bc.util.Util.availableMemory();
        final long tb4 = System.currentTimeMillis();

        int updateCount = 0;

        final Select select = new SelectProductByAvailability(availability, this.jpa);
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(select, batchSize, true)) {
            
            final EntityManager em = select.getEntityManager();
            
            this.begin(em);
            
            for(int i=0; i<results.getPageCount(); i++) {

                List<Product> products = results.getPage(i);

                for(Product product : products) {
                    
                    final BigDecimal discount = product.getDiscount();
                    
                    if(discount != null) {
                        BigDecimal discount_updated = discount.multiply(new BigDecimal(String.valueOf(this.factor)), this.mathContext);
                        LOG.finer(() -> "Discount: "+discount+", updated to: "+discount_updated);                        
                        product.setDiscount(discount_updated);
                    }
                    
                    final BigDecimal price = product.getPrice();
                    if(price != null) {
                        BigDecimal price_updated = price.multiply(new BigDecimal(String.valueOf(this.factor)), this.mathContext);
                        LOG.finer(() -> "Price: "+price+", updated to: "+price_updated);                        
                        product.setPrice(price_updated);
                    }
                    
                    this.persist(em, product);
                }
                if(LOG.isLoggable(Level.FINE)){
                    LOG.log(Level.FINE, "Spent:: memory: {0}, time: {1} updating {2} of {3} product's availabiltiy", 
                        new Object[]{ (System.currentTimeMillis()-tb4),  updateCount,  results.getSize()});
                }
            }
            
            this.commit(em);
        }
        
        return updateCount;
    }

    public void begin(EntityManager em) {
        em.getTransaction().begin();
    }
    
    public void persist(EntityManager em, Object entity) {
        em.persist(entity);
    }
    
    public void commit(EntityManager em) {
        this.jpa.commit(em.getTransaction());
    }
    
    public final int getBatchSize() {
        return batchSize;
    }
}
