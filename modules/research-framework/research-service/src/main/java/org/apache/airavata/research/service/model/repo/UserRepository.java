package org.apache.airavata.research.service.model.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.apache.airavata.research.service.model.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
}
