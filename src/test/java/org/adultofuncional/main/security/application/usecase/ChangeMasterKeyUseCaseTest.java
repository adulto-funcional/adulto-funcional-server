package org.adultofuncional.main.security.application.usecase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyChangeRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pruebas unitarias de {@link ChangeMasterKeyUseCase}.
 *
 * <p>
 * El cambio de Master Key debe recifrar las credenciales existentes. Si no se
 * hiciera, el usuario perdería acceso a sus contraseñas guardadas.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class ChangeMasterKeyUseCaseTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordRepository passwordRepository;

  @Mock
  private EncryptionService encryptionService;

  @Mock
  private MasterKeySessionService masterKeySessionService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ChangeMasterKeyUseCase useCase;

  @Test
  @DisplayName("Debe cambiar Master Key y recifrar credenciales existentes")
  void execute_shouldChangeMasterKeyAndReEncryptExistingPasswords() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash",
        "oldMasterHash");
    Password password = Password.create(
        "Gmail",
        "oldSalt",
        new byte[] { 1, 2, 3 },
        new byte[] { 4, 5, 6 },
        LocalDate.of(2026, 1, 10),
        account.getId());
    MasterKeyChangeRequest request = MasterKeyChangeRequest.builder()
        .currentMasterKey("ClaveActual12")
        .newMasterKey("ClaveNueva123")
        .build();
    EncryptionService.EncryptedData encryptedData = new EncryptionService.EncryptedData(
        "newSalt",
        new byte[] { 7, 8, 9 },
        new byte[] { 10, 11, 12 });

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("ClaveActual12", "oldMasterHash")).thenReturn(true);
    when(passwordRepository.findAllByAccountId(account.getId())).thenReturn(List.of(password));
    when(encryptionService.decrypt("oldSalt", new byte[] { 1, 2, 3 }, new byte[] { 4, 5, 6 }, "ClaveActual12"))
        .thenReturn("secreto");
    when(encryptionService.encrypt("secreto", "ClaveNueva123")).thenReturn(encryptedData);
    when(passwordEncoder.encode("ClaveNueva123")).thenReturn("newMasterHash");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MasterKeyStatusResponse response = useCase.execute(account.getId(), request);

    ArgumentCaptor<Password> passwordCaptor = ArgumentCaptor.forClass(Password.class);
    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

    verify(passwordRepository).save(passwordCaptor.capture());
    verify(accountRepository).save(accountCaptor.capture());
    verify(masterKeySessionService).verify(account.getId(), "ClaveNueva123");

    assertEquals("newSalt", passwordCaptor.getValue().getSalt());
    assertArrayEquals(new byte[] { 7, 8, 9 }, passwordCaptor.getValue().getIv());
    assertArrayEquals(new byte[] { 10, 11, 12 }, passwordCaptor.getValue().getCiphertext());
    assertEquals(LocalDate.of(2026, 1, 10), passwordCaptor.getValue().getLastChangeDate());
    assertEquals("newMasterHash", accountCaptor.getValue().getMasterKeyHash());
    assertTrue(response.isHasMasterKey());
    assertTrue(response.isVerified());
  }

  @Test
  @DisplayName("Debe rechazar cambio cuando la Master Key actual es incorrecta")
  void execute_shouldRejectWhenCurrentMasterKeyIsInvalid() {
    Account account = Account.create(
        "Miguel",
        "Blandon",
        "miguel@example.com",
        "+573001234567",
        "passwordHash",
        "oldMasterHash");
    MasterKeyChangeRequest request = MasterKeyChangeRequest.builder()
        .currentMasterKey("ClaveIncorrecta12")
        .newMasterKey("ClaveNueva123")
        .build();

    when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("ClaveIncorrecta12", "oldMasterHash")).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> useCase.execute(account.getId(), request));

    verify(passwordRepository, never()).findAllByAccountId(account.getId());
    verify(accountRepository, never()).save(any(Account.class));
  }
}
