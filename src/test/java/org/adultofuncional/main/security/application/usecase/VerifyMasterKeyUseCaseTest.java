package org.adultofuncional.main.security.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pruebas unitarias de {@link VerifyMasterKeyUseCase}.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class VerifyMasterKeyUseCaseTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MasterKeySessionService masterKeySessionService;

  @InjectMocks
  private VerifyMasterKeyUseCase useCase;

  @Test
  @DisplayName("Debe verificar Master Key correcta y abrir sesión del gestor")
  void execute_shouldVerifyMasterKeyAndOpenSession() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash",
        "masterHash");
    MasterKeyRequest request = MasterKeyRequest.builder()
        .masterKey("ClaveMaestra12")
        .build();

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("ClaveMaestra12", "masterHash")).thenReturn(true);

    MasterKeyStatusResponse response = useCase.execute(account.getId(), request);

    verify(masterKeySessionService).verify(account.getId(), "ClaveMaestra12");
    assertTrue(response.isHasMasterKey());
    assertTrue(response.isVerified());
  }

  @Test
  @DisplayName("Debe rechazar Master Key incorrecta")
  void execute_shouldRejectInvalidMasterKey() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash",
        "masterHash");
    MasterKeyRequest request = MasterKeyRequest.builder()
        .masterKey("ClaveIncorrecta12")
        .build();

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("ClaveIncorrecta12", "masterHash")).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> useCase.execute(account.getId(), request));

    verify(masterKeySessionService, never()).verify(account.getId(), "ClaveIncorrecta12");
  }
}
