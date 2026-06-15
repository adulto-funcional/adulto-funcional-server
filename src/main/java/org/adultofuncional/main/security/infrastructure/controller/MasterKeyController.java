package org.adultofuncional.main.security.infrastructure.controller;

import java.util.UUID;

import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.security.application.dto.MasterKeyChangeRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyRequest;
import org.adultofuncional.main.security.application.dto.MasterKeyStatusResponse;
import org.adultofuncional.main.security.application.usecase.ChangeMasterKeyUseCase;
import org.adultofuncional.main.security.application.usecase.ClearMasterKeySessionUseCase;
import org.adultofuncional.main.security.application.usecase.CreateMasterKeyUseCase;
import org.adultofuncional.main.security.application.usecase.GetMasterKeyStatusUseCase;
import org.adultofuncional.main.security.application.usecase.VerifyMasterKeyUseCase;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.adultofuncional.main.shared.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para el ciclo de vida de la Master Key.
 *
 * <p>
 * Expone operaciones independientes del CRUD de credenciales para que los
 * clientes puedan consultar, crear, verificar, cambiar y cerrar la sesión de
 * la Master Key después de registrar una cuenta. La ruta base es
 * {@code /api/security/master-key}.
 *
 * <p>
 * La Master Key permite acceder al gestor de contraseñas. Su hash Argon2 se
 * almacena en la cuenta, pero el valor en texto plano solo vive durante la
 * petición o en la sesión interna temporal del gestor.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@RestController
@RequestMapping("/api/security/master-key")
@RequiredArgsConstructor
public class MasterKeyController {

  private final AccountRepository accountRepository;
  private final GetMasterKeyStatusUseCase getMasterKeyStatusUseCase;
  private final CreateMasterKeyUseCase createMasterKeyUseCase;
  private final VerifyMasterKeyUseCase verifyMasterKeyUseCase;
  private final ChangeMasterKeyUseCase changeMasterKeyUseCase;
  private final ClearMasterKeySessionUseCase clearMasterKeySessionUseCase;

  /**
   * Resuelve el UUID de cuenta desde el correo autenticado en Spring Security.
   *
   * @param email correo del principal autenticado.
   * @return UUID de la cuenta.
   * @throws NotFoundException si la cuenta no existe.
   */
  private UUID resolveAccountId(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new NotFoundException("Cuenta no encontrada para el email: " + email))
        .getId();
  }

  /**
   * Consulta si la cuenta tiene Master Key y si está verificada en la sesión.
   *
   * @param loggedEmail correo del usuario autenticado.
   * @return estado de configuración y sesión.
   */
  @GetMapping("/status")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> getStatus(
      @AuthenticationPrincipal String loggedEmail) {

    MasterKeyStatusResponse response = getMasterKeyStatusUseCase.execute(resolveAccountId(loggedEmail));

    return ResponseEntity.ok(ApiResponse.<MasterKeyStatusResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Estado de Master Key obtenido exitosamente")
        .data(response)
        .build());
  }

  /**
   * Crea la Master Key para una cuenta que todavía no la tiene configurada.
   *
   * @param request     DTO con la Master Key en texto plano.
   * @param loggedEmail correo del usuario autenticado.
   * @return estado actualizado.
   */
  @PostMapping
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> create(
      @Validated @RequestBody MasterKeyRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    MasterKeyStatusResponse response = createMasterKeyUseCase.execute(resolveAccountId(loggedEmail), request);

    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<MasterKeyStatusResponse>builder()
        .status(HttpStatus.CREATED.value())
        .message("Master Key creada exitosamente")
        .data(response)
        .build());
  }

  /**
   * Verifica la Master Key y habilita la sesión interna del gestor.
   *
   * @param request     DTO con la Master Key en texto plano.
   * @param loggedEmail correo del usuario autenticado.
   * @return estado actualizado.
   */
  @PostMapping("/verify")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> verify(
      @Validated @RequestBody MasterKeyRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    MasterKeyStatusResponse response = verifyMasterKeyUseCase.execute(resolveAccountId(loggedEmail), request);

    return ResponseEntity.ok(ApiResponse.<MasterKeyStatusResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Master Key verificada exitosamente")
        .data(response)
        .build());
  }

  /**
   * Cambia la Master Key y recifra las credenciales existentes.
   *
   * @param request     DTO con clave actual y nueva.
   * @param loggedEmail correo del usuario autenticado.
   * @return estado actualizado.
   */
  @PatchMapping
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> change(
      @Validated @RequestBody MasterKeyChangeRequest request,
      @AuthenticationPrincipal String loggedEmail) {

    MasterKeyStatusResponse response = changeMasterKeyUseCase.execute(resolveAccountId(loggedEmail), request);

    return ResponseEntity.ok(ApiResponse.<MasterKeyStatusResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Master Key actualizada exitosamente")
        .data(response)
        .build());
  }

  /**
   * Cierra la sesión interna de Master Key.
   *
   * @param loggedEmail correo del usuario autenticado.
   * @return estado actualizado.
   */
  @DeleteMapping("/session")
  public ResponseEntity<ApiResponse<MasterKeyStatusResponse>> clearSession(
      @AuthenticationPrincipal String loggedEmail) {

    MasterKeyStatusResponse response = clearMasterKeySessionUseCase.execute(resolveAccountId(loggedEmail));

    return ResponseEntity.ok(ApiResponse.<MasterKeyStatusResponse>builder()
        .status(HttpStatus.OK.value())
        .message("Sesión de Master Key cerrada exitosamente")
        .data(response)
        .build());
  }
}
