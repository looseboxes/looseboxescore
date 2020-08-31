/*
 * Copyright 2017 NUROX Ltd.
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

package com.looseboxes.actions;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 4:27:14 PM
 */
public class PathSupplier implements Callable<Path> {

    private final Path path;

    public PathSupplier(Path path) {
        this.path = Objects.requireNonNull(path);
    }
    
    @Override
    public Path call() {
        return path;
    }
}
