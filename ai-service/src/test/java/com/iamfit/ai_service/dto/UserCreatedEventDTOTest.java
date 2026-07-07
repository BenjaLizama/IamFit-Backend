package com.iamfit.ai_service.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserCreatedEventDTOTest {

    @Test
    @DisplayName("UserCreatedEventDTO - Debe funcionar correctamente con Getter y Setter")
    void testUserCreatedEventDTO() {
        UserCreatedEventDTO dto = new UserCreatedEventDTO();
        dto.setCredentialId("CRED777");
        dto.setNickname("Runner");
        dto.setAge(25);
        dto.setHeight(180);
        dto.setWeight(80);
        dto.setSex("M");

        assertEquals("CRED777", dto.getCredentialId());
        assertEquals("Runner", dto.getNickname());
        assertEquals(25, dto.getAge());
        assertEquals(180, dto.getHeight());
        assertEquals(80, dto.getWeight());
        assertEquals("M", dto.getSex());
        assertNotNull(dto.toString());
    }
}