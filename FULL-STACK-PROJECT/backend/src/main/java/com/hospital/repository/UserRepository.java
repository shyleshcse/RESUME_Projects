package com.hospital.repository;

import com.hospital.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * A Repository = the class that talks to ONE database table.
 * We only write the method NAMES. Spring writes the SQL for us.
 *   findByEmail("a@b.com")  ->  SELECT * FROM users WHERE email = 'a@b.com'
 * JpaRepository already gives us save(), findById(), findAll(), count(), deleteById() ...
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
