package com.chaw.concert.app.infrastructure.mysql.user;

import com.chaw.concert.app.domain.common.auth.entity.User;
import com.chaw.concert.app.domain.common.auth.respository.UserRepository;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository repository;

    public UserRepositoryImpl(UserJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public User findByUsername(String username) {
        User user = repository.findByUsername(username);
        if (user == null) {
            throw new BaseException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return user;
    }

    @Override
    public Boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public Long count() {
        return repository.count();
    }

    @Override
    public User findByUuid(String uuid) {
        return repository.findByUuid(uuid);
    }

    @Override
    public void save(User user) {
        repository.save(user);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }
}
