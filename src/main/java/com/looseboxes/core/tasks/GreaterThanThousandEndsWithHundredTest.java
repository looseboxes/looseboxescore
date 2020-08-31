/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.looseboxes.core.tasks;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 12, 2018 8:36:34 AM
 */
public class GreaterThanThousandEndsWithHundredTest implements Predicate<BigDecimal>, Serializable {

    private final BigDecimal thousand = new BigDecimal("1000");

    @Override
    public boolean test(BigDecimal result) {
        
        boolean success = false;
        
        if(result.doubleValue() > thousand.doubleValue()) {

            final BigDecimal rem = result.remainder(thousand);

//                System.out.println(result + ".remainder("+thou+") = " + rem);

            if(rem.doubleValue() == 100) {

                success = true;
            } 
        }
        
        return success;
    }
}
