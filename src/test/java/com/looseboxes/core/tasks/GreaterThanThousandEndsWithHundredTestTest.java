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

import java.math.BigDecimal;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class GreaterThanThousandEndsWithHundredTestTest {
    
    public GreaterThanThousandEndsWithHundredTestTest() {
    }
    
    /**
     * Test of test method, of class GreaterThanThousandEndsWithHundredTest.
     */
    @Test
    public void testTest() {
        System.out.println("test");

        GreaterThanThousandEndsWithHundredTest instance = new GreaterThanThousandEndsWithHundredTest();

        final double [] arr = {1200, 2100, 3100, 7110, 10101};
        
        for(int i=0; i<arr.length; i++) {
            
            final BigDecimal target = new BigDecimal(Double.toString(arr[i]));
            
            final boolean success = instance.test(target);
            
            System.out.println("Ends with 100: " + success + "\ttarget: " + target);
        }
    }
}
