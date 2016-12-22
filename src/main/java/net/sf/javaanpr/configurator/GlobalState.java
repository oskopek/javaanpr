/*
 * Copyright 2016 JavaANPR contributors
 * Copyright 2006 Ondrej Martinsky
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package net.sf.javaanpr.configurator;

import java.io.IOException;

public final class GlobalState {

    private static final GlobalState INSTANCE = new GlobalState();
    private final Configurator configurator;

    private GlobalState() {
        try {
            configurator = Configurator.build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize global state.");
        }
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    public static GlobalState getInstance() {
        return INSTANCE;
    }
}
