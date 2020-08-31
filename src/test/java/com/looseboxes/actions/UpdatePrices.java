package com.looseboxes.actions;

import com.bc.jpa.context.JpaContext;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Product_;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 3:47:58 PM
 */
public class UpdatePrices implements Callable<Map<Integer, Integer>> {

    private static final Logger logger = Logger.getLogger(UpdatePrices.class.getName());

    private final JpaContext jpaContext;
    
    private final Callable<Map<Integer, BigDecimal>> inputSource;
    
    private final BigDecimal discountFactor = new BigDecimal(0.5);

    public UpdatePrices(JpaContext jpaContext, final Path path) {
        this(jpaContext, new ImportPrices(path));
    }
    
    public UpdatePrices(JpaContext jpaContext, Callable<Map<Integer, BigDecimal>> inputSource) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.inputSource = Objects.requireNonNull(inputSource);
    }
    
    @Override
    public Map<Integer, Integer> call() throws Exception {
        
        final Map<Integer, BigDecimal> input = inputSource.call();
        
        final Map<Integer, Integer> output = new LinkedHashMap(input.size() + 1, 1.0f);
        
        final EntityManager em = jpaContext.getEntityManager(Product.class);
        try{
        
            final Set<Entry<Integer, BigDecimal>> entrySet = input.entrySet();
            
            em.getTransaction().begin();
            
            for(Entry<Integer, BigDecimal> entry : entrySet) {
                
                final Integer productId = entry.getKey();
                
                final BigDecimal price = getPrice(entry.getValue());
                
                final BigDecimal discount = getDiscount(entry.getValue());
                
                if(logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, "Product ID: {0}, discount price: {1}, price: {2}, discount: {3}", 
                            new Object[]{productId, entry.getValue(), price, discount});
                }
                
                final CriteriaBuilder cb = em.getCriteriaBuilder();
                final CriteriaUpdate<Product> update = cb.createCriteriaUpdate(Product.class);
                final Root<Product> product = update.from(Product.class);
                update.where(cb.equal(product.get(Product_.productid), productId))
                        .set(product.get(Product_.price), price)
                        .set(product.get(Product_.discount), discount);
                
                em.createQuery(update).executeUpdate();
                
            }
            
            em.getTransaction().commit();
            
        }finally{
            em.close();
        }
        
        return output;
    }
    
    public BigDecimal getPrice(BigDecimal discountPrice) {
        return discountPrice.add(this.getDiscount(discountPrice));
    }

    public BigDecimal getDiscount(BigDecimal discountPrice) {
        return discountPrice.multiply(this.discountFactor);
    }
}
