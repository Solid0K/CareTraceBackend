package com.krishu.caretracev2.Controller;

import com.krishu.caretracev2.DTO.MessageRequest;
import com.krishu.caretracev2.DTO.MessageResponse;
import com.krishu.caretracev2.Service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/sendMessage/{patientId}")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable String patientId,@RequestBody MessageRequest request, Authentication authentication){
        return ResponseEntity.ok(messageService.sendMessage(patientId,request,authentication));
    }

    @GetMapping("/getConversation/{patientId}")
    public ResponseEntity<List<MessageResponse>> getConversation(@PathVariable String patientId, Authentication authentication){
        return ResponseEntity.ok(messageService.getConversation(patientId,authentication));
    }

    @PutMapping("/markAsRead/{messageId}")
    public void MarkAsRead(@PathVariable String messageId,Authentication authentication){
        messageService.markAsRead(messageId,authentication);
    }

    @GetMapping("/getUnreadMessages")
    public ResponseEntity<List<MessageResponse>> getUnReadMessage(Authentication authentication){
        return ResponseEntity.ok(messageService.getUnreadMessages(authentication));
    }
}
