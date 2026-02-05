package com.example.be_voluongquang.services.impl;

import com.example.be_voluongquang.dto.request.contact.ContactRequestDTO;
import com.example.be_voluongquang.dto.response.contact.ContactResponseDTO;
import com.example.be_voluongquang.entity.ContactEntity;
import com.example.be_voluongquang.entity.ContactStatus;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.repository.ContactRepository;
import com.example.be_voluongquang.services.ContactService;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ContactServiceImpl implements ContactService {
    private static final String CONTACT_LABEL = "Contact";

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public ContactResponseDTO createContact(ContactRequestDTO request) {
        ContactRequestDTO payload = request != null ? request : new ContactRequestDTO();

        String email = normalizeOptional(payload.getEmail());
        String phone = normalizeOptional(payload.getPhone());
        if (!StringUtils.hasText(email) && !StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("Email hoặc số điện thoại là bắt buộc");
        }

        ContactEntity entity = ContactEntity.builder()
                .email(email)
                .phone(phone)
                .message(normalizeRequired(payload.getMessage(), "Message"))
                .status(ContactStatus.NEW)
                .build();

        return toResponse(contactRepository.save(entity));
    }

    @Override
    public List<ContactResponseDTO> getContacts(ContactStatus status) {
        List<ContactEntity> entities = status == null
                ? contactRepository.findByIsDeletedFalseOrderByCreatedAtDesc()
                : contactRepository.findByIsDeletedFalseAndStatusOrderByCreatedAtDesc(status);
        return entities.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContactResponseDTO updateContactStatus(String contactId, ContactStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        ContactEntity entity = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException(CONTACT_LABEL, "contactId", contactId));
        if (Boolean.TRUE.equals(entity.getIsDeleted())) {
            throw new ResourceNotFoundException(CONTACT_LABEL, "contactId", contactId);
        }
        entity.setStatus(status);
        return toResponse(contactRepository.save(entity));
    }

    private ContactResponseDTO toResponse(ContactEntity entity) {
        return ContactResponseDTO.builder()
                .contactId(entity.getContactId())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.getIsDeleted())
                .build();
    }

    private String normalizeRequired(String value, String label) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
