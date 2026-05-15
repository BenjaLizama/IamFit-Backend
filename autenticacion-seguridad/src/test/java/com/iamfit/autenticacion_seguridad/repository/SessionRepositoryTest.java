package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import com.iamfit.autenticacion_seguridad.entity.SessionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    private CredentialEntity credential;

    @BeforeEach
    void setUp() {
        // La credencial necesita email y password (NOT NULL en tu schema)
        credential = new CredentialEntity();
        credential.setEmail("test-" + UUID.randomUUID() + "@mail.com");
        credential.setPassword("secure_password_hash");
        credential.setIsActive(true);

        credential = entityManager.persistAndFlush(credential);
    }

    @Test
    @DisplayName("Debería encontrar sesión por hash de refresh token")
    void findByRefreshTokenHash_ShouldReturnSession() {
        SessionEntity session = createValidSession("token-123", "device-1");

        entityManager.persistAndFlush(session);

        Optional<SessionEntity> result = sessionRepository.findByRefreshTokenHash("token-123");

        assertThat(result).isPresent();
        assertThat(result.get().getRefreshTokenHash()).isEqualTo("token-123");
    }

    @Test
    @DisplayName("Debería encontrar sesión por credencial y ID de dispositivo")
    void findByCredentialAndDeviceId_ShouldReturnSession() {
        SessionEntity session = createValidSession("token-456", "pixel-7");

        entityManager.persistAndFlush(session);

        Optional<SessionEntity> result = sessionRepository.findByCredentialAndDeviceId(credential, "pixel-7");

        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo("pixel-7");
    }

    @Test
    @DisplayName("Debería eliminar sesiones por credencial")
    void deleteByCredential_ShouldRemoveSessions() {
        SessionEntity session = createValidSession("token-del", "device-x");
        entityManager.persistAndFlush(session);

        sessionRepository.deleteByCredential(credential);
        entityManager.clear();

        assertThat(sessionRepository.findByRefreshTokenHash("token-del")).isEmpty();
    }

    @Test
    @DisplayName("Debería revocar todas las sesiones activas de un usuario")
    void revokeAllByCredentialId_ShouldDeactivateSessions() {
        SessionEntity session = createValidSession("h1", "device-y");
        session.setIsActive(true);
        entityManager.persistAndFlush(session);

        sessionRepository.revokeAllByCredentialId(credential.getId());

        entityManager.flush();
        entityManager.clear();

        Optional<SessionEntity> updated = sessionRepository.findByRefreshTokenHash("h1");
        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debería eliminar sesiones expiradas")
    void deleteByExpiryDateBefore_ShouldRemoveExpired() {
        Instant now = Instant.now();

        // Sesión expirada
        SessionEntity expired = createValidSession("expired", "d1");
        expired.setExpiryDate(now.minusSeconds(60));

        // Sesión válida
        SessionEntity valid = createValidSession("valid", "d2");
        valid.setExpiryDate(now.plusSeconds(3600));

        entityManager.persist(expired);
        entityManager.persist(valid);
        entityManager.flush();

        sessionRepository.deleteByExpiryDateBefore(now);
        entityManager.clear();

        assertThat(sessionRepository.findByRefreshTokenHash("expired")).isEmpty();
        assertThat(sessionRepository.findByRefreshTokenHash("valid")).isPresent();
    }

    @Test
    @DisplayName("Debería crear índices físicos correctos para SESSION")
    void schema_ShouldContainExpectedSessionIndexes() throws SQLException {
        Map<String, List<String>> indexes = new HashMap<>();

        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "SESSION", false, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");

                if (indexName == null || columnName == null || indexName.startsWith("PRIMARY_KEY")) {
                    continue;
                }

                indexes.computeIfAbsent(indexName, key -> new ArrayList<>()).add(columnName);
            }
        }

        assertThat(indexes)
                .containsEntry("IDX_SESSION_REFRESH_TOKEN_HASH", List.of("REFRESH_TOKEN_HASH"))
                .containsEntry("IDX_SESSION_CREDENTIAL_DEVICE", List.of("CREDENTIAL_ID", "DEVICE_ID"));
    }

    /**
     * Helper para asegurar que todos los campos NOT NULL estén presentes
     */
    private SessionEntity createValidSession(String hash, String deviceId) {
        SessionEntity session = new SessionEntity();
        session.setCredential(credential);
        session.setRefreshTokenHash(hash);
        session.setDeviceId(deviceId);
        session.setExpiryDate(Instant.now().plusSeconds(3600)); // Crucial: es NOT NULL
        session.setIsActive(true);
        return session;
    }
}
