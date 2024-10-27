package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    User findByUuid(String uuid);

    User findByUsername(String username);

    Boolean existsByUsername(String username);
}
