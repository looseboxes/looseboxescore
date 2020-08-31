package com.looseboxes.core;

import com.bc.util.QueryParametersConverter;
import com.looseboxes.pu.entities.Address;
import com.looseboxes.pu.entities.Address_;
import com.looseboxes.pu.entities.Country;
import com.looseboxes.pu.entities.Country_;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.Select;

/**
 * @(#)Util.java   27-Apr-2015 13:47:10
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
public class Util {
    private transient static final Logger LOG = Logger.getLogger(Util.class.getName());
    
    public static boolean isContainsAtLeastOneAddressDetail(Map params) {
        // Country is often filled by default se we comment out
        boolean output = //params.get(Address_.countryid.getName()) != null |
                (params.get(Address_.regionid.getName()) != null |
                params.get(Address_.city.getName()) != null |
                params.get(Address_.county.getName()) != null) |
                params.get(Address_.streetAddress.getName()) != null;
        return output;
    }
    
    public static boolean isContainsAddressDetails(Map params) {
        boolean output = params.get(Address_.countryid.getName()) != null &&
                (params.get(Address_.regionid.getName()) != null |
                params.get(Address_.city.getName()) != null |
                params.get(Address_.county.getName()) != null) &&
                params.get(Address_.streetAddress.getName()) != null;
        return output;
    }
    
    public static com.looseboxes.pu.entities.Currency getCurrencyEntityForLocale(Locale locale) {
        
        com.looseboxes.pu.entities.Currency output;
        
        JpaContext jpaContext = LbApp.getInstance().getJpaContext();
        
        Currency curr = Currency.getInstance(locale);

        if(curr != null) {
            try(Select<com.looseboxes.pu.entities.Currency> qb = jpaContext.getDaoForSelect(com.looseboxes.pu.entities.Currency.class)) {
                output = qb.from(com.looseboxes.pu.entities.Currency.class)
                .where(com.looseboxes.pu.entities.Currency_.currency.getName(), curr.getCurrencyCode())
                .createQuery().getSingleResult();
            }catch(javax.persistence.NoResultException noNeedToLog) {
                output = null;
            }
        }else{
            output = null;
        }
        
        if(output == null) {
            

            List<Number> countryIds; 
            try{
                final String localeIso3Ctry = locale.getISO3Country();
// E.g.  Caused by: java.util.MissingResourceException: Couldn't find 3-letter country code for CS                    
                try(Select<Number> qb = jpaContext.getDaoForSelect(Country.class, Number.class)) {

                    countryIds = qb.from(Country.class)
                        .select(Country_.countryid.getName())
                        .where(Country.class, Country_.isocode3.getName(), localeIso3Ctry)
                        .or().where(Country_.country.getName(), localeIso3Ctry)
                        .createQuery().getResultList();
                }
            }catch(java.util.MissingResourceException e) {
                countryIds = null;
                if(LOG.isLoggable(Level.WARNING)){
                        LOG.log(Level.WARNING, "{0}", e.toString());
                }
            }

            if(countryIds != null && !countryIds.isEmpty()) {
            
                // If countryid = 102 then currencyid for country = 102
                //
                List<com.looseboxes.pu.entities.Currency> found;
                try(Select<com.looseboxes.pu.entities.Currency> qb = jpaContext.getDaoForSelect(com.looseboxes.pu.entities.Currency.class)) {
                    found = qb.from(com.looseboxes.pu.entities.Currency.class)
                    .where(com.looseboxes.pu.entities.Currency_.currencyid.getName(), countryIds)
                    .createQuery().getResultList();
                }

                if(found == null || found.isEmpty()) {
                    output = null;
                }else if(found.size() == 1) {
                    output = found.get(0);
                }else{

                    output = found.get(0);
                }
            }else{
                output = null;
            }
        }    
        return output;
    }
    
    public static Locale getLocaleForCurrencySymbol(String symbol, Locale defaultLocale) {
        Locale output = defaultLocale;
        if(symbol != null) {
            Locale [] locales = Locale.getAvailableLocales();
            for(Locale loc:locales) {
                java.util.Currency locCurr;
                try{
                    locCurr = Currency.getInstance(loc);
                }catch(IllegalArgumentException ignored) { 
                    locCurr = null;
                }
                if(locCurr != null && symbol.equals(locCurr.getSymbol())) {
                    output = loc;
                    break;
                }
            }
        }
        return output;
    }
    
    public static Locale getLocaleForCurrencyCode(String code, Locale defaultLocale) {
        Locale output = defaultLocale;
        if(code != null) {
            Locale [] locales = Locale.getAvailableLocales();
            for(Locale loc:locales) {
                java.util.Currency locCurr;
                try{
                    locCurr = Currency.getInstance(loc);
                }catch(IllegalArgumentException ignored) { 
                    locCurr = null;
                }
                if(locCurr != null && code.equals(locCurr.getCurrencyCode())) {
                    output = loc;
                    break;
                }
            }
        }
        return output;
    }
    
    public static Locale getLocale(Address address, Locale defaultLocale) {
        Locale output;
        if(address != null) {
            output = getLocale(address.getCountryid(), defaultLocale);
        }else{
            output = defaultLocale;
        }
        return output;
    }
    
    public static Locale getLocale(Country country, Locale defaultLocale) {
        Locale output = defaultLocale;
        if(country != null) {
            String iso3countryCode = country.getIsocode3();
            Locale [] locales = Locale.getAvailableLocales();
            for(Locale loc:locales) {
                try{
                    if(iso3countryCode.equals(loc.getISO3Country()) ||
                            iso3countryCode.equals(loc.getCountry())) {
                        output = loc;
                        break;
                    }
// E.g.  Caused by: java.util.MissingResourceException: Couldn't find 3-letter country code for CS                    
                }catch(java.util.MissingResourceException e) {
                    if(LOG.isLoggable(Level.WARNING)){
                              LOG.log(Level.WARNING, "{0}", e.toString());
                    }
                }
            }
        }
        return output;
    }
    
    public static boolean contains(Object toFind, boolean ignoreCase, Object ...candidates) {
        boolean output = false;
        for(Object candidate:candidates) {
            boolean matchFound;
            if(toFind == null && candidate == null) {
                matchFound = true;
            }else if(toFind == null && candidate != null) {
                matchFound = false;
            }else if(toFind != null && candidate == null) {
                matchFound = false;
            }else{
                if(ignoreCase) {
                    matchFound = candidate.toString().equalsIgnoreCase(toFind.toString());
                }else{
                    matchFound = candidate.equals(toFind);
                }
            }
            if(matchFound) {
                output = matchFound;
                break;
            }
        }
        return output;
    }

    public static String get(String [] arr, boolean shortest) {
        
        if(!shortest) {

            return arr[0];

        }else{

            String shortestString = null;
            for(String up:arr) {
                if(shortestString == null || up.length() < shortestString.length()) {
                    shortestString = up;
                }
            }
            
            if(shortestString == null) {
                throw new NullPointerException();
            }

            return shortestString;
        }
    }

    public static boolean isHttpUrl(String link) {
        return link.toLowerCase().trim().startsWith("http:");
    }
    
    /**
     * Use the java 7 {@link java.nio.file.Files Files} class
     * @deprecated
     */
    @Deprecated
    public static long stream(InputStream input, OutputStream output) throws IOException {

        ReadableByteChannel inputChannel = null;
        WritableByteChannel outputChannel = null;

        try {
            inputChannel = Channels.newChannel(input);
            outputChannel = Channels.newChannel(output);
            ByteBuffer buffer = ByteBuffer.allocate(10240);
            long size = 0;

            while (inputChannel.read(buffer) != -1) {
                buffer.flip();
                size += outputChannel.write(buffer);
                buffer.clear();
            }

            return size;
        }
        finally {
            if (outputChannel != null) try { outputChannel.close(); } catch (IOException ignore) { /**/ }
            if (inputChannel != null) try { inputChannel.close(); } catch (IOException ignore) { /**/ }
        }
    }    
    
    /**
     * The First element of the array is equal to the input value. 
     * If an encoding is specified, then comes the URL decoded input value
     * Then comes further variants...<br/><br/>
     * Only variants[0] is guaranteed not to be null. variants[1] and 
     * greater may be null.
     */
    public static String [] getVariants(String value, String encoding) {
        
        String [] variants = new String[3];
        
        variants[0] = value;
        
        try{
            variants[1] = URLDecoder.decode(value, encoding);
        }catch(UnsupportedEncodingException e) {
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, null, e);
            }
        }catch(RuntimeException e) {
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, null, e);
            }
        }

        if(variants[1] != null) {
            variants[2] = variants[1].replace(' ', '+');
        }
        
        return variants;
    }

    public static String createFilename(String input) {
        return createFilename(input, 255, '_');
    }
    
    public static String createFilename(String input, int maxLen, char separator) {
        if(input == null || input.isEmpty()) {
            return "";
        }
        if(input.length() > maxLen) {
            input = input.substring(0, maxLen);
        }
        final int len = input.length();
        StringBuilder output = new StringBuilder();
        boolean mayAppendSeparator = false;
        for(int i=0; i<len; i++) {
            char ch = input.charAt(i);
            if(Character.isLetterOrDigit(ch)) {
                output.append(ch);
                mayAppendSeparator = true;
            }else{
                if(mayAppendSeparator && i < len-1) {
                    output.append(separator);
                    mayAppendSeparator = false;
                }
            }
        }
        if(output.length() > 0) {
            if(output.charAt(output.length()-1) == separator) {
                output.setLength(output.length()-1);
            }
        }
        return output.toString();
    }
    
    /**
     * Convert <tt>http://www.aboki.com/feeds/rss.xml</tt> to <tt>aboki_feeds_rss</tt>
     * @param path
     * @return 
     */
    public static String createIdForPath(String path) {
        int n = path.indexOf("://");
        if(n != -1) {
            path = path.substring(n+3);
        }
        path = path.replace("www.", "");
        path = path.replace("\\", "/");
        int start = path.indexOf('/')+1;
        if(start == -1) {
            start = 0;
        }
// This did not work as lastIndexOf search backwards, see method documentation
//        int end = sval.lastIndexOf('.', start);
        int end = path.indexOf('.', start);
        if(end == -1) {
            end = path.length();
        }
        path = path.substring(start, end);
        path = path.replace('/', '_');
        return path;
    }

    public static InputStream getInputStream(String path) throws IOException {
        try{
            URL url = new URL(path);
            return url.openStream();
        }catch(MalformedURLException e) {
            return new FileInputStream(path);
        }
    }
    
    public static String [] getSearchTerms(String searchText) {
        
        if(searchText == null || searchText.isEmpty()) {
            throw new NullPointerException("You did not specify any word or phrase"); 
        }
        
        String [] parts = searchText.split("\\s");
        
        String [] output = null;

        if(parts == null || parts.length < 2) {
            
            output = new String[]{searchText};
            
        }else{
        
            output = new String [parts.length + 1];

            output[0] = searchText;

            System.arraycopy(parts, 0, output, 1, parts.length);
        }
        
        return output;
    }
    
    /**
     * Crude logic to convert words to their singular form.
     * For words ending with <tt>y</tt>, 'y' is replaced with 'ies'. 
     * For words ending with <tt>s</tt>, 's' is appended to the end of the word
     * For all other words, 's' is append to the end of the word.
     * @author Josh
     */
    public static String singular(String text) {
        
        String output = text.trim();

        final int len = output.length();

        final int last = len - 1;
//Logger.getLogger(this.getClass().getName()).info(this.getClass().getName()+". BEFORE: "+text);
        if(output.lastIndexOf("ies") == last) {
            output = output.substring(0, last) + "y";
        }else if(output.lastIndexOf("IES") == last) {
            output = output.substring(0, last) + "Y";
        }else if(output.lastIndexOf("es") == last){
            output = output.substring(0, len-2);
        }else if(output.lastIndexOf("ES") == last){
            output = output.substring(0, len-2);
        }else{
            output = output.substring(0, len-1);
        }

//Logger.getLogger(this.getClass().getName()).info(this.getClass().getName()+". AFTER: "+output);
        return output;
    }
    
    /**
     * Crude logic to convert words to their plural form
     * For words ending with <tt>y</tt>, 'y' is replaced with 'ies'. 
     * For words ending with <tt>s</tt>, 'es' is append to the end of the word.
     * For all other words, 's' is append to the end of the word.
     * @author Josh
     */
    public static String plural(String input) {
        
        String output = input.trim();
        
        final int len = output.length();

        final int last = len - 1;
//Logger.getLogger(this.getClass().getName()).info(this.getClass().getName()+". BEFORE: "+input);
        if(output.lastIndexOf('y') == last) {
            output = output.substring(0, last) + "ies";
        }else if(output.lastIndexOf('Y') == last) {
            output = output.substring(0, last) + "IES";
        }else if(output.lastIndexOf('s') == last){
            output = output + "es";
        }else if(output.lastIndexOf('S') == last){
            output = output + "ES";
        }else{
            if(Character.isLowerCase(output.charAt(last))) {
                output = output + "s";
            }else{
                output = output = "S";
            }
        }
//Logger.getLogger(this.getClass().getName()).info(this.getClass().getName()+". AFTER: "+output);
        return output;
    }

    /**
     * @param pathStr The path to the file to be deleted
     * @return true if the file was deleted, false if the file was not deleted 
     * or does not exist
     */
    public static boolean deleteFile(String pathStr) throws NullPointerException {
        
        if(pathStr == null) throw new NullPointerException();
        
        try{
            Path path = Paths.get(pathStr);
            if(Files.exists(path, (LinkOption)null)) {
                Files.delete(path);
                return true;
            }else{
                return false;
            }
        }catch(Exception e) {
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, "Failed to delete file", e);
            }
            return false;
        }
    }

    /**
     * Uses the default math context
     */
    public static BigDecimal multiply(MathContext context, double a, double b) {
        
        BigDecimal output = BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(b), context);
//Logger.getLogger(Util.class.getName()).fine(a + " multiply " + b + " = " + output);
        return output;
    }

    /**
     * Uses the default math context
     */
    public static BigDecimal divide(MathContext context, double a, double b) {
        BigDecimal output = BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), context);
//Logger.getLogger(Util.class.getName()).fine(a + " divide " + b + " = " + output);
        return output;
    }

    /**
     * @param contentType 'text/plain' or 'text/xml', default is 'text/plain'
     * @param messages
     * @return 
     */
    public static String toString(String contentType, Collection<String> messages) {

        if(messages.isEmpty()) return "";

        final boolean XML = contentType != null && contentType.endsWith("xml");
        
        final String LB = XML ? "<br/>" : "\n";
        
        StringBuilder builder = new StringBuilder();
        
        Iterator<String> iter = messages.iterator();
        while(iter.hasNext()) {
            String message = iter.next();
            if(message == null || message.isEmpty()) continue;
            builder.append(message);
            if(iter.hasNext()) {
                builder.append(LB);
            }
        }

        return builder.toString();
    }

    /**
     * @param params Contains key-value pairs to be used in generating a query String
     * @return Query of form <tt>key_1=val_1&key_2=val_2&key_3=val_3
     * @throws NullPointerException if the StringBuilder appendTo is null
     */
    public static void appendQuery(Map<String, Object> params, StringBuilder appendTo) {
        appendQuery(params, appendTo, "utf-8");
    }
    
    /**
     * @param params Contains key-value pairs to be used in generating a query String
     * @param appendTo The StringBuilder to append the query created to
     * @param charset If a value is provided an attempt is made to encode the query values using this charset
     * @return Query of form <tt>key_1=val_1&key_2=val_2&key_3=val_3
     * @throws NullPointerException if the StringBuilder appendTo is null
     */
    public static void appendQuery(Map<String, Object> params, 
            StringBuilder appendTo, final String charset) {
        final QueryParametersConverter qpc = new QueryParametersConverter();
        appendTo.append(qpc.convert(params, true, charset));
    }
    
    /**
     * Expected <tt>arg0</tt> format:
     * <tt>'key=value{x}key1=value1{x}key2=value2'</tt><br/><br/>
     * Where {x} referers to the separator used between key-value pairs<br/>
     * For the above input this method will return a Map with the key-value
     * pairs contained in the <tt>arg0</tt> input.
     * @param String
     * @param separator
     * @return
     */
    public static Map<String, String> getParameters(
            String input, String separator) {
        return getParameters(input, separator, false);
    }
    
    public static Map<String, String> getParameters(
            String input, final String separator, final boolean nullsAllowed) {
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Separator: {0}, Nulls allowed: {1}, Query: {2}", 
new Object[]{ separator,  nullsAllowed,  input});
} 

        QueryParametersConverter qpc = new QueryParametersConverter(nullsAllowed, nullsAllowed, separator);
        
        Map<String, String> result = qpc.reverse(input);
        
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Output: {0}", result);
}        

        return result;
    }

    public static String getStackTrace(Throwable t) {
        
        StringBuffer stackTrace = null;
        
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);

            t.printStackTrace(pw);

            stackTrace = sw.getBuffer();

            if(stackTrace.length() == 0) {
                stackTrace.append(t);
            }

        }catch(Exception ex) {

            Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", ex);
        }finally{

            if(pw != null) pw.close();

            if(sw != null) try { sw.close(); }catch(IOException e) {
                Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", e);
            }
        }
        return stackTrace.toString();
    }
}
