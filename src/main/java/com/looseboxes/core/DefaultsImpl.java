package com.looseboxes.core;

import com.bc.jpa.fk.EnumReferences;
import com.looseboxes.pu.References;
import com.looseboxes.pu.entities.Country;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.TimeZone;
import com.bc.jpa.context.JpaContext;


/**
 * @(#)DefaultsImpl.java   27-Apr-2015 08:29:50
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
public class DefaultsImpl implements Defaults {

    @Override
    public TimeZone getDefaultTimeZone() {
        return TimeZone.getTimeZone("GMT+1:00");
    }

    @Override
    public Locale getDefaultLocale() {
        ////////////////////////////////////////////////////////////////////////
        // DO NOT CHANGE THIS 
        // Otherwise change all default values of currencies in the database 
        // which is set to match this i.e: 566 (for NGN)
        ////////////////////////////////////////////////////////////////////////
        return new Locale("en","NG","");
    }

    @Override
    public Country getDefaultCountry() {
        JpaContext cf = LbApp.getInstance().getJpaContext();
        EnumReferences refs = cf.getEnumReferences();
        return (Country)refs.getEntity(References.country.Nigeria);
    }

    @Override
    public Currency getDefaultCurrency() {
        return Currency.getInstance(this.getDefaultLocale());
    }

    @Override
    public MathContext getDefaultMathContext() {
        // Price in the database is Decimal(12, 4).
        // This is covered by a precision of 14.
        //
        return new MathContext(14, RoundingMode.HALF_UP);
    }
}
