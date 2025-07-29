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
package org.apache.airavata.research.service.v2.repository;

import java.util.List;
import org.apache.airavata.research.service.v2.entity.Code;
import org.apache.airavata.research.service.v2.enums.PrivacyEnumV2;
import org.apache.airavata.research.service.v2.enums.StateEnumV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {
    
    // Find by name containing (case insensitive)
    List<Code> findByNameContainingIgnoreCaseAndPrivacyAndState(String name, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find by code type
    List<Code> findByCodeTypeAndPrivacyAndState(String codeType, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find by programming language
    List<Code> findByProgrammingLanguageAndPrivacyAndState(String programmingLanguage, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find by framework
    List<Code> findByFrameworkAndPrivacyAndState(String framework, PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find all public and active codes with pagination
    Page<Code> findByPrivacyAndState(PrivacyEnumV2 privacy, StateEnumV2 state, Pageable pageable);
    
    // Search by name, description, or tags with pagination
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Code> findByKeywordSearchAndPrivacyAndState(@Param("keyword") String keyword, 
                                                     @Param("privacy") PrivacyEnumV2 privacy, 
                                                     @Param("state") StateEnumV2 state, 
                                                     Pageable pageable);
    
    // Search codes by keyword (for simple list)
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Code> findByKeywordSearchAndPrivacyAndState(@Param("keyword") String keyword, 
                                                     @Param("privacy") PrivacyEnumV2 privacy, 
                                                     @Param("state") StateEnumV2 state);
    
    // Find all public and active codes
    List<Code> findAllByPrivacyAndState(PrivacyEnumV2 privacy, StateEnumV2 state);
    
    // Find codes by tag
    @Query("SELECT c FROM Code c JOIN c.tags t WHERE c.privacy = :privacy AND c.state = :state AND LOWER(t.tagValue) = LOWER(:tag)")
    List<Code> findByTagAndPrivacyAndState(@Param("tag") String tag, 
                                           @Param("privacy") PrivacyEnumV2 privacy, 
                                           @Param("state") StateEnumV2 state);
    
    // Find codes by author
    @Query("SELECT c FROM Code c JOIN c.authors a WHERE c.privacy = :privacy AND c.state = :state AND LOWER(a) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Code> findByAuthorAndPrivacyAndState(@Param("author") String author, 
                                              @Param("privacy") PrivacyEnumV2 privacy, 
                                              @Param("state") StateEnumV2 state);
    
    // Find codes by dependency
    @Query("SELECT c FROM Code c JOIN c.dependencies d WHERE c.privacy = :privacy AND c.state = :state AND LOWER(d) = LOWER(:dependency)")
    List<Code> findByDependencyAndPrivacyAndState(@Param("dependency") String dependency, 
                                                  @Param("privacy") PrivacyEnumV2 privacy, 
                                                  @Param("state") StateEnumV2 state);
    
    // Find top starred codes
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state ORDER BY c.starCount DESC")
    List<Code> findTopStarredCodes(@Param("privacy") PrivacyEnumV2 privacy, 
                                   @Param("state") StateEnumV2 state, 
                                   Pageable pageable);
    
    // Find recently created codes
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state ORDER BY c.createdAt DESC")
    List<Code> findRecentCodes(@Param("privacy") PrivacyEnumV2 privacy, 
                               @Param("state") StateEnumV2 state, 
                               Pageable pageable);
    
    // Find starred codes (starCount > 0)
    Page<Code> findByStarCountGreaterThanAndPrivacyAndState(int starCount, 
                                                            PrivacyEnumV2 privacy, 
                                                            StateEnumV2 state, 
                                                            Pageable pageable);
}