package com.chaw.concert.app.domain.common.auth.respository;

import com.chaw.concert.app.domain.common.auth.entity.User;

public interface UserRepository {
    User findByUuid(String uuid);

    void save(User user);

    void deleteAll();

    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Long count();
}
