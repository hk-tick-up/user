package com.example.user.config.kafka;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user.entity.User;

@RestController
public class KafkaController {

    @Autowired
    private KafkaTemplate<Object, Object> template;

    @PostMapping(path = "/join-waiting-room")
    public void sendTo(@PathVariable String userId) {
        this.template.send("user", new User(userId));
    }
}
