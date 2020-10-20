package full.network.repository;

import full.network.domain.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @EntityGraph(attributePaths = {"comments"})
    List<Message> findAll();
}
