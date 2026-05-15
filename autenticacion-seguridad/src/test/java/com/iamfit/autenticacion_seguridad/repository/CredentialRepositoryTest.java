package com.iamfit.autenticacion_seguridad.repository;

import com.iamfit.autenticacion_seguridad.entity.CredentialEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.hibernate.exception.ConstraintViolationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CredentialRepositoryTest {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Debería encontrar credencial por email")
    void findByEmail_ShouldReturnCredential() {
        CredentialEntity credential = new CredentialEntity();
        credential.setEmail("test@nomall.com");
        credential.setPassword("hash");
        credential.setIsActive(true);

        entityManager.persist(credential);
        entityManager.flush();

        Optional<CredentialEntity> result = credentialRepository.findByEmail("test@nomall.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@nomall.com");
    }

    @Test
    @DisplayName("Debería eliminar cuentas inactivas antiguas")
    void deleteOldInactiveAccounts_ShouldWork() {
        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(60);

        CredentialEntity old = new CredentialEntity();
        old.setEmail("old@test.com");
        old.setPassword("p");
        old.setIsActive(false);
        old.setDeactivatedAt(now.minusSeconds(120));

        CredentialEntity recent = new CredentialEntity();
        recent.setEmail("recent@test.com");
        recent.setPassword("p");
        recent.setIsActive(false);
        recent.setDeactivatedAt(now);

        entityManager.persist(old);
        entityManager.persist(recent);
        entityManager.flush();

        int deleted = credentialRepository.deleteOldInactiveAccounts(cutoff);
        entityManager.clear();

        assertThat(deleted).isEqualTo(1);
        assertThat(credentialRepository.findByEmail("old@test.com")).isEmpty();
        assertThat(credentialRepository.findByEmail("recent@test.com")).isPresent();
    }

    @Test
    @DisplayName("Debería desactivar cuenta y actualizar timestamps")
    void deactivateById_ShouldUpdateFields() {
        CredentialEntity credential = new CredentialEntity();
        credential.setEmail("active@test.com");
        credential.setPassword("p");
        credential.setIsActive(true);

        entityManager.persist(credential);
        entityManager.flush();
        UUID id = credential.getId();

        credentialRepository.deactivateById(id);
        entityManager.clear();

        Optional<CredentialEntity> updated = credentialRepository.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().getIsActive()).isFalse();
        assertThat(updated.get().getDeactivatedAt()).isNotNull();
        assertThat(updated.get().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debería crear un índice único sobre email")
    void schema_ShouldContainUniqueEmailIndex() throws SQLException {
        List<String> emailIndexColumns = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getIndexInfo(null, null, "CREDENTIAL", false, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString("INDEX_NAME");
                String columnName = resultSet.getString("COLUMN_NAME");

                if (indexName == null || columnName == null || indexName.startsWith("PRIMARY_KEY")) {
                    continue;
                }

                if (indexName.startsWith("UX_CREDENTIAL_EMAIL")) {
                    emailIndexColumns.add(columnName);
                }
            }
        }

        assertThat(emailIndexColumns).containsExactly("EMAIL");
    }

    @Test
    @DisplayName("Debería rechazar emails duplicados a nivel de base de datos")
    void duplicateEmail_ShouldFailOnFlush() {
        CredentialEntity first = new CredentialEntity();
        first.setEmail("duplicate@test.com");
        first.setPassword("p");
        first.setIsActive(true);

        CredentialEntity duplicate = new CredentialEntity();
        duplicate.setEmail("duplicate@test.com");
        duplicate.setPassword("p");
        duplicate.setIsActive(true);

        entityManager.persist(first);
        entityManager.flush();

        entityManager.persist(duplicate);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(ConstraintViolationException.class);
    }
}
