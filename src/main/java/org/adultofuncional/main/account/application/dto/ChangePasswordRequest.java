package org.adultofuncional.main.account.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para cambiar la contraseña de inicio de sesión de una cuenta.
 *
 * <p>
 * Recibe la contraseña actual para validar la identidad del usuario y la nueva
 * contraseña para reemplazar el hash almacenado. Ninguno de estos valores se
 * devuelve en respuestas ni debe registrarse en logs.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

  /** Contraseña actual en texto plano, usada solo durante la petición. */
  @NotBlank(message = "La contraseña actual es obligatoria")
  @Size(min = 8, max = 24, message = "La contraseña actual debe tener entre 8 y 24 caracteres")
  private String currentPassword;

  /** Nueva contraseña en texto plano, usada solo para generar el nuevo hash. */
  @NotBlank(message = "La nueva contraseña es obligatoria")
  @Size(min = 8, max = 24, message = "La nueva contraseña debe tener entre 8 y 24 caracteres")
  private String newPassword;
}
