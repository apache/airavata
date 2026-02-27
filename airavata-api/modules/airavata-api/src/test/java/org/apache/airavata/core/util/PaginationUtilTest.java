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
package org.apache.airavata.core.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link PaginationUtil}.
 */
class PaginationUtilTest {

    @Test
    void toPageRequest_standardLimitAndOffset() {
        Pageable pageable = PaginationUtil.toPageRequest(10, 0);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void toPageRequest_offsetDivisibleByLimit() {
        Pageable pageable = PaginationUtil.toPageRequest(10, 20);
        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void toPageRequest_offsetNotDivisibleByLimit() {
        Pageable pageable = PaginationUtil.toPageRequest(10, 15);
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void toPageRequest_zeroLimit_clampsToOne() {
        Pageable pageable = PaginationUtil.toPageRequest(0, 0);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void toPageRequest_negativeLimit_clampsToOne() {
        Pageable pageable = PaginationUtil.toPageRequest(-5, 0);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(1, pageable.getPageSize());
    }

    @Test
    void toPageRequest_negativeOffset_handledGracefully() {
        // Negative offset divided by positive limit gives negative page number in integer math
        // Spring PageRequest clamps or handles this, but the utility should not throw
        assertDoesNotThrow(() -> PaginationUtil.toPageRequest(10, -1));
    }

    @Test
    void toPageRequest_largeOffset() {
        Pageable pageable = PaginationUtil.toPageRequest(25, 100);
        assertEquals(4, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
    }
}
