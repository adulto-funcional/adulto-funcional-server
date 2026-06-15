package org.adultofuncional.main.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar la Master Key de una cuenta.
 *
 * <p>
 * Recibe la clave actual para autenticar al usuario dentro del gestor y la
 * nueva clave para reemplazar el hash almacenado. El cambio recifra todas las
 * credenciales existentes, por lo que ambas claves son necesarias durante la
 * misma operación.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterKeyChangeRequest {

  /** Master Key actual en texto plano. */
  @NotBlank(message = "La clave maestra actual es obligatoria")
  @Size(min = 12, max = 24, message = "La clave maestra actual debe tener entre 12 y 24 caracteres")
  private String currentMasterKey;

  /** Nueva Master Key en texto plano. */
  @NotBlank(message = "La nueva clave maestra es obligatoria")
  @Size(min = 12, max = 24, message = "La nueva clave maestra debe tener entre 12 y 24 caracteres")
  private String newMasterKey;
}
