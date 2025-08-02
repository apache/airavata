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
import org.apache.airavata.research.service.enums.PrivacyEnum;
import org.apache.airavata.research.service.enums.StateEnum;
import org.apache.airavata.research.service.v2.entity.Code;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {
    
    // Find by name containing (case insensitive)
    List<Code> findByNameContainingIgnoreCaseAndPrivacyAndState(String name, PrivacyEnum privacy, StateEnum state);
    
    // Find by code type
    List<Code> findByCodeTypeAndPrivacyAndState(String codeType, PrivacyEnum privacy, StateEnum state);
    
    // Find by programming language
    List<Code> findByProgrammingLanguageAndPrivacyAndState(String programmingLanguage, PrivacyEnum privacy, StateEnum state);
    
    // Find by framework
    List<Code> findByFrameworkAndPrivacyAndState(String framework, PrivacyEnum privacy, StateEnum state);
    
    // Find all public and active codes with pagination
    Page<Code> findByPrivacyAndState(PrivacyEnum privacy, StateEnum state, Pageable pageable);
    
    // Search by name, description, or tags with pagination
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Code> findByKeywordSearchAndPrivacyAndState(@Param("keyword") String keyword, 
                                                     @Param("privacy") PrivacyEnum privacy, 
                                                     @Param("state") StateEnum state, 
                                                     Pageable pageable);
    
    // Search codes by keyword (for simple list)
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Code> findByKeywordSearchAndPrivacyAndState(@Param("keyword") String keyword, 
                                                     @Param("privacy") PrivacyEnum privacy, 
                                                     @Param("state") StateEnum state);
    
    // Find all public and active codes
    List<Code> findAllByPrivacyAndState(PrivacyEnum privacy, StateEnum state);
    
    // Find codes by tag
    @Query("SELECT c FROM Code c JOIN c.tags t WHERE c.privacy = :privacy AND c.state = :state AND LOWER(t.value) = LOWER(:tag)")
    List<Code> findByTagAndPrivacyAndState(@Param("tag") String tag, 
                                           @Param("privacy") PrivacyEnum privacy, 
                                           @Param("state") StateEnum state);
    
    // Find codes by author
    @Query("SELECT c FROM Code c JOIN c.authors a WHERE c.privacy = :privacy AND c.state = :state AND LOWER(a) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Code> findByAuthorAndPrivacyAndState(@Param("author") String author, 
                                              @Param("privacy") PrivacyEnum privacy, 
                                              @Param("state") StateEnum state);
    
    // Find codes by dependency
    @Query("SELECT c FROM Code c JOIN c.dependencies d WHERE c.privacy = :privacy AND c.state = :state AND LOWER(d) = LOWER(:dependency)")
    List<Code> findByDependencyAndPrivacyAndState(@Param("dependency") String dependency, 
                                                  @Param("privacy") PrivacyEnum privacy, 
                                                  @Param("state") StateEnum state);
    
    // Find top starred codes (TODO: implement proper v1 ResourceStar integration)
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state ORDER BY c.createdAt DESC")
    List<Code> findTopStarredCodes(@Param("privacy") PrivacyEnum privacy, 
                                   @Param("state") StateEnum state, 
                                   Pageable pageable);
    
    // Find recently created codes
    @Query("SELECT c FROM Code c WHERE c.privacy = :privacy AND c.state = :state ORDER BY c.createdAt DESC")
    List<Code> findRecentCodes(@Param("privacy") PrivacyEnum privacy, 
                               @Param("state") StateEnum state, 
                               Pageable pageable);
    
    // TODO: Remove this method - implement proper v1 ResourceStar integration
    // Temporarily removed starCount-based method
}