package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.user.entity.User;
import com.chaw.concert.app.domain.common.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryImpl(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public User findByUuid(String uuid) {
        return repository.findByUuid(uuid);
    }
}
