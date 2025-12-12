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
package org.apache.airavata.registry.utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class for merging entity lists during update operations.
 * 
 * <p>This class provides standardized methods for merging collections of entities,
 * handling duplicates and ensuring proper synchronization between existing and new entity states.
 * 
 * <p>Used by services to merge child entity collections when updating parent entities,
 * preventing duplicate entries and maintaining referential integrity.
 */
public class EntityMergeHelper {

    /**
     * Merges two lists of entities, handling duplicates based on an ID extractor function.
     * 
     * <p>This method:
     * <ul>
     *   <li>Deduplicates the current list by keeping only the first occurrence of each ID</li>
     *   <li>Maps new items by ID</li>
     *   <li>Updates existing items with new data or adds new items</li>
     *   <li>Removes items from current list that are not in new list</li>
     * </ul>
     * 
     * @param <T> The entity type
     * @param currentList The existing list of entities (will be modified)
     * @param newList The new list of entities to merge in
     * @param idExtractor Function to extract the unique identifier from an entity
     */
    public static <T> void mergeLists(List<T> currentList, List<T> newList, Function<T, String> idExtractor) {
        if (currentList == null || newList == null) {
            return;
        }
        
        // First, deduplicate currentList by keeping only first occurrence of each ID
        Map<String, T> seen = new LinkedHashMap<>();
        Iterator<T> iterator = currentList.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            String id = idExtractor.apply(item);
            if (id != null) {
                if (seen.containsKey(id)) {
                    iterator.remove(); // Remove duplicate
                } else {
                    seen.put(id, item);
                }
            }
        }
        
        // Create map of new items by ID
        Map<String, T> newMap = newList.stream()
                .filter(item -> idExtractor.apply(item) != null)
                .collect(Collectors.toMap(
                        idExtractor,
                        item -> item,
                        (existing, replacement) -> replacement, // Keep last occurrence if duplicates
                        LinkedHashMap::new));
        
        // Update existing items or add new ones
        for (Map.Entry<String, T> entry : newMap.entrySet()) {
            String id = entry.getKey();
            T newItem = entry.getValue();
            if (seen.containsKey(id)) {
                // Update existing item - replace in place
                int index = currentList.indexOf(seen.get(id));
                if (index >= 0) {
                    currentList.set(index, newItem);
                }
            } else {
                // Add new item
                currentList.add(newItem);
            }
        }
        
        // Remove items from current list that are not in new list
        currentList.removeIf(item -> {
            String id = idExtractor.apply(item);
            return id != null && !newMap.containsKey(id);
        });
    }

    /**
     * Merges two lists of entities using a composite key extractor.
     * 
     * <p>Useful for entities with composite primary keys where a single ID extractor
     * is not sufficient.
     * 
     * @param <T> The entity type
     * @param currentList The existing list of entities (will be modified)
     * @param newList The new list of entities to merge in
     * @param keyExtractor Function to extract a composite key from an entity
     */
    public static <T, K> void mergeListsByKey(List<T> currentList, List<T> newList, Function<T, K> keyExtractor) {
        if (currentList == null || newList == null) {
            return;
        }
        
        // Deduplicate currentList
        Map<K, T> seen = new LinkedHashMap<>();
        Iterator<T> iterator = currentList.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            K key = keyExtractor.apply(item);
            if (key != null) {
                if (seen.containsKey(key)) {
                    iterator.remove();
                } else {
                    seen.put(key, item);
                }
            }
        }
        
        // Create map of new items by key
        Map<K, T> newMap = newList.stream()
                .filter(item -> keyExtractor.apply(item) != null)
                .collect(Collectors.toMap(
                        keyExtractor,
                        item -> item,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new));
        
        // Update existing items or add new ones
        for (Map.Entry<K, T> entry : newMap.entrySet()) {
            K key = entry.getKey();
            T newItem = entry.getValue();
            if (seen.containsKey(key)) {
                int index = currentList.indexOf(seen.get(key));
                if (index >= 0) {
                    currentList.set(index, newItem);
                }
            } else {
                currentList.add(newItem);
            }
        }
        
        // Remove items not in new list
        currentList.removeIf(item -> {
            K key = keyExtractor.apply(item);
            return key != null && !newMap.containsKey(key);
        });
    }
}

