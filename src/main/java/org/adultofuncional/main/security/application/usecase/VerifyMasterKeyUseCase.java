package org.adultofuncional.main.security.application.usecase;

import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: verificar la Master Key del gestor de contraseñas.
 *
 * <p>
 * Compara la clave recibida con el hash Argon2 almacenado en la cuenta. Si
 * coincide, mantiene la Master Key temporalmente en la sesión interna del
 * gestor para operaciones de cifrado y descifrado.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class VerifyMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final MasterKeySessionService masterKeySessionService;

  /**
   * Ejecuta la verificación de la Master Key.
   *
   * @param accountId identificador de la cuenta autenticada.
   * @param request   DTO con la Master Key en texto plano.
   * @return estado actualizado de la Master Key.
   * @throws NotFoundException     si la cuenta no existe.
   * @throws UnauthorizedException si la cuenta no tiene Master Key o si la
   *                               clave recibida no coincide.
   */
  @Transactional(readOnly = true)
  public MasterKeyStatusResponse execute(UUID accountId, MasterKeyRequest request) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!account.hasMasterKey()) {
      throw new UnauthorizedException("La cuenta no tiene una Master Key configurada");
    }

    if (!passwordEncoder.matches(request.getMasterKey(), account.getMasterKeyHash())) {
      throw new UnauthorizedException("Master Key incorrecta");
    }

    masterKeySessionService.verify(accountId, request.getMasterKey());

    return MasterKeyStatusResponse.builder()
        .hasMasterKey(true)
        .verified(true)
        .build();
  }
}
