package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para operaciones que reciben una Master Key en texto plano.
 *
 * <p>
 * Se usa al crear o verificar la Master Key del gestor de contraseñas. La clave
 * se usa solo durante la petición: se hashea con Argon2 cuando se persiste como
 * configuración de cuenta, o se mantiene temporalmente en la sesión interna del
 * gestor cuando la verificación es exitosa.
 *
 * <p>
 * No utiliza {@code @NoHtml} porque una Master Key puede contener caracteres
 * como {@code <} o {@code >}; además, este valor nunca se renderiza ni se
 * devuelve al cliente.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterKeyRequest {

  /**
   * Master Key ingresada por el usuario.
   *
   * <p>
   * Debe mantener el mismo rango usado durante registro para no crear reglas
   * divergentes entre clientes.
   */
  @NotBlank(message = "La clave maestra es obligatoria")
  @Size(min = 12, max = 24, message = "La clave maestra debe tener entre 12 y 24 caracteres")
  private String masterKey;
}
