package full.network.service;

import full.network.domain.Message;
import full.network.domain.User;
import full.network.domain.UserSubscription;
import full.network.domain.Views;
import full.network.dto.EventType;
import full.network.dto.MessagePageDto;
import full.network.dto.MetaDto;
import full.network.dto.ObjectType;
import full.network.repository.MessageRepository;
import full.network.repository.UserSubscriptionRepository;
import full.network.util.WebSocketSender;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private static String URL_PATTERN="https?:\\/\\/?[\\w\\d\\._\\-%\\/\\?=&#]+";
    private static String IMAGE_PATTERN="\\.(jpeg|jpg|gif|png)$";

    private static Pattern URL_REGEX = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);
    private static Pattern IMG_REGEX = Pattern.compile(IMAGE_PATTERN, Pattern.CASE_INSENSITIVE);
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final MessageRepository messageRepository;
    private final BiConsumer<EventType, Message> webSocketSender;


    public MessageService(UserSubscriptionRepository userSubscriptionRepository, MessageRepository messageRepository, WebSocketSender webSocketSender) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.messageRepository = messageRepository;
        this.webSocketSender = webSocketSender.getSender(ObjectType.MESSAGE, Views.FullMessage.class);
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


    public void delete(Message message) {
        messageRepository.delete(message);
        webSocketSender.accept(EventType.REMOVE,message);
    }

    public Message update(Message messageFromDb, Message message,User user) throws IOException {
        messageFromDb.setText(message.getText());
        fillMeta(messageFromDb);
        messageFromDb.setAuthor(user);

        Message updatedMessage= messageRepository.save(messageFromDb);

        webSocketSender.accept(EventType.UPDATE,updatedMessage);
        return updatedMessage;
    }

    public Message create(Message message, User user) throws IOException {
        message.setCreationDate(LocalDateTime.now());
        fillMeta(message);
        message.setAuthor(user);
        Message updatedMessage = messageRepository.save(message);
        webSocketSender.accept(EventType.CREATE,updatedMessage);
        return updatedMessage;
    }

    public MessagePageDto findForUser(Pageable pageable,User user) {
        List<User> channels = userSubscriptionRepository.findBySubscriber(user)
                .stream()
                .map(UserSubscription::getChannel)
                .collect(Collectors.toList());

        channels.add(user);

        Page<Message> page = messageRepository.findByAuthorIn(channels,pageable);
        return new MessagePageDto(
                page.getContent(),
                pageable.getPageNumber(),
                page.getTotalPages()
        );
    }
}
