package com.selimhorri.app.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserResource Unit Tests")
class UserResourceUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserResource userResource;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
        objectMapper = new ObjectMapper();

        CredentialDto credentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("testuser")
                .build();

        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .credentialDto(credentialDto)
                .build();
    }

    @Test
    @DisplayName("Should return all users when GET /api/users")
    void testFindAll_Success() throws Exception {
        // Given
        List<UserDto> users = new ArrayList<>();
        users.add(testUserDto);
        when(userService.findAll()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dtos").isArray())
                .andExpect(jsonPath("$.dtos[0].userId").value(1))
                .andExpect(jsonPath("$.dtos[0].firstName").value("John"));

        verify(userService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by ID when GET /api/users/{userId}")
    void testFindById_Success() throws Exception {
        // Given
        String userId = "1";
        when(userService.findById(1)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(userService, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void testFindById_NotFound() throws Exception {
        // Given
        String userId = "999";
        when(userService.findById(999))
                .thenThrow(new UserObjectNotFoundException("User with id: 999 not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should return user by username when GET /api/users/username/{username}")
    void testFindByUsername_Success() throws Exception {
        // Given
        String username = "testuser";
        when(userService.findByUsername(username)).thenReturn(testUserDto);

        // When & Then
        mockMvc.perform(get("/api/users/username/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.credential.username").value("testuser"));

        verify(userService, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should create user when POST /api/users")
    void testSave_Success() throws Exception {
        // Given
        UserDto newUserDto = UserDto.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .phone("9876543210")
                .build();

        UserDto savedUserDto = UserDto.builder()
                .userId(2)
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .phone("9876543210")
                .build();

        when(userService.save(any(UserDto.class))).thenReturn(savedUserDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.firstName").value("New"));

        verify(userService, times(1)).save(any(UserDto.class));
    }

    @Test
    @DisplayName("Should update user when PUT /api/users")
    void testUpdate_Success() throws Exception {
        // Given
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .phone("1111111111")
                .build();

        when(userService.update(any(UserDto.class))).thenReturn(updatedUserDto);

        // When & Then
        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));

        verify(userService, times(1)).update(any(UserDto.class));
    }

    @Test
    @DisplayName("Should update user by ID when PUT /api/users/{userId}")
    void testUpdateById_Success() throws Exception {
        // Given
        String userId = "1";
        UserDto updatedUserDto = UserDto.builder()
                .userId(1)
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userService.update(eq(1), any(UserDto.class))).thenReturn(updatedUserDto);

        // When & Then
        mockMvc.perform(put("/api/users/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));

        verify(userService, times(1)).update(eq(1), any(UserDto.class));
    }

    @Test
    @DisplayName("Should delete user when DELETE /api/users/{userId}")
    void testDeleteById_Success() throws Exception {
        // Given
        String userId = "1";
        doNothing().when(userService).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/api/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(userService, times(1)).deleteById(1);
    }
}

