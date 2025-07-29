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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeRepository extends JpaRepository<Code, String> {
    
    // Find by name containing (case insensitive)
    List<Code> findByNameContainingIgnoreCaseAndIsPublicTrue(String name);
    
    // Find by code type
    List<Code> findByCodeTypeAndIsPublicTrue(String codeType);
    
    // Find by programming language
    List<Code> findByProgrammingLanguageAndIsPublicTrue(String programmingLanguage);
    
    // Find by framework
    List<Code> findByFrameworkAndIsPublicTrue(String framework);
    
    // Find all public and active codes with pagination
    Page<Code> findByIsPublicTrueAndIsActiveTrue(Pageable pageable);
    
    // Search by name, description, or tags with pagination
    @Query("SELECT c FROM Code c WHERE c.isPublic = true AND c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Code> findByKeywordSearchAndIsPublicTrueAndIsActiveTrue(@Param("keyword") String keyword, Pageable pageable);
    
    // Search codes by keyword (for simple list)
    @Query("SELECT c FROM Code c WHERE c.isPublic = true AND c.isActive = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.codeType) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.programmingLanguage) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.framework) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Code> findByKeywordSearchAndIsPublicTrueAndIsActiveTrue(@Param("keyword") String keyword);
    
    // Find all public and active codes
    List<Code> findAllByIsPublicTrueAndIsActiveTrue();
    
    // Find codes by tag
    @Query("SELECT c FROM Code c JOIN c.tags t WHERE c.isPublic = true AND c.isActive = true AND LOWER(t) = LOWER(:tag)")
    List<Code> findByTagAndIsPublicTrue(@Param("tag") String tag);
    
    // Find codes by author
    @Query("SELECT c FROM Code c JOIN c.authors a WHERE c.isPublic = true AND c.isActive = true AND LOWER(a) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<Code> findByAuthorAndIsPublicTrue(@Param("author") String author);
    
    // Find codes by dependency
    @Query("SELECT c FROM Code c JOIN c.dependencies d WHERE c.isPublic = true AND c.isActive = true AND LOWER(d) = LOWER(:dependency)")
    List<Code> findByDependencyAndIsPublicTrue(@Param("dependency") String dependency);
    
    // Find top starred codes
    @Query("SELECT c FROM Code c WHERE c.isPublic = true AND c.isActive = true ORDER BY c.starCount DESC")
    List<Code> findTopStarredCodes(Pageable pageable);
    
    // Find recently created codes
    @Query("SELECT c FROM Code c WHERE c.isPublic = true AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Code> findRecentCodes(Pageable pageable);
    
    // Find starred codes (starCount > 0)
    Page<Code> findByStarCountGreaterThanAndIsPublicTrueAndIsActiveTrue(int starCount, Pageable pageable);
}