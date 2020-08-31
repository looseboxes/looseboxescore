/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.looseboxes.actions;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 3:18:42 PM
 */
public class ImportPrices implements Callable<Map<Integer, BigDecimal>> {

    private transient static final Logger logger = Logger.getLogger(ImportPrices.class.getName());

    private final Callable<Path> pathSource;
    
    private final String charsetName;
    
    private final String separator;
    
    public ImportPrices(Path path) {
        this(new PathSupplier(path), "utf-8", ",");
    }

    public ImportPrices(Callable<Path> pathSource, String charsetName, String separator) {
        this.pathSource = Objects.requireNonNull(pathSource);
        this.charsetName = Objects.requireNonNull(charsetName);
        this.separator = Objects.requireNonNull(separator);
    }
    
    @Override
    public Map<Integer, BigDecimal> call() throws Exception {

        final Map<Integer, BigDecimal> output = new LinkedHashMap<>();
        
        final Path path = pathSource.call();
        
        final List<String> lines = Files.readAllLines(path, Charset.forName(charsetName));

        for(String line : lines) {
            line = line.trim();
            final int n = line.indexOf(separator);
            if(n == -1) {
                logger.log(Level.FINE, "Does not contain {0} line: {1}", new Object[]{separator, line});
                continue;
            }
            final String [] parts = line.split(Pattern.quote(","));
            if(parts.length != 2) {
                logger.log(Level.FINE, "Does not comprise of 2 parts separated by {0} line: {1}", new Object[]{separator, line});
                continue;
            }

            final Integer productId = Integer.parseInt(parts[0].trim());
            
            final BigDecimal price = BigDecimal.valueOf(Double.parseDouble(parts[1].trim()));

            output.put(productId, price);
        }
        
        return output;
    }
}
/**
 * 

            dao.from(Product.class)
                    .where(Product.class, Product_.productid.getName(), productId)
                    .set(Product_.price.getName(), price)
                    .and().set(Product_.discount.getName(), price)
                    .executeUpdate();
 * 
 */