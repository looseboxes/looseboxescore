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
public class RoundTest {
    
    private final int [] toNearests = {10, 50, 100};

    private final double [] inputs = {2300, 2001.98, 1070, 1205, 1369, 36.13};
    
    public RoundTest() { }
    
    /**
     * Test of down method, of class Round.
     */
    @Test
    public void testDown() {
        System.out.println("down");

        final Round instance = new Round();

        for(int toNearest : toNearests) {
            
            for(double input : inputs) {
        
                final int output = instance.down(new BigDecimal(Double.toString(input)).intValue(), toNearest);
                
                System.out.println("Round down to nearest:\t" + toNearest + ",\t" + input + "\t=\t" + output);
            }
        }
    }

    /**
     * Test of up method, of class Round.
     */
    @Test
    public void testUp() {
        System.out.println("up");

        final Round instance = new Round();

        for(int toNearest : toNearests) {
            
            for(double input : inputs) {
        
                final int output = instance.up(new BigDecimal(Double.toString(input)).intValue(), toNearest);
                
                System.out.println("Round up to nearest:\t" + toNearest + ",\t" + input + "\t=\t" + output);
            }
        }
    }
}
