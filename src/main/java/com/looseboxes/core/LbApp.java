package com.looseboxes.core;

import com.bc.mail.config.MailConfig;
import com.bc.jpa.dao.sql.MySQLDateTimePatterns;
import com.bc.util.concurrent.NamedThreadFactory;
import com.looseboxes.core.tasks.UpdateProductAvailability;
import com.looseboxes.pu.AbstractListings;
import com.looseboxes.pu.LbJpaContext;
import com.looseboxes.pu.Listings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.bc.jpa.context.JpaContext;
import java.net.URI;
import java.util.logging.Logger;


/**
 * @(#)LbApp.java   16-Apr-2015 00:38:28
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
public class LbApp {

    private static final Logger LOG = Logger.getLogger(LbApp.class.getName());
    
    private static LbApp instance;
    
    private MailConfig mailConfig;
    
    private JpaContext jpaContext;
    
    protected LbApp() { }
    
    public static LbApp getInstance() {
        if(instance == null) {
            instance = new LbApp();
        }
        return instance;
    }
    public static void setInstance(LbApp app) {
        instance = app;
    }
    
    public String getPackageLoggerName() {
        return LbApp.class.getPackage().getName();
    }
    
    public void init() throws IOException, URISyntaxException {
        
        this.init("META-INF/persistence.xml");
    }
    
    public void init(String persistenceFilename) throws IOException, URISyntaxException {
        
        Defaults defaults = this.getDefaults();
        Locale.setDefault(defaults.getDefaultLocale());

        TimeZone.setDefault(defaults.getDefaultTimeZone());
        
        LOG.log(Level.INFO, "Creating entity controller factory");

        if(persistenceFilename == null) {
            
            persistenceFilename = "META-INF/persistence.xml";
        }
        
        if("META-INF/persistence.xml".equals(persistenceFilename)) {

            this.jpaContext = new LbJpaContext(persistenceFilename, new MySQLDateTimePatterns());

        }else{
            
            try{
                this.jpaContext = new LbJpaContext(new URI(persistenceFilename), new MySQLDateTimePatterns());
            }catch(Exception e) {
                
                LOG.log(Level.SEVERE, null, e);
                
                throw new RuntimeException(e.toString());
            }
        }
        
        LOG.fine("Done creating JpaContext");
        
        this.initSchedules();
    }
    
    public String getPersistenceFilename() {
        return "META-INF/persistence.xml";
    }
    
    protected void initSchedules() {
        this.initHourlySchedules();
        this.initDailySchedules();
        LOG.fine("Done initializing schedules");
    }
    
    protected void initHourlySchedules() {
        UpdateProductAvailability updateAvailability = new UpdateProductAvailability(
                LbApp.getInstance().getJpaContext());
        final ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory(this.getClass().getName()+"_UpdateProductAvailability_ThreadPool"));
        svc.scheduleWithFixedDelay(updateAvailability, 1, 60, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                com.bc.util.Util.shutdownAndAwaitTermination(svc, 500, TimeUnit.MILLISECONDS);
            }
        });
    }
    
    protected void initDailySchedules() { }
    
    public Defaults getDefaults() {
        return new DefaultsImpl();
    }
    
    public boolean isProductionMode() {
        return false;
    }
    
    /**
     * @param fname The file name of the file whose URL will be returned
     * @return The URL of the file referenced by the input file name
     * @throws java.net.MalformedURLException
     */
    public URL getURL(String fname) throws MalformedURLException {
        return Paths.get(fname).toUri().toURL();
    }
    
    /**
     * An external url is a link used by the application and located outside 
     * the application's context. This implementation simply calls 
     * {@link #getURL(java.lang.String)}. Subclasses should therefore override 
     * this method to return the appropriate external URL.
     * @param fname The file name of the file whose URL will be returned
     * @return The URL referencing a file located outside this application's context
     * @throws java.net.MalformedURLException
     */
    public URL getExternalURL(String fname) throws MalformedURLException {
        return this.getURL(fname);
    }
    
    /**
     * @param fname The file name of the file whose path will be returned
     * @return The full path of the file referenced by the input file name
     */
    public String getPath(String fname) {
//@todo optimize for java 7        
        File file;
        URL url = Thread.currentThread().getContextClassLoader().getResource(fname);
        if(url != null) {
            try{
                file = Paths.get(url.toURI()).toFile();
            }catch(URISyntaxException e) {
                file = new File(fname);
            }
        }else{
            file = new File(fname);
        }
        return file.getAbsolutePath();
    }

    /**
     * An external path is a path used by the application and located outside 
     * the application's context. This implementation simply calls 
     * {@link #getPath(java.lang.String)}. Subclasses should therefore override 
     * this method to return the appropriate external path.
     * @param fname The file name of the file whose external path will be returned
     * @return The path used by this application and located outside this application's context
     */
    public String getExternalPath(String fname) {
        return this.getPath(fname);
    }
    
    private Map<Class, Listings> listings;
    public <T> Listings getListings(final Class<T> entityType) throws SQLException {
        if(listings == null) {
            listings = new WeakHashMap<>();
        }
        
        Listings output = listings.get(entityType);
        
        if(output == null) {
            output = new AbstractListings<T>() {
                @Override
                public Class<T> getEntityClass() {
                    return entityType;
                }
                @Override
                public JpaContext getControllerFactory() {
                    return LbApp.this.getJpaContext();
                }
            };
            
            // Caching listings like this may not capture most recent updates
            //
            listings.put(entityType, output);
        }
        return output;
    }
    
    public String getBaseURL() {
        return null;
    }

    public MailConfig getMailConfig() {
        if(mailConfig == null) {
            mailConfig = MailConfig.builder().build();
        }
        return mailConfig;
    }
    
    public JpaContext getJpaContext() {
        return jpaContext;
    }
}
