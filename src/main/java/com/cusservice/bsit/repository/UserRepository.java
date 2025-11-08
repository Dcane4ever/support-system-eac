package com.cusservice.bsit.repository;

import com.cusservice.bsit.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    List<User> findByRoleAndAvailable(User.Role role, boolean available);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
