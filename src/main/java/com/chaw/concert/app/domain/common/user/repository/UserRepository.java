package com.chaw.concert.app.domain.common.user.repository;

import com.chaw.concert.app.domain.common.user.entity.User;

public interface UserRepository {
    User findByUuid(String uuid);
}
