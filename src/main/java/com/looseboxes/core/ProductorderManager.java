package com.looseboxes.core;

import com.bc.jpa.controller.EntityController;
import com.bc.jpa.exceptions.NonexistentEntityException;
import com.bc.jpa.exceptions.PreexistingEntityException;
import java.util.logging.Logger;
import com.looseboxes.pu.entities.Orderproduct;
import com.looseboxes.pu.entities.Orderproduct_;
import com.looseboxes.pu.entities.Productorder;
import com.looseboxes.pu.entities.Productvariant;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.Select;


/**
 * @(#)ProductorderSync.java   20-Jul-2015 02:45:34
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
public class ProductorderManager implements Serializable {
    private transient static final Logger LOG = Logger.getLogger(ProductorderManager.class.getName());
    
    public ProductorderManager() { }
    
    public JpaContext getJpaContext() {
        return LbApp.getInstance().getJpaContext();
    }
    
    public int getItemCount(Productorder order) {
        
        int itemCount  = 0;
        
        if(order != null) {
        
            List<Orderproduct> orderItems = order.getOrderproductList();
            
            itemCount = this.getItemCount(orderItems);
        }

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Order: {0}, Total items: {1}",new Object[]{ order,  itemCount});
}        

        return itemCount;
    }
    
    public int getItemCount(List<Orderproduct> orderItems) {
        
        int itemCount  = 0;
        
        if(orderItems != null) {

            for(Orderproduct orderItem:orderItems) {

                itemCount += orderItem.getQuantity();
            }
        }

        return itemCount;
    }

    public void validateQuantities(Productorder order) {
        
        List<Orderproduct> orderproducts = order.getOrderproductList();
        
        for(Orderproduct orderproduct:orderproducts) {

            this.validateQuantities(orderproduct);

            int ordered = orderproduct.getQuantity();
            Productvariant unit = orderproduct.getProductvariantid();
            int instock = unit.getQuantityInStock();

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Order product ID: {0}, instock: {1}, ordered: {2}", 
new Object[]{ orderproduct.getOrderproductid(),  instock,  ordered});
}

            if(ordered > instock) {
                throw new UnsupportedOperationException("Ordered "+ordered+" > InStock: "+instock+" for orderproduct: "+orderproduct);
            }
        }
    }
    
    public void updateQuantityInStock(Productorder order, boolean increment) {

        JpaContext cf = LbApp.getInstance().getJpaContext();
        
        EntityManager em = cf.getEntityManager(Productorder.class);
        
        try{
            
            EntityTransaction t = em.getTransaction();
            
            try{
                
                t.begin();
                
                this.updateQuantityInStock(em, order, increment);
                
                t.commit();
                
            }finally{
                if(t.isActive()) {
                    t.rollback();
                }
            }
        }finally{
            em.close();
        }
    }
    
    public void updateQuantityInStock(EntityManager em, Productorder order, boolean increment) {
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Item count: {0}, order: {1}",new Object[]{ this.getItemCount(order),  order});
}
// Get a managed instance
//                
        order = em.merge(order);

        List<Orderproduct> orderproducts = order.getOrderproductList();

        for(Orderproduct orderproduct:orderproducts) {

            if(!increment) {
                this.validateQuantities(orderproduct);
            }

            int ordered = orderproduct.getQuantity();
            Productvariant unit = orderproduct.getProductvariantid();
            int instock = unit.getQuantityInStock();

            int update = increment ? instock + ordered : instock - ordered;

if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "Order product ID: {0}, instock: {1}, ordered: {2}, update: {3}", 
new Object[]{ orderproduct.getOrderproductid(),  instock,  ordered,  update});
}

            if(update < 0) {
                throw new UnsupportedOperationException();
            }

            unit.setQuantityInStock(update);
        }
    }

    private void validateQuantities(Orderproduct orderproduct) {
        int ordered = orderproduct.getQuantity();
        Productvariant unit = orderproduct.getProductvariantid();
        if(ordered < 1) {
            throw new UnsupportedOperationException("Quantity ordered: "+ordered+" < 1. Orderproduct ID: "+orderproduct.getOrderproductid()+", Productvariant ID: "+unit.getProductvariantid());
        }
        int instock = unit.getQuantityInStock();
        if(instock < 1) {
            throw new UnsupportedOperationException("Quantity in stock: "+instock+" < 1. Orderproduct ID: "+orderproduct.getOrderproductid()+", Productvariant ID: "+unit.getProductvariantid());
        }
    }
    
    public int trimQuantities(Productorder order, boolean syncWithDatabase) {
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "BEFORE TRIM QUANTITIES, itemcount: {0}, order: {1}", 
new Object[]{ this.getItemCount(order),  order});
}
        
        int removedOrderproducts = 0;

        int removedVariants = 0;

        JpaContext cf = this.getJpaContext();

        EntityManager em = cf.getEntityManager(Productorder.class);
        
        try{

            EntityTransaction t = null;

            try{

                if(syncWithDatabase) {
                    
                    t = em.getTransaction();
                    
                    t.begin();
        
// Get a managed instance
//        
                    order = em.merge(order);
                }

                List<Orderproduct> orderproducts = order.getOrderproductList();

                Iterator<Orderproduct> iter = orderproducts.iterator();
                
                while(iter.hasNext()) {

                    Orderproduct orderproduct = iter.next();

                    int ordered = orderproduct.getQuantity();

                    if(ordered < 1) {
                        iter.remove();
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Removed orderitem: {0} with productvariant ID: {1}",new Object[]{ orderproduct,  orderproduct.getProductvariantid().getProductvariantid()});
}
                        ++removedOrderproducts;
                        continue;
                    }

                    Productvariant unit = orderproduct.getProductvariantid();

                    int instock = unit.getQuantityInStock();

                    if(instock < 1) {
                        iter.remove();
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Removed orderitem: {0} with productvariant ID: {1}",new Object[]{ orderproduct,  orderproduct.getProductvariantid().getProductvariantid()});
}
                        ++removedOrderproducts;
                        continue;
                    }

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Order product ID: {0}, instock: {1}, ordered: {2}", 
new Object[]{ orderproduct.getOrderproductid(),  instock,  ordered});
}

                    if(ordered > instock) {

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Updated order quantity from {0} to {1} for {2}, {3}", 
new Object[]{ orderproduct.getQuantity(),  instock,  orderproduct,  orderproduct.getProductvariantid().getProductvariantid()});
}

                        orderproduct.setQuantity(instock);

                        removedVariants += (ordered - instock);
                    }
                }
    
                if(syncWithDatabase && t != null) {
                    t.commit();
                }

            }finally{
                if(t != null && t.isActive()) {
                    t.rollback();
                }
            }
        }finally{

            em.close();
        }

if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "AFTER TRIM QUANTITIES, itemcount: {0}, order: {1}", 
new Object[]{ this.getItemCount(order),  order});
}
        
        return removedVariants;
    }
    
    public void sync(Productorder update, Productorder toUpdate, boolean updateDatabase) 
            throws PreexistingEntityException, NonexistentEntityException, Exception {

        if(update.getProductorderid() != null &&
                toUpdate.getProductorderid() != null &&
                update.equals(toUpdate)) {
            throw new UnsupportedOperationException("Trying to sync a "+update.getClass().getName()+" with itself");
        }
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Src order, buyer: {0}, Tgt order, buyer: {1}",
new Object[]{ update.getBuyer(),  toUpdate.getBuyer()});
}

        if(updateDatabase) {
            if(toUpdate.getBuyer() == null && update.getBuyer() == null) {
                throw new NullPointerException(); 
            }
        }
        
        if(updateDatabase) {

if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "Syncing shopping cart order with database", this.getClass());
}
            
            JpaContext cf = this.getJpaContext();
            
            EntityController<Productorder, Integer> ec = cf.getEntityController(Productorder.class, Integer.class);
            
            // Editing or an entity which does not have id is problematic
            //
            final boolean edit = toUpdate.getProductorderid() != null;
            
            EntityManager em = ec.getEntityManager();

            try{

                EntityTransaction t = em.getTransaction();

                try{

                    t.begin();
                    
                    // Next step makes our entity managed so that changes will
                    // be synchronized with the database
                    //
                    if(edit) {
                        toUpdate = em.merge(toUpdate);
                    }else{
                        em.persist(toUpdate);
                    }
                    
                    this.sync(update, toUpdate);

                    t.commit();

                }finally{
                    if(t.isActive()) {
                        t.rollback();
                    }
                }
            }finally{
                em.close();
            }
        }else{
            this.sync(update, toUpdate);
        }    
    }
    
    public void sync(Productorder update, Productorder toUpdate) {
        
        this.sync(update, toUpdate, null, null, false);
    }
    
    public void sync(Productorder update, Productorder toUpdate, 
            List<Orderproduct> appendAdded, List<Orderproduct> appendUpdated, boolean cascade) {
        
        if(update.getProductorderid() != null &&
                toUpdate.getProductorderid() != null &&
                update.equals(toUpdate)) {
            throw new UnsupportedOperationException("Trying to sync a "+update.getClass().getName()+" with itself");
        }
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Src order, buyer: {0}, Tgt order, buyer: {1}",
new Object[]{ update.getBuyer(),  toUpdate.getBuyer()});
}

        if(toUpdate.getBuyer() == null) {
            toUpdate.setBuyer(update.getBuyer());
        }
        if(toUpdate.getDatecreated() == null) {
            toUpdate.setDatecreated(update.getDatecreated());
        }
        
        this.syncOrderproducts(update, toUpdate, appendAdded, appendUpdated);
        
        if(toUpdate.getOrderstatusid() == null) { 
            toUpdate.setOrderstatusid(update.getOrderstatusid());
        }
        if(toUpdate.getOrderDate() == null) {
            toUpdate.setOrderDate(update.getOrderDate());
        }
        if(cascade && toUpdate.getPayment() == null) {
            toUpdate.setPayment(update.getPayment());
        }
        if(toUpdate.getProductorderid() == null) {
            toUpdate.setProductorderid(update.getProductorderid());
        }
        if(toUpdate.getRequiredDate() == null) {
            toUpdate.setRequiredDate(update.getRequiredDate());
        }
        if(cascade && toUpdate.getShippingdetails() == null) {
            toUpdate.setShippingdetails(update.getShippingdetails());
        }
//        if(toUpdate.getTimemodified() == null) {
//            toUpdate.setTimemodified(update.getTimemodified());
//        }
    }
    
    private void syncOrderproducts(
            Productorder update, Productorder toUpdate,
            List<Orderproduct> appendAdded, List<Orderproduct> appendUpdated) {
        
        List<Orderproduct> itemsUpdate = update.getOrderproductList();
        
        List<Orderproduct> itemsToUpdate = toUpdate.getOrderproductList();

        if(LOG.isLoggable(Level.FINE)){
            LOG.log(Level.FINE, "Adding {0} items to {1} existing", 
                    new Object[]{this.getItemCount(itemsUpdate), this.getItemCount(itemsToUpdate)});
        }


        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Items update: {0}\nItems to update: {1}",
                    new Object[]{ itemsUpdate,  itemsToUpdate});
        }        
        
        if(itemsUpdate != null && !itemsUpdate.isEmpty()) {

            final boolean listCreated;
            
            if((listCreated = itemsToUpdate == null)) {
                itemsToUpdate = new ArrayList<>();
            }

            for(Orderproduct itemUpdate:itemsUpdate) {
                
                Orderproduct selectedItemToUpdate = null;
                
                for(Orderproduct itemToUpdate:itemsToUpdate) {
                    
                    if(itemUpdate.getProductvariantid().equals(itemToUpdate.getProductvariantid())) {
                        
                        selectedItemToUpdate = itemToUpdate;
                        
                        this.mergeQuantities(itemUpdate, itemToUpdate);
                        
                        break;
                    }
                }
                
                if(selectedItemToUpdate != null) {
                    if(appendUpdated != null) {
                        appendUpdated.add(selectedItemToUpdate);
                    }
                }else{
                    
                    Orderproduct copy = this.copy(itemUpdate, true);
                    
                    copy.setProductorderid(toUpdate);
                    
                    if(copy.getDatecreated() == null) {
                        copy.setDatecreated(new Date());
                    }
                    
                    if(appendAdded != null) {
                        appendAdded.add(copy);
                    }
                    
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Adding: {0}", copy);
}
                    itemsToUpdate.add(copy);
                }
            }
            
if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "After merging items: {0}", this.getItemCount(itemsToUpdate));
}
            
//            List<Orderproduct> notInDatabase = this.getItemsNotInDatabase(itemsToUpdate);

//XLogger.getInstance().log(Level.FINER, "After merging items not in database: {0}, {1}", 
//        this.getClass(), this.getItemCount(notInDatabase), notInDatabase);
            
//            if(notInDatabase != null && !notInDatabase.isEmpty()) {

//                this.persist(notInDatabase);
//            }
            
            if(listCreated) {
                toUpdate.setOrderproductList(itemsToUpdate);
            }
        }
    }
    
    private int mergeQuantities(Orderproduct src, Orderproduct tgt) {
        int srcQty = src.getQuantity() == -1 ? 0 : src.getQuantity();
        int tgtQty = tgt.getQuantity() == -1 ? 0 : tgt.getQuantity();
        int newQty = tgtQty + srcQty;
        int minQty = tgt.getProductvariantid().getProductid().getMinimumOrderQuantity();
        if(minQty > 0 && newQty > minQty) {
            newQty = minQty;
        }
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Adding qty {0} from {1} to qty {2} in {3}",
new Object[]{ srcQty,  src,  tgtQty,  tgt});
}

        tgt.setQuantity(newQty);
        
if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "Updated quantity in {0} from {1} to {1}",
new Object[]{ tgt,  tgtQty,  newQty});
}
        
        return newQty;
    }
    
    public Orderproduct copy(Orderproduct orderproduct, boolean cascade) {
        Orderproduct copy = new Orderproduct();
        sync(orderproduct, copy, cascade);
        return copy;
    }
    
    public void sync(Orderproduct update, Orderproduct toUpdate, boolean cascade) {
        
        if(update.getOrderproductid() != null && 
                toUpdate.getOrderproductid() != null && 
                update.equals(toUpdate)) {
            throw new UnsupportedOperationException("Trying to sync a "+update.getClass().getName()+" with itself");
        }
        
        if(toUpdate.getCurrencyid() == null) {
            toUpdate.setCurrencyid(update.getCurrencyid());
        }
        if(toUpdate.getDatecreated() == null) {
            toUpdate.setDatecreated(update.getDatecreated());
        }
        if(toUpdate.getDiscount() == null) {
            toUpdate.setDiscount(update.getDiscount());
        }
        if(toUpdate.getOrderproductid() == null) {
            toUpdate.setOrderproductid(update.getOrderproductid());
        }
        if(toUpdate.getPrice() == null) {
            toUpdate.setPrice(update.getPrice());
        }
        if(cascade && toUpdate.getProductorderid() == null) { 
            toUpdate.setProductorderid(update.getProductorderid());
        }
        if(toUpdate.getProductvariantid() == null) {
            toUpdate.setProductvariantid(update.getProductvariantid());
        }
        if(toUpdate.getQuantity() < 1) {
            toUpdate.setQuantity(update.getQuantity());
        }
//        if(toUpdate.getTimemodified() == null) {
//            toUpdate.setTimemodified(update.getTimemodified());
//        }
    }

    public void format(List<Orderproduct> orderproducts, Productorder toUpdate) {
        
        for(Orderproduct op:orderproducts) {

            op.setProductorderid(toUpdate);

            if(op.getDatecreated() == null) {
                op.setDatecreated(new Date());
            }
        }
    }
    
    public List<Orderproduct> getItemsNotInDatabase(List<Orderproduct> orderproducts) {
        
        List<Orderproduct> notInDatabase = null;

        for(Orderproduct op:orderproducts) {

            if(op.getOrderproductid() == null) {
                if(notInDatabase == null) {
                    notInDatabase = new ArrayList<>(orderproducts.size());
                }
                notInDatabase.add(op);
            }
        }
        
        return notInDatabase;
    }

    public Productorder syncOrderWithDatabase(Productorder order) 
            throws PreexistingEntityException, NonexistentEntityException, Exception {

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Syncing with database, order: {0}", order);
}

        JpaContext cf = this.getJpaContext();

        EntityManager em = cf.getEntityManager(Productorder.class);
        
        Integer oid = order.getProductorderid();
        
        Productorder found;
        if(oid != null) {
            found = em.find(Productorder.class, oid);
        }else{
            found = null;
        }

if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Sync order with database, found in database: {0}", found);
}
        
        Productorder syncd;
        try{
            
            EntityTransaction t = em.getTransaction();
            
            try{
                
                t.begin();
                
                if(found != null) {
                    syncd = em.merge(order);
                }else{
                    em.persist(order);
                    syncd = order;
                }
                
                t.commit();
                
            }finally{
                if(t.isActive()) {
                    t.rollback();
                }
            }
        }finally{
            em.close();
        }
        
        return syncd;
    }
    
    public void persist(List<Orderproduct> orderproducts) {
        
        EntityManager em = this.getJpaContext().getEntityManager(Orderproduct.class);

        try{

            EntityTransaction t = em.getTransaction();

            try{

                t.begin();

                for(Orderproduct op:orderproducts) {

                    em.persist(op);
                }

                t.commit();

            }finally{
                if(t.isActive()) {
                    t.rollback();
                }
            }
        }finally{
            em.close();
        }
    }
    
    public long countOrderproducts(Integer orderId) {
        JpaContext cf = LbApp.getInstance().getJpaContext();
        EntityController<Orderproduct, ?> ec = cf.getEntityController(Orderproduct.class);
        Map where = Collections.singletonMap(Orderproduct_.productorderid.getName(), orderId);
        return ec.count(where);
    }

    public long countProductunits(Integer orderId) {
        
        JpaContext jpaContext = LbApp.getInstance().getJpaContext();

        // SELECT SUM(column_to_sum) FROM table WHERE column = value
        try(Select<Number> qb = jpaContext.getDaoForSelect(Orderproduct.class, Number.class)) {
            Number sum = qb.from(Orderproduct.class)
            .sum(Orderproduct_.quantity.getName())
            .where(Orderproduct_.productorderid.getName(), orderId)
            .createQuery().getSingleResult();
            return sum == null ? 0L : sum.longValue();
        }catch(javax.persistence.NoResultException e) {
            return 0L;
        }
    }
}
/**
 * 
    private void update(boolean edit, EntityController ec, Productorder toUpdate) 
            throws NonexistentEntityException, Exception {
        if(edit) {
            ec.edit(toUpdate);
        }else{
            ec.create(toUpdate);
        }
    }

    private void update1(boolean edit, EntityController ec, Productorder toUpdate) {
        
        this.update1(edit, ec, toUpdate, null, null);
    }
    
    private void update1(boolean edit, EntityController ec, Productorder toUpdate,
            List<Orderproduct> toEdit, List<Orderproduct> toInsert) {
        
        this.updateOrder(edit, ec, toUpdate);
        
        if((toEdit == null || toEdit.isEmpty()) &&
                (toInsert == null || toInsert.isEmpty())) {
            
            return;
        }

////////////////////////////////////////////////////////
// This other logic didn't work --- needs more research
///////////////////////////////////////////////////////
        EntityManager em = ec.getEntityManager();
        
        try{
            
            EntityTransaction t = em.getTransaction();
            
            try{
                
                t.begin();
                
                if(toEdit != null && !toEdit.isEmpty()) {

XLogger.getInstance().log(Level.FINE, "Editing {0} entities", this.getClass(), toEdit.size());

                    for(Orderproduct op:toEdit) {
                        em.merge(op);
                    }
                }

                if(toInsert != null && !toInsert.isEmpty()) {

XLogger.getInstance().log(Level.FINE, "Inserting {0} entities", this.getClass(), toInsert.size());

                    for(Orderproduct op:toInsert) {
                        em.persist(op);
                    }
                }
                
                
                t.commit();
                
            }finally{
                if(t.isActive()) {
                    t.rollback();
                }
            }
            
        }finally{
            em.close();
        }
    }
    
    private void updateOrder(boolean edit, EntityController ec, Productorder toUpdate) {
        
        EntityManager em = ec.getEntityManager();
        
        try{

            EntityTransaction t = em.getTransaction();

            try{

                t.begin();

                if(edit) {
                    em.merge(toUpdate);
                }else{
                    em.persist(toUpdate);
                }

                t.commit();

            }finally{
                if(t.isActive()) {
                    t.rollback();
                }
            }
        }finally{
            em.close();
        }
    }
    
    private void syncOrderproducts(Productorder update, Productorder toUpdate, 
            List<Orderproduct> appendAdded, List<Orderproduct> appendUpdated) {
        
        List<Orderproduct> itemsToUpdate = toUpdate.getOrderproductList();
        
        List<Orderproduct> itemsUpdate = update.getOrderproductList();
        
XLogger.getInstance().log(Level.FINER, "Adding {0} items to {1} existing", this.getClass(),
(itemsUpdate == null ? null : itemsUpdate.size()), (itemsToUpdate == null ? null : itemsToUpdate.size()));
        
        
        if(itemsUpdate != null && !itemsUpdate.isEmpty()) {

            if(itemsToUpdate == null) {
                itemsToUpdate = new ArrayList<>();
                toUpdate.setOrderproductList(itemsToUpdate);
            }

            for(Orderproduct itemUpdate:itemsUpdate) {
                
                Orderproduct selectedItemToUpdate = null;
                
                for(Orderproduct itemToUpdate:itemsToUpdate) {
                    
                    if(itemUpdate.getProductvariantid().equals(itemToUpdate.getProductvariantid())) {
                        
                        selectedItemToUpdate = itemToUpdate;
                        
                        int newQty = itemToUpdate.getQuantity() + itemUpdate.getQuantity();
                        int minQty = itemToUpdate.getProductvariantid().getProductid().getMinimumOrderQuantity();
                        if(minQty > 0 && newQty > minQty) {
                            newQty = minQty;
                        }
                        itemToUpdate.setQuantity(newQty);
                        break;
                    }
                }
                
                if(selectedItemToUpdate != null) {
                    if(appendUpdated != null) {
                        appendUpdated.add(selectedItemToUpdate);
                    }
                }else{
                    
                    Orderproduct copy = this.copy(itemUpdate, true);
                    
                    copy.setProductorderid(toUpdate);
                    
                    if(copy.getDatecreated() == null) {
                        copy.setDatecreated(new Date());
                    }
                    
                    if(appendAdded != null) {
                        appendAdded.add(copy);
                    }
                    itemsToUpdate.add(copy);
                }
            }
        }
    }
    private void syncOrderproducts(Productorder update, Productorder toUpdate) {
        
        List<Orderproduct> itemsUpdate = update.getOrderproductList();
        
        List<Orderproduct> itemsToUpdate = toUpdate.getOrderproductList();

XLogger.getInstance().log(Level.FINE, "Adding {0} items to {1} existing", this.getClass(),
(itemsUpdate == null ? null : itemsUpdate.size()), (itemsToUpdate == null ? null : itemsToUpdate.size()));

XLogger.getInstance().log(Level.FINER, "Items update: {0}\nItems to update: {1}", this.getClass(), itemsUpdate, itemsToUpdate);        
        
        Map<Integer, Orderproduct> merged = new HashMap<>();
        
// This must come first        
        this.mergeVariants(merged, itemsToUpdate, toUpdate, false);
        
// this must come second        
        this.mergeVariants(merged, itemsUpdate, toUpdate, true);

XLogger.getInstance().log(Level.FINE, "After merging items: {0}", this.getClass(), merged.size());

        ArrayList<Orderproduct> updated;
        
        if(!merged.isEmpty()) {
            
            updated = new ArrayList<>();
            
            updated.addAll(merged.values());
            
            this.format(updated, toUpdate);
            
            List<Orderproduct> notInDatabase = this.getItemsNotInDatabase(updated);

            if(notInDatabase != null && !notInDatabase.isEmpty()) {

                this.persist(notInDatabase);
            }
        }else{
            
            updated = null;
        }
        
        toUpdate.setOrderproductList(updated);
    }
    
    public int trimQuantities_old(Productorder order, boolean syncWithDatabase) {
        
XLogger.getInstance().log(Level.INFO, "BEFORE TRIM QUANTITIES, itemcount: {0}, order: {1}", 
        this.getClass(), this.getItemCount(order), order);
        
        List<Orderproduct> orderproducts = order.getOrderproductList();
        
        int removedOrderproducts = 0;
        
        int removedVariants = 0;
        
        List<Orderproduct> toupdate = null;

        Iterator<Orderproduct> iter = orderproducts.iterator();

        while(iter.hasNext()) {

            Orderproduct orderproduct = iter.next();

            int ordered = orderproduct.getQuantity();

            if(ordered < 1) {
                iter.remove();
XLogger.getInstance().log(Level.INFO, "Removed orderitem: {0} with productvariant ID: {1}", this.getClass(), orderproduct, orderproduct.getProductvariantid().getProductvariantid());
                ++removedOrderproducts;
                continue;
            }

            Productvariant unit = orderproduct.getProductvariantid();

            int instock = unit.getQuantityInStock();

            if(instock < 1) {
                iter.remove();
XLogger.getInstance().log(Level.INFO, "Removed orderitem: {0} with productvariant ID: {1}", this.getClass(), orderproduct, orderproduct.getProductvariantid().getProductvariantid());
                ++removedOrderproducts;
                continue;
            }

XLogger.getInstance().log(Level.FINER, "Order product ID: {0}, instock: {1}, ordered: {2}", 
this.getClass(), orderproduct.getOrderproductid(), instock, ordered);

            if(ordered > instock) {

XLogger.getInstance().log(Level.INFO, "Updated order quantity from {0} to {1} for {2}, {3}", 
this.getClass(), orderproduct.getQuantity(), instock, orderproduct, orderproduct.getProductvariantid().getProductvariantid());
                
                orderproduct.setQuantity(instock);
                
                removedVariants += (ordered - instock);

                if(syncWithDatabase) {
                    
                    if(toupdate == null) {
                        toupdate = new ArrayList<>(orderproducts.size());
                    }

                    toupdate.add(orderproduct);
                }
            }
        }

        if(syncWithDatabase && (removedOrderproducts > 0 || removedVariants > 0)) {
            
            ControllerFactory cf = this.getControllerFactory();
            
            String databaseName = cf.getMetaData().getDatabaseName(Productorder.class);

            EntityManager em = cf.getEntityManager(databaseName);

            try{

                EntityTransaction t = em.getTransaction();

                try{

                    t.begin();

                    if(toupdate != null) {
                        for(Orderproduct orderproduct:toupdate) {
                            em.merge(orderproduct);
                        }
                    }
                    
                    em.merge(order);

                    t.commit();

                }finally{
                    if(t.isActive()) {
                        t.rollback();
                    }
                }
            }finally{

                em.close();
            }
        }

XLogger.getInstance().log(Level.INFO, "AFTER TRIM QUANTITIES, itemcount: {0}, order: {1}", 
        this.getClass(), this.getItemCount(order), order);
        
        return removedVariants;
    }

    private void mergeVariants(
            Map<Integer, Orderproduct> merged, 
            List<Orderproduct> orderproducts, 
            Productorder tgt, boolean createCopy) {
        
        if(orderproducts == null) {
            return;
        }
        
        if(tgt == null) {
            throw new NullPointerException();
        }
        
        for(Orderproduct op:orderproducts) {

            Integer variantId = op.getProductvariantid().getProductvariantid();

XLogger.getInstance().log(Level.FINER, "Processing {0} with productvariant ID: {1}", 
this.getClass(), op, variantId);
            
            if(!merged.containsKey(variantId)) {

                Orderproduct toAdd;
                
                if(createCopy) {
                    
                    toAdd = this.copy(op, true);

                    toAdd.setProductorderid(tgt);

                    if(toAdd.getDatecreated() == null) {
                        toAdd.setDatecreated(new Date());
                    }
                }else{
                    
                    toAdd = op;
                }

XLogger.getInstance().log(Level.FINER, "Adding {0} with productvariant ID: {1} to merged {2}s", 
this.getClass(), toAdd, variantId, Orderproduct.class.getName());

                merged.put(variantId, toAdd);

            }else{
                
                Orderproduct saved = merged.get(variantId);
                
                this.mergeQuantities(op, saved);
            }
        }
    }
    
 * 
 */