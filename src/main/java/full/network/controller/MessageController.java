package full.network.controller;

import com.fasterxml.jackson.annotation.JsonView;
import full.network.domain.Message;
import full.network.domain.Views;
import full.network.dto.EventType;
import full.network.dto.ObjectType;
import full.network.repository.MessageRepository;
import full.network.util.WebSocketSender;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;


@RestController
@RequestMapping("message")
public class MessageController {
    private final MessageRepository messageRepository;
    private final BiConsumer<EventType,Message> webSocketSender;

    public MessageController(MessageRepository messageRepository, WebSocketSender webSocketSender) {
        this.messageRepository = messageRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE,Views.IdName.class);
    }

    @GetMapping
    @JsonView(Views.IdName.class)
    public List<Message> getM(){
        return messageRepository.findAll();
    }

    @GetMapping("{id}")
    public Message getOne(@PathVariable("id") Message message){
        return message;
    }

    @PostMapping
    public Message createMessage(@RequestBody Message message){
        message.setCreationDate(LocalDateTime.now());
        Message updatedMessage = messageRepository.save(message);
        webSocketSender.accept(EventType.CREATE,updatedMessage);
        return updatedMessage;
    }

    @PutMapping("{id}")
    public Message update(@PathVariable("id") Message messageFromDb,
                                     @RequestBody Message message){
        BeanUtils.copyProperties(message,messageFromDb,"id");
        Message updatedMessage= messageRepository.save(messageFromDb);
        webSocketSender.accept(EventType.UPDATE,updatedMessage);
        return updatedMessage;
    }
    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message){

        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE,message);
    }


}
