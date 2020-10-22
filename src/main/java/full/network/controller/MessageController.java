package full.network.controller;

import com.fasterxml.jackson.annotation.JsonView;
import full.network.domain.Message;
import full.network.domain.User;
import full.network.domain.Views;
import full.network.dto.MessagePageDto;
import full.network.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("message")
public class MessageController {
    public static final int MESSAGES_PP = 3;
    private final MessageService messageService;
    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    @JsonView(Views.FullMessage.class)
    public MessagePageDto list(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = MESSAGES_PP, sort = { "id" }, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return messageService.findForUser(pageable, user);
    }

    @GetMapping("{id}")
    public Message getOne(@PathVariable("id") Message message){
        return message;
    }

    @PostMapping
    @JsonView(Views.FullMessage.class)
    public Message createMessage(@RequestBody Message message,
                                 @AuthenticationPrincipal User user) throws IOException {
        return messageService.create(message,user);
    }


    @PutMapping("{id}")
    @JsonView(Views.FullMessage.class)
    public Message update(@PathVariable("id") Message messageFromDb,
                          @RequestBody Message message,
                          @AuthenticationPrincipal User user)
            throws IOException {
        if(user.equals(messageFromDb.getAuthor())){
            return messageService.update(messageFromDb,message,user);
        }
        else{
            return messageFromDb;
        }

    }
    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message,
                       @AuthenticationPrincipal User user){
        if(user.equals(message.getAuthor())){
            messageService.delete(message);
        }
    }


}
