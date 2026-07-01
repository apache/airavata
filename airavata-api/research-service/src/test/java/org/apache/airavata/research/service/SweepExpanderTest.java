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

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SweepExpanderTest {

    @Test
    void expand_is_cartesian_product_overriding_base() {
        var base = Map.of("temp", "300", "app", "echo");
        var axes = new LinkedHashMap<String, List<String>>();
        axes.put("temp", List.of("300", "400"));
        axes.put("salt", List.of("lo", "hi"));
        var out = SweepExpander.expand(base, axes);
        assertEquals(4, out.size()); // 2 x 2
        assertEquals("echo", out.get(0).get("app")); // base carried
        assertTrue(out.stream().anyMatch(m -> m.get("temp").equals("400") && m.get("salt").equals("hi")));
        assertEquals(Set.of("300", "400"), out.stream().map(m -> m.get("temp")).collect(toSet())); // axis overrides base
    }

    @Test
    void expand_no_axes_returns_single_base_copy() {
        assertEquals(1, SweepExpander.expand(Map.of("a", "1"), Map.of()).size());
    }
}
