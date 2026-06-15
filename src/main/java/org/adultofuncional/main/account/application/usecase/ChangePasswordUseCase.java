package org.adultofuncional.main.account.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.application.dto.ChangePasswordRequest;
import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso para cambiar la contraseña de inicio de sesión de una cuenta.
 *
 * <p>
 * La operación exige conocer la contraseña actual. Si la contraseña actual no
 * coincide con el hash almacenado, no se modifica la cuenta. La nueva
 * contraseña se persiste únicamente como hash Argon2.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Cambia la contraseña de una cuenta existente.
   *
   * @param accountId identificador de la cuenta propietaria.
   * @param request   contraseñas actual y nueva.
   * @throws NotFoundException si la cuenta no existe.
   * @throws BusinessException si la contraseña actual no coincide.
   */
  @Transactional
  public void execute(UUID accountId, ChangePasswordRequest request) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPasswordHash())) {
      throw new BusinessException("La contraseña actual es incorrecta");
    }

    account.updatePasswordHash(passwordEncoder.encode(request.getNewPassword()));
    accountRepository.save(account);
  }
}
