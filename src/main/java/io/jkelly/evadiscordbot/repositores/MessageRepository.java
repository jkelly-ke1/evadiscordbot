package io.jkelly.evadiscordbot.repositores;

import io.jkelly.evadiscordbot.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
}
