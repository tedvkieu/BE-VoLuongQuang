package com.example.be_voluongquang.controller.admin;

import com.example.be_voluongquang.dto.request.contact.ContactStatusUpdateRequestDTO;
import com.example.be_voluongquang.dto.response.contact.ContactResponseDTO;
import com.example.be_voluongquang.entity.ContactStatus;
import com.example.be_voluongquang.services.ContactService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/admin/contact", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminContactController {

    private final ContactService contactService;

    public AdminContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public List<ContactResponseDTO> getContacts(
            @RequestParam(name = "status", required = false) ContactStatus status) {
        return contactService.getContacts(status);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactResponseDTO> updateStatus(
            @PathVariable("id") String id,
            @Valid @RequestBody ContactStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(contactService.updateContactStatus(id, request.getStatus()));
    }
}

