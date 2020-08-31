package com.looseboxes.core;

import com.looseboxes.pu.entities.Country;
import java.math.MathContext;
import java.util.Locale;
import java.util.TimeZone;


/**
 * @(#)Defaults.java   27-Apr-2015 08:27:13
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
public interface Defaults {

    TimeZone getDefaultTimeZone();
    
    Locale getDefaultLocale();
    
    Country getDefaultCountry();
    
    java.util.Currency getDefaultCurrency();
    
    MathContext getDefaultMathContext();
}
