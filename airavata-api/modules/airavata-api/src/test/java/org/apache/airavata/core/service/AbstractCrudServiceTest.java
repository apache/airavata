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
package org.apache.airavata.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.apache.airavata.core.mapper.EntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Unit tests for {@link AbstractCrudService} using a concrete test subclass.
 */
@ExtendWith(MockitoExtension.class)
class AbstractCrudServiceTest {

    @Mock
    private JpaRepository<TestEntity, String> repository;

    @Mock
    private EntityMapper<TestEntity, TestModel> mapper;

    private TestCrudService service;

    @BeforeEach
    void setUp() {
        service = new TestCrudService(repository, mapper);
    }

    // ========== create ==========

    @Test
    void create_withNoId_generatesIdAndSaves() {
        TestModel model = new TestModel(null, "data");
        TestEntity entity = new TestEntity("generated-id", "data");

        when(mapper.toEntity(any())).thenReturn(entity);

        String id = service.create(model);

        assertNotNull(id);
        assertFalse(id.isBlank());
        verify(repository).save(entity);
    }

    @Test
    void create_withExistingId_usesProvidedId() {
        TestModel model = new TestModel("my-id", "data");
        TestEntity entity = new TestEntity("my-id", "data");

        when(mapper.toEntity(any())).thenReturn(entity);

        String id = service.create(model);

        assertEquals("my-id", id);
        verify(repository).save(entity);
    }

    // ========== get ==========

    @Test
    void get_existingId_returnsModel() {
        TestEntity entity = new TestEntity("id-1", "data");
        TestModel model = new TestModel("id-1", "data");

        when(repository.findById("id-1")).thenReturn(Optional.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        TestModel result = service.get("id-1");

        assertNotNull(result);
        assertEquals("id-1", result.id);
        assertEquals("data", result.data);
    }

    @Test
    void get_nonExistentId_returnsNull() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        TestModel result = service.get("missing");

        assertNull(result);
    }

    // ========== update ==========

    @Test
    void update_existingId_savesUpdatedEntity() {
        TestModel model = new TestModel("id-1", "updated");
        TestEntity entity = new TestEntity("id-1", "updated");

        when(repository.existsById("id-1")).thenReturn(true);
        when(mapper.toEntity(any())).thenReturn(entity);

        service.update("id-1", model);

        assertEquals("id-1", model.id);
        verify(repository).save(entity);
    }

    @Test
    void update_nonExistentId_throwsIllegalArgument() {
        TestModel model = new TestModel("missing", "data");
        when(repository.existsById("missing")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.update("missing", model));
        verify(repository, never()).save(any());
    }

    // ========== delete ==========

    @Test
    void delete_existingId_deletesById() {
        when(repository.existsById("id-1")).thenReturn(true);

        service.delete("id-1");

        verify(repository).deleteById("id-1");
    }

    @Test
    void delete_nonExistentId_throwsIllegalArgument() {
        when(repository.existsById("missing")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.delete("missing"));
        verify(repository, never()).deleteById(any());
    }

    // ========== listByGateway ==========

    @Test
    void listByGateway_delegatesToFindByGatewayAndMaps() {
        TestEntity e1 = new TestEntity("id-1", "a");
        TestEntity e2 = new TestEntity("id-2", "b");
        TestModel m1 = new TestModel("id-1", "a");
        TestModel m2 = new TestModel("id-2", "b");

        service.gatewayEntities = List.of(e1, e2);
        when(mapper.toModelList(List.of(e1, e2))).thenReturn(List.of(m1, m2));

        List<TestModel> result = service.listByGateway("gw-1");

        assertEquals(2, result.size());
        assertEquals("gw-1", service.lastGatewayId);
    }

    @Test
    void listByGateway_emptyResult() {
        service.gatewayEntities = List.of();
        when(mapper.toModelList(List.of())).thenReturn(List.of());

        List<TestModel> result = service.listByGateway("gw-1");

        assertTrue(result.isEmpty());
    }

    // ========== Test helpers ==========

    static class TestEntity {
        String id;
        String data;

        TestEntity(String id, String data) {
            this.id = id;
            this.data = data;
        }
    }

    static class TestModel {
        String id;
        String data;

        TestModel(String id, String data) {
            this.id = id;
            this.data = data;
        }
    }

    static class TestCrudService extends AbstractCrudService<TestEntity, TestModel> {
        String lastGatewayId;
        List<TestEntity> gatewayEntities = List.of();

        TestCrudService(JpaRepository<TestEntity, String> repository, EntityMapper<TestEntity, TestModel> mapper) {
            super(repository, mapper);
        }

        @Override
        protected String getId(TestModel model) {
            return model.id;
        }

        @Override
        protected void setId(TestModel model, String id) {
            model.id = id;
        }

        @Override
        protected List<TestEntity> findByGateway(String gatewayId) {
            lastGatewayId = gatewayId;
            return gatewayEntities;
        }

        @Override
        protected String entityName() {
            return "TestEntity";
        }
    }
}
