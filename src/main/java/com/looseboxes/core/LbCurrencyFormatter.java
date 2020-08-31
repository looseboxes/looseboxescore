package com.looseboxes.core;

import com.bc.fxrateservice.FxRate;
import com.bc.fxrateservice.FxRateService;
import com.bc.fxrateservice.impl.DefaultFxRateService;
import com.bc.util.CurrencyFormatter;
import java.util.Locale;


/**
 * @(#)LbCurrencyFormatter.java   20-Aug-2015 20:19:53
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
public class LbCurrencyFormatter extends CurrencyFormatter {

    private transient static final FxRateService fxRateService = new DefaultFxRateService();
    
    @Override
    public float getRate(Locale from, Locale to) {
        
        final FxRate fxRate = fxRateService.getRate(from, to);
        
        return fxRate.getRateOrDefault(-1.0f);
    }
}
