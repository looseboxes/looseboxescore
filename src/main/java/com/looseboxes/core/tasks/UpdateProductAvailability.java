package com.looseboxes.core.tasks;

import com.bc.jpa.controller.EntityController;
import com.bc.jpa.fk.EnumReferences;
import java.util.logging.Logger;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Availability;
import com.looseboxes.pu.entities.Availability_;
import com.looseboxes.pu.entities.Product;
import com.looseboxes.pu.entities.Product_;
import com.looseboxes.pu.entities.Productvariant;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.search.BaseSearchResults;
import com.looseboxes.pu.query.SelectProductByAvailability;
import com.bc.jpa.dao.Select;
import java.util.Objects;

/**
 * @(#)UpdateProductAvailability.java   27-Jun-2015 14:46:07
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
public class UpdateProductAvailability implements Runnable, Serializable {
    
    private transient static final Logger LOG = Logger.getLogger(UpdateProductAvailability.class.getName());
    
    private final int batchSize;
    
    private final int quantityForLimitedAvailability;
    
    private final JpaContext jpa;
    
    private final EnumReferences enumRefs;

    public UpdateProductAvailability(JpaContext jpa) {
        this(jpa, 100, 1);
    }
    
    public UpdateProductAvailability(JpaContext jpa, 
            int batchSize, int quantityForLimitedAvailability) {
        this.jpa = Objects.requireNonNull(jpa);
        this.enumRefs = jpa.getEnumReferences();
        this.batchSize = batchSize;
        this.quantityForLimitedAvailability = quantityForLimitedAvailability;
    }
    
    @Override
    public void run() {
        try{
            
            final Enum [] arr = this.enumRefs.getValues(References.availability.class);

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
        
        final EntityController<Product, Integer> ec = this.jpa.getEntityController(Product.class, Integer.class);
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(select, batchSize, true)) {
            
            EnumMap<References.availability, Availability> cache = 
                    new EnumMap<>(References.availability.class);

            for(int i=0; i<results.getPageCount(); i++) {

                List<Product> products = results.getPage(i);

                for(Product product:products) {

                    Availability target = this.getTargetAvailability(cache, product);

                    if(target != null) {

                        Availability current = product.getAvailabilityid();

    //XLogger.getInstance().log(Level.FINEST, "Product ID: {0} current: {1}, target: {2}", 
    //this.getClass(), product.getProductid(), current.getAvailability()+"="+current.getAvailabilityid(), 
    //target.getAvailability()+"="+target.getAvailabilityid());

                        if(!target.equals(current)) {

                            if(true) {
                                Product update = ec.editById(product.getProductid(), Product_.availabilityid.getName(), target);
                            }else if(false) { 
    // No actual updates were observed. The data in database and on the website did not change
    // Futhermore, the sql log did not contain any --UPDATE queries                            
                                product.setAvailabilityid(target);
                                try{
                                    ec.merge(product);
                                }catch(Exception e) {
                                    if(LOG.isLoggable(Level.WARNING)){
                                        LOG.log(Level.WARNING, "{0}", e.toString());
                                    }
                                }
                            }else{
    // No actual updates were observed. The data in database and on the website did not change
    // Futhermore, the sql log did not contain any --UPDATE queries                            
                                EntityManager em = this.jpa.getEntityManager(Product.class);
                                try{
                                    EntityTransaction t = em.getTransaction();
                                    try{
                                        t.begin();
                                        product.setAvailabilityid(target);
                                        em.merge(product); 
                                        t.commit();
                                        ++updateCount;
                                    }finally{
                                        if(t.isActive()) {
                                            t.rollback();
                                        }
                                    }
                                }finally{
                                    em.close();
                                }
                            }
                            if(LOG.isLoggable(Level.FINER)){
                                LOG.log(Level.FINER, "Product ID: {0} updated availability from {1} to {2}", 
                                    new Object[]{ product.getProductid(),  current.getAvailability(),  target.getAvailability()});
                            }
                        }
                    }
                }
                if(LOG.isLoggable(Level.FINE)){
                    LOG.log(Level.FINE, "Spent:: memory: {0}, time: {1} updating {2} of {3} product's availabiltiy",
                        new Object[]{ (System.currentTimeMillis()-tb4),  updateCount,  results.getSize()});
                }
            }
        }
        
        return updateCount;
    }

    public int update1(References.availability availability) {

        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Availability: {0}", availability);
        }

        final long mb4 = com.bc.util.Util.availableMemory();
        final long tb4 = System.currentTimeMillis();

        int updateCount = 0;
        
        final Select select = new SelectProductByAvailability(availability, this.jpa);
        
        final EntityController<Product, Integer> ec = this.jpa.getEntityController(Product.class, Integer.class);
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(select, batchSize, true)) {

            EnumMap<References.availability, Availability> cache = 
                    new EnumMap<>(References.availability.class);
            
            for(int i=0; i<results.getPageCount(); i++) {

                List<Product> products = results.getPage(i);

                for(Product product : products) {

                    Availability target = this.getTargetAvailability(cache, product);

                    if(target != null) {

                        Availability current = product.getAvailabilityid();

        //XLogger.getInstance().log(Level.FINEST, "Product ID: {0} current: {1}, target: {2}", 
        //this.getClass(), product.getProductid(), current.getAvailability()+"="+current.getAvailabilityid(), 
        //target.getAvailability()+"="+target.getAvailabilityid());

                        if(!target.equals(current)) {

        //                    update.setProductid(product.getProductid());
        //                    update.setAvailabilityid(target);

                            product.setAvailabilityid(target);

                            try{
                                
                                ec.merge(product);

                                ++updateCount;
                                
                                if(LOG.isLoggable(Level.FINER)){
                                    LOG.log(Level.FINER, "Product ID: {0} updated availability from {1} to {2}", 
                                        new Object[]{ product.getProductid(),  current.getAvailability(),  target.getAvailability()});
                                }
                                
                            }catch(Exception e) {
                                
                                if(LOG.isLoggable(Level.WARNING)){
                                    LOG.log(Level.WARNING, "{0}", e.toString());
                                }
                            }
                        }
                    }
                }
            }    
            if(LOG.isLoggable(Level.FINE)){
                LOG.log(Level.FINE, "Spent:: memory: {0}, time: {1} updating {2} of {3} product's availabiltiy", 
                    new Object[]{ (System.currentTimeMillis()-tb4),  updateCount,  results.getSize()});
            }
        }
        
        return updateCount;
    }
    
    public int update2(References.availability availability) {

        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Availability: {0}", availability);
        }

        final long mb4 = com.bc.util.Util.availableMemory();
        final long tb4 = System.currentTimeMillis();

        int updateCount = 0;
            
        final Select qb = new SelectProductByAvailability(availability, this.jpa);
        
        try(BaseSearchResults<Product> results = new BaseSearchResults(qb, batchSize, true)) {

            EnumMap<References.availability, Availability> cache = 
                    new EnumMap<>(References.availability.class);

            for(int i=0; i<results.getPageCount(); i++) {

                List<Product> products = results.getPage(i);

                EntityManager em = this.jpa.getEntityManager(Product.class);

                try{
                    
                    try{

                        em.getTransaction().begin();

                        updateCount += this.update(em, products, cache);

                        em.getTransaction().commit();

                    }finally{
                        
                        EntityTransaction t = em.getTransaction();

                        if(t.isActive()) {
                            t.rollback();
                        }
                    }
                }finally{
                    em.close();
                }
            }
            
            if(LOG.isLoggable(Level.FINE)){
                LOG.log(Level.FINE, "Spent:: memory: {0}, time: {1} updating {2} of {3} product's availabiltiy", 
                    new Object[]{ (System.currentTimeMillis()-tb4),  updateCount,  results.getSize()});
            }
        }
        
        return updateCount;
    }
    
    private int update(
            EntityManager em, Collection<Product> products, 
            EnumMap<References.availability, Availability> cache) {
        
        int updateCount = 0;
        
        for(Product product:products) {

            Availability target = this.getTargetAvailability(cache, product);
            
            if(target != null) {

                Availability current = product.getAvailabilityid();

//XLogger.getInstance().log(Level.FINEST, "Product ID: {0} current: {1}, target: {2}", 
//this.getClass(), product.getProductid(), current.getAvailability()+"="+current.getAvailabilityid(), 
//target.getAvailability()+"="+target.getAvailabilityid());
                
                if(!target.equals(current)) {

                    product.setAvailabilityid(target);

                    Product merged = em.merge(product); 
                    
                    if(LOG.isLoggable(Level.FINER)){
                        LOG.log(Level.FINER, "Product ID: {0} updated availability from {1} to {2}", 
                            new Object[]{ product.getProductid(),  current.getAvailability(),  target.getAvailability()});
                    }

                    ++updateCount;
                }
            }
        }
        
        return updateCount;
    }
    
    public Availability getTargetAvailability(EnumMap<References.availability, Availability> cache, Product product) {
        
        Availability target;
        
        References.availability targetEnum = this.getTargetAvailability(product);

        if(targetEnum != null) {

            target = cache.get(targetEnum);

            if(target == null) {
                target = (Availability)this.enumRefs.getEntity(targetEnum);
                cache.put(targetEnum, target);
            }
        }else{
            target = null;
        }    
        return target;
    }
    
    public References.availability getTargetAvailability(Product product) {
        
        List<Productvariant> variants = product.getProductvariantList();

        int qty = 0;

        for(Productvariant variant:variants) {

            qty += variant.getQuantityInStock();
        }
        
        Short aid = product.getAvailabilityid().getAvailabilityid();
        
        final Enum current = this.enumRefs.getEnum(Availability_.availabilityid.getName(), aid.intValue());

        References.availability target;
        
        if(current != References.availability.SoldOut && qty <= 0) {

            target = References.availability.SoldOut;
            
        }else if(current != References.availability.LimitedAvailability && qty == quantityForLimitedAvailability) {
            
            target = References.availability.LimitedAvailability;
            
        }else if(current != References.availability.InStock && qty > quantityForLimitedAvailability){
            
            target = References.availability.InStock;
            
        }else{
            
            target = null;
        }

        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Product ID: {0}, Quantity in stock: {1}, Availability; current: {2}, target: {3}", 
                new Object[]{ product.getProductid(),  qty,  current,  target});
        }

        return target;
    }

    public int getQuantityForLimitedAvailability() {
        return quantityForLimitedAvailability;
    }

    public int getBatchSize() {
        return batchSize;
    }
}
