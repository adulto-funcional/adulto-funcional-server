package org.adultofuncional.main.account.application.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.adultofuncional.main.account.application.dto.ChangePasswordRequest;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ChangePasswordUseCase useCase;

  @Test
  void execute_shouldChangePasswordWhenCurrentPasswordMatches() {
    UUID accountId = UUID.randomUUID();
    Account account = Account.reconstitute(
        accountId,
        "Ana",
        "Perez",
        "ana@example.com",
        "3001234567",
        LocalDateTime.now().minusDays(1),
        "oldPasswordHash",
        null);
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .currentPassword("PasswordActual12")
        .newPassword("PasswordNueva12")
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("PasswordActual12", "oldPasswordHash")).thenReturn(true);
    when(passwordEncoder.encode("PasswordNueva12")).thenReturn("newPasswordHash");

    useCase.execute(accountId, request);

    ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
    verify(accountRepository).save(accountCaptor.capture());
    verify(passwordEncoder).encode("PasswordNueva12");
  }

  @Test
  void execute_shouldRejectWhenCurrentPasswordDoesNotMatch() {
    UUID accountId = UUID.randomUUID();
    Account account = Account.reconstitute(
        accountId,
        "Ana",
        "Perez",
        "ana@example.com",
        "3001234567",
        LocalDateTime.now().minusDays(1),
        "oldPasswordHash",
        null);
    ChangePasswordRequest request = ChangePasswordRequest.builder()
        .currentPassword("PasswordIncorrecta12")
        .newPassword("PasswordNueva12")
        .build();

    when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
    when(passwordEncoder.matches("PasswordIncorrecta12", "oldPasswordHash")).thenReturn(false);

    assertThrows(BusinessException.class, () -> useCase.execute(accountId, request));

    verify(accountRepository, never()).save(account);
    verify(passwordEncoder, never()).encode("PasswordNueva12");
  }
}
