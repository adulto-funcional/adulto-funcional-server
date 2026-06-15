package org.adultofuncional.main.security.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pruebas unitarias de {@link CreateMasterKeyUseCase}.
 *
 * <p>
 * Validan que una cuenta registrada sin Master Key pueda crearla después del
 * registro, y que una cuenta que ya la tenga no pueda sobrescribirla por este
 * flujo.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class CreateMasterKeyUseCaseTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MasterKeySessionService masterKeySessionService;

  @InjectMocks
  private CreateMasterKeyUseCase useCase;

  @Test
  @DisplayName("Debe crear Master Key para una cuenta que no la tiene")
  void execute_shouldCreateMasterKeyWhenAccountDoesNotHaveOne() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash");
    MasterKeyRequest request = MasterKeyRequest.builder()
        .masterKey("ClaveMaestra12")
        .build();

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    when(passwordEncoder.encode("ClaveMaestra12")).thenReturn("masterHash");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MasterKeyStatusResponse response = useCase.execute(account.getId(), request);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(accountCaptor.capture());
    verify(masterKeySessionService).verify(account.getId(), "ClaveMaestra12");

    assertEquals("masterHash", accountCaptor.getValue().getMasterKeyHash());
    assertTrue(response.isHasMasterKey());
    assertTrue(response.isVerified());
  }

  @Test
  @DisplayName("Debe rechazar la creación cuando la cuenta ya tiene Master Key")
  void execute_shouldRejectWhenAccountAlreadyHasMasterKey() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash",
        "existingMasterHash");
    MasterKeyRequest request = MasterKeyRequest.builder()
        .masterKey("ClaveMaestra12")
        .build();

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

    assertThrows(ConflictException.class, () -> useCase.execute(account.getId(), request));

    verify(accountRepository, never()).save(any(Account.class));
    verify(masterKeySessionService, never()).verify(any(UUID.class), any(String.class));
  }
}
