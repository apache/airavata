package org.apache.airavata.research.service.model.repo;

import org.apache.airavata.research.service.model.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    @Query("SELECT r FROM #{#entityName} r WHERE TYPE(r) IN :types")
    Page<Resource> findAllByTypes(@Param("types") List<Class<? extends Resource>> types, Pageable pageable);
}
