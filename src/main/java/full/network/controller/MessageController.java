package full.network.controller;

import com.fasterxml.jackson.annotation.JsonView;
import full.network.domain.Message;
import full.network.domain.User;
import full.network.domain.Views;
import full.network.dto.EventType;
import full.network.dto.MetaDto;
import full.network.dto.ObjectType;
import full.network.repository.MessageRepository;
import full.network.util.WebSocketSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping("message")
public class MessageController {
    private static String URL_PATTERN="https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static String IMAGE_PATTERN="\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);

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
    public Message createMessage(@RequestBody Message message,
                                 @AuthenticationPrincipal User user) throws IOException {
        message.setCreationDate(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(user);
        Message updatedMessage = messageRepository.save(message);
        webSocketSender.accept(EventType.CREATE,updatedMessage);
        return updatedMessage;
    }

    @PutMapping("{id}")
    public Message update(@PathVariable("id") Message messageFromDb,
                                     @RequestBody Message message) throws IOException {
        BeanUtils.copyProperties(message,messageFromDb,"id");
        fillMeta(messageFromDb);
        Message updatedMessage= messageRepository.save(messageFromDb);
        webSocketSender.accept(EventType.UPDATE,updatedMessage);
        return updatedMessage;
    }
    @DeleteMapping("{id}")
    public void delete(@PathVariable("id") Message message){

        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE,message);
    }

    private void fillMeta(Message message) throws IOException {
        String text=message.getText();
        Matcher matcher =  URL_REGEX.matcher(text);
        if (matcher.find()) {
            String url = text.substring(matcher.start(), matcher.end());

            matcher = IMG_REGEX.matcher(url);

            message.setLink(url);

            if (matcher.find()) {
                message.setLinkCover(url);
            } else if (!url.contains("youtu")) {
                MetaDto meta = getMeta(url);

                message.setLinkCover(meta.getCover());
                message.setLinkTitle(meta.getTitle());
                message.setLinkDescription(meta.getDescription());
            }
        }
    }
    private MetaDto getMeta(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        Elements title = doc.select("meta[name$=title],meta[property$=title]");
        Elements description = doc.select("meta[name$=description],meta[property$=description]");
        Elements cover = doc.select("meta[name$=image],meta[property$=image]");

        return new MetaDto(
            getContent(title.first()),
            getContent(description.first()),
            getContent(cover.first()));
    }
    private String getContent(Element element) {
        return element == null ? "" : element.attr("content");
    }



}
