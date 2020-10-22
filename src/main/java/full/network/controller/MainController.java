package full.network.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import full.network.domain.User;
import full.network.domain.Views;
import full.network.dto.MessagePageDto;
import full.network.repository.UserDetailsRepository;
import full.network.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@Controller
@RequestMapping("/")
public class MainController {
    private final UserDetailsRepository userDetailsRepository;
    private final MessageService messageService;
    private final ObjectWriter writer;
    private  final ObjectWriter profileWriter;

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    public MainController(UserDetailsRepository userDetailsRepository, MessageService messageService, ObjectMapper mapper)
    {
        this.userDetailsRepository = userDetailsRepository;
        this.messageService = messageService;
        this.writer= mapper.setConfig(mapper.getSerializationConfig())
                .writerWithView(Views.FullMessage.class);
        this.profileWriter = mapper.setConfig(mapper.getSerializationConfig())
                .writerWithView(Views.FullProfile.class);
    }

    @GetMapping
    public String main(
            Model model,
            @AuthenticationPrincipal User user
    ) throws JsonProcessingException {
        HashMap<Object, Object> data = new HashMap<>();

        if (user != null) {
            User userFromDb = userDetailsRepository.findById(user.getId()).get();
            String serializedProfile = profileWriter.writeValueAsString(userFromDb);
            model.addAttribute("profile", serializedProfile);

            Sort sort = Sort.by(Sort.Direction.DESC, "id");
            PageRequest pageRequest = PageRequest.of(0, MessageController.MESSAGES_PP, sort);
            MessagePageDto messagePageDto = messageService.findForUser(pageRequest, user);

            String messages = writer.writeValueAsString(messagePageDto.getMessages());

            model.addAttribute("messages", messages);
            data.put("currentPage", messagePageDto.getCurrentPage());
            data.put("totalPages", messagePageDto.getTotalPages());
        } else {
            model.addAttribute("messages", "[]");
            model.addAttribute("profile", "null");
        }

        model.addAttribute("frontendData", data);
        model.addAttribute("isDevMode", "dev".equals(profile));

        return "index";
    }
}