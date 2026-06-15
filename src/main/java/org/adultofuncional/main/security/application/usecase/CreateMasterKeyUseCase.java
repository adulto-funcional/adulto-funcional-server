package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.ConflictException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: crear una Master Key después del registro.
 *
 * <p>
 * Permite que una cuenta registrada sin Master Key active posteriormente el
 * gestor de contraseñas. La clave se guarda solo como hash Argon2 y, tras la
 * creación exitosa, se marca como verificada para que el usuario pueda empezar
 * a crear credenciales sin repetir el paso.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class CreateMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta la creación de la Master Key.
   *
   * @param accountId identificador de la cuenta autenticada.
   * @param request   DTO con la Master Key en texto plano.
   * @return estado actualizado de la Master Key.
   * @throws NotFoundException si la cuenta no existe.
   * @throws ConflictException si la cuenta ya tiene Master Key configurada.
   */
  @Transactional
  public MasterKeyStatusResponse execute(UUID accountId, MasterKeyRequest request) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (account.hasMasterKey()) {
      throw new ConflictException("La cuenta ya tiene una Master Key configurada");
    }

    account.updateMasterKeyHash(passwordEncoder.encode(request.getMasterKey()));
    accountRepository.save(account);
    masterKeySessionService.verify(accountId, request.getMasterKey());

    return MasterKeyStatusResponse.builder()
        .hasMasterKey(true)
        .verified(true)
        .build();
  }
}
