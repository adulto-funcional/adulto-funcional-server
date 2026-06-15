package org.adultofuncional.main.security.application.usecase;

import java.util.List;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyChangeRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.domain.model.Password;
import org.adultofuncional.main.security.domain.repository.PasswordRepository;
import org.adultofuncional.main.security.domain.service.EncryptionService;
import org.adultofuncional.main.security.domain.service.MasterKeySessionService;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Caso de uso: cambiar la Master Key de una cuenta.
 *
 * <p>
 * La Master Key protege la clave AES derivada para cada credencial almacenada.
 * Por eso el cambio no puede limitarse a reemplazar el hash: primero se
 * descifra cada credencial con la clave actual y luego se recifra con la nueva.
 * Si cualquier credencial no puede descifrarse, la transacción falla y no se
 * persiste el cambio.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Service
@RequiredArgsConstructor
public class ChangeMasterKeyUseCase {

  private final AccountRepository accountRepository;
  private final PasswordRepository passwordRepository;
  private final EncryptionService encryptionService;
  private final MasterKeySessionService masterKeySessionService;
  private final PasswordEncoder passwordEncoder;

  /**
   * Ejecuta el cambio y recifrado de credenciales.
   *
   * @param accountId identificador de la cuenta autenticada.
   * @param request   DTO con clave actual y nueva.
   * @return estado actualizado de la Master Key.
   * @throws NotFoundException     si la cuenta no existe.
   * @throws UnauthorizedException si la clave actual no coincide.
   * @throws BusinessException     si la nueva clave es igual a la actual.
   */
  @Transactional
  public MasterKeyStatusResponse execute(UUID accountId, MasterKeyChangeRequest request) {
    Account account = accountRepository.findById(accountId)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

    if (!account.hasMasterKey()) {
      throw new BusinessException("La cuenta no tiene una Master Key configurada");
    }

    if (!passwordEncoder.matches(request.getCurrentMasterKey(), account.getMasterKeyHash())) {
      throw new UnauthorizedException("Master Key actual incorrecta");
    }

    if (request.getCurrentMasterKey().equals(request.getNewMasterKey())) {
      throw new BusinessException("La nueva Master Key debe ser diferente a la actual");
    }

    List<Password> passwords = passwordRepository.findAllByAccountId(accountId);
    for (Password password : passwords) {
      String plainPassword = encryptionService.decrypt(
          password.getSalt(),
          password.getIv(),
          password.getCiphertext(),
          request.getCurrentMasterKey());

      EncryptionService.EncryptedData encryptedData = encryptionService.encrypt(
          plainPassword,
          request.getNewMasterKey());

      password.update(
          password.getApplicationName(),
          encryptedData.salt(),
          encryptedData.iv(),
          encryptedData.ciphertext(),
          password.getLastChangeDate());

      passwordRepository.save(password);
    }

    account.updateMasterKeyHash(passwordEncoder.encode(request.getNewMasterKey()));
    accountRepository.save(account);
    masterKeySessionService.verify(accountId, request.getNewMasterKey());

    return MasterKeyStatusResponse.builder()
        .hasMasterKey(true)
        .verified(true)
        .build();
  }
}
