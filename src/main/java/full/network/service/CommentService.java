package full.network.service;

import full.network.domain.Comment;
import full.network.domain.User;
import full.network.domain.Views;
import full.network.dto.EventType;
import full.network.dto.ObjectType;
import full.network.repository.CommentRepository;
import full.network.util.WebSocketSender;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Service
public class CommentService {
    private  final CommentRepository commentRepository;
    private final BiConsumer<EventType,Comment> wsSender;

    public CommentService(CommentRepository commentRepository, WebSocketSender wsSender) {
        this.commentRepository = commentRepository;
        this.wsSender = wsSender.getSender(ObjectType.COMMENT, Views.FullComment.class);
    }
    public Comment create(Comment comment, User user){
        comment.setAuthor(user);
        Comment commentFromDb=commentRepository.save(comment);
        wsSender.accept(EventType.CREATE,commentFromDb);


        return commentFromDb;
    }
}
