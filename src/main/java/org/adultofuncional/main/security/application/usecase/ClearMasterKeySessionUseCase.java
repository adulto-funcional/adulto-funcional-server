package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: cerrar la sesión interna de Master Key.
 *
 * <p>
 * Elimina la Master Key temporal almacenada para la cuenta autenticada. No
 * elimina ni modifica el hash persistido; solo bloquea el acceso al gestor
 * hasta que el usuario vuelva a verificar la clave.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ClearMasterKeySessionUseCase {

  private final AccountRepository accountRepository;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta el cierre de sesión de Master Key.
   *
   * @param accountId identificador de la cuenta autenticada.
   * @return estado actualizado después de limpiar la sesión.
   * @throws NotFoundException si la cuenta no existe.
   */
  public MasterKeyStatusResponse execute(UUID accountId) {
    boolean hasMasterKey = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId))
        .hasMasterKey();

    masterKeySessionService.clear(accountId);

    return MasterKeyStatusResponse.builder()
        .hasMasterKey(hasMasterKey)
        .verified(false)
        .build();
  }
}
