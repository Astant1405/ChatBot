package ru.astant.service1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.astant.service1.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByYandexId(String yandexId);
}
