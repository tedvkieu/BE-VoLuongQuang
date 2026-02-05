package com.example.be_voluongquang.services;

import com.example.be_voluongquang.dto.request.contact.ContactRequestDTO;
import com.example.be_voluongquang.dto.response.contact.ContactResponseDTO;
import com.example.be_voluongquang.entity.ContactStatus;
import java.util.List;

public interface ContactService {
    ContactResponseDTO createContact(ContactRequestDTO request);

    List<ContactResponseDTO> getContacts(ContactStatus status);

    ContactResponseDTO updateContactStatus(String contactId, ContactStatus status);
}
