package com.somamission.peanutbutter.repository;

import com.somamission.peanutbutter.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
