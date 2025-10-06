package ru.astant.service1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.astant.service1.models.User;
import ru.astant.service1.services.KafkaMessageProducer;
import ru.astant.service1.services.UserService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/chat")
public class ChatController {

    private final UserService userService;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Autowired
    public ChatController(UserService userService, KafkaMessageProducer kafkaMessageProducer) {
        this.userService = userService;
        this.kafkaMessageProducer = kafkaMessageProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, Object> request, Principal principal) {
        String message = request.get("message").toString();
        User user = userService.getCurrentUser(principal);
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("message", message);
        kafkaMessageProducer.sendUser(result);
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Message sent successfully");

        return ResponseEntity.ok(response);
    }
}
