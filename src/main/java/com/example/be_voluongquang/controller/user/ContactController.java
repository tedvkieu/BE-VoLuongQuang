package com.example.be_voluongquang.controller.user;

import com.example.be_voluongquang.dto.request.contact.ContactRequestDTO;
import com.example.be_voluongquang.dto.response.contact.ContactResponseDTO;
import com.example.be_voluongquang.services.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/contact", produces = MediaType.APPLICATION_JSON_VALUE)
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/public")
    public ResponseEntity<ContactResponseDTO> createContact(@Valid @RequestBody ContactRequestDTO request) {
        ContactResponseDTO created = contactService.createContact(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

