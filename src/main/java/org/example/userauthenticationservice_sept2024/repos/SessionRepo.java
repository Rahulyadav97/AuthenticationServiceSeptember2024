package org.example.userauthenticationservice_sept2024.repos;

import org.example.userauthenticationservice_sept2024.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepo extends JpaRepository<Session, Long> {
    Session findByToken(String token);
    Session save(Session session);
}
