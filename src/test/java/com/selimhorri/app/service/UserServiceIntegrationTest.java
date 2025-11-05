package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserService Integration Tests")
class UserServiceIntegrationTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should integrate with UserRepository and CredentialRepository to save user")
    void testSave_IntegrationWithRepositories() {
        // Given
        UserDto userDto = UserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();

        // When
        UserDto result = userService.save(userDto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getUserId());
        User savedUser = userRepository.findById(result.getUserId()).orElse(null);
        assertNotNull(savedUser);
        assertEquals("John", savedUser.getFirstName());
        assertEquals("Doe", savedUser.getLastName());
    }

    @Test
    @DisplayName("Should integrate with repositories to find user with credentials")
    void testFindById_IntegrationWithRepositories() {
        // Given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
        user = userRepository.save(user);

        Credential credential = Credential.builder()
                .username("testuser")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(user)
                .build();
        credentialRepository.save(credential);

        // When
        UserDto result = userService.findById(user.getUserId());

        // Then
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals("John", result.getFirstName());
        assertNotNull(result.getCredentialDto());
        assertEquals("testuser", result.getCredentialDto().getUsername());
    }

    @Test
    @DisplayName("Should integrate with repositories to filter users without credentials")
    void testFindAll_IntegrationWithRepositories_FiltersUsersWithoutCredentials() {
        // Given
        User userWithCredential = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
        userWithCredential = userRepository.save(userWithCredential);

        Credential credential = Credential.builder()
                .username("johnuser")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(userWithCredential)
                .build();
        credentialRepository.save(credential);

        User userWithoutCredential = User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .phone("0987654321")
                .build();
        userRepository.save(userWithoutCredential);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size()); // Only user with credentials
        assertEquals("John", result.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should integrate with repositories to delete user and credentials")
    void testDeleteById_IntegrationWithRepositories() {
        // Given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
        user = userRepository.save(user);

        Credential credential = Credential.builder()
                .username("testuser")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(user)
                .build();
        credential = credentialRepository.save(credential);

        Integer userId = user.getUserId();
        Integer credentialId = credential.getCredentialId();

        // When
        userService.deleteById(userId);

        // Then
        User deletedUser = userRepository.findById(userId).orElse(null);
        assertNotNull(deletedUser);
        assertNull(deletedUser.getCredential()); // Credential should be unlinked
        assertFalse(credentialRepository.existsById(credentialId)); // Credential should be deleted
    }

    @Test
    @DisplayName("Should integrate with repositories to update user information")
    void testUpdate_IntegrationWithRepositories() {
        // Given
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
        user = userRepository.save(user);

        Credential credential = Credential.builder()
                .username("testuser")
                .password("password123")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .user(user)
                .build();
        credentialRepository.save(credential);

        UserDto updatedUserDto = UserDto.builder()
                .userId(user.getUserId())
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .phone("1111111111")
                .build();

        // When
        UserDto result = userService.update(updatedUserDto);

        // Then
        assertNotNull(result);
        User updatedUser = userRepository.findById(user.getUserId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("1111111111", updatedUser.getPhone());
        // Verify credential is still linked
        assertNotNull(updatedUser.getCredential());
    }
}

