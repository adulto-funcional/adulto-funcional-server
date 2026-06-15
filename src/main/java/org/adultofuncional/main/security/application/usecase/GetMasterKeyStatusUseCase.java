package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: consultar el estado de la Master Key de una cuenta.
 *
 * <p>
 * Informa si la cuenta ya tiene una Master Key configurada y si fue verificada
 * en la sesión actual del gestor. No expone hashes, claves en texto plano ni
 * material criptográfico.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class GetMasterKeyStatusUseCase {

  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta la consulta de estado.
   *
   * @param accountId identificador de la cuenta autenticada.
   * @return estado de configuración y sesión de la Master Key.
   * @throws NotFoundException si la cuenta no existe.
   */
  @Transactional(readOnly = true)
  public MasterKeyStatusResponse execute(UUID accountId) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    return MasterKeyStatusResponse.builder()
        .hasMasterKey(account.hasMasterKey())
        .verified(account.hasMasterKey() && masterKeySessionService.isVerified(accountId))
        .build();
  }
}
