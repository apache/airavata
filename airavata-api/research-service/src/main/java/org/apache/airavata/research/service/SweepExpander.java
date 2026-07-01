/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure cartesian-product expander for sweep axes. No Spring dependencies — trivially unit-testable.
 *
 * <p>{@link #expand} returns the cartesian product of all axis value lists, each combination merged
 * onto a copy of the base map (axis values override base keys with the same name). Iteration order
 * follows the insertion order of {@code axes} (matches Python's {@code itertools.product}).
 */
public class SweepExpander {

    private SweepExpander() {}

    /**
     * Expand {@code axes} into all combinations, each merged onto a copy of {@code base}.
     *
     * @param base  the baseline key-value map carried into every combination
     * @param axes  axis name → ordered list of values; must be a {@link java.util.LinkedHashMap}
     *              or any insertion-ordered map for deterministic product ordering
     * @return list of maps, one per combination (axis values override matching base keys)
     */
    public static List<Map<String, String>> expand(Map<String, String> base, Map<String, List<String>> axes) {
        if (axes.isEmpty()) {
            Map<String, String> copy = new HashMap<>(base);
            return List.of(copy);
        }

        // Convert axes to parallel arrays for indexed iteration
        List<String> axisNames = new ArrayList<>(axes.keySet());
        List<List<String>> axisValues = new ArrayList<>();
        for (String name : axisNames) {
            axisValues.add(axes.get(name));
        }

        // Iterative cartesian product: start with one empty combo, extend one axis at a time
        List<Map<String, String>> combos = new ArrayList<>();
        combos.add(new HashMap<>(base));

        for (int i = 0; i < axisNames.size(); i++) {
            String axisName = axisNames.get(i);
            List<String> values = axisValues.get(i);
            List<Map<String, String>> expanded = new ArrayList<>(combos.size() * values.size());
            for (Map<String, String> combo : combos) {
                for (String value : values) {
                    Map<String, String> next = new HashMap<>(combo);
                    next.put(axisName, value);
                    expanded.add(next);
                }
            }
            combos = expanded;
        }

        return combos;
    }
}
