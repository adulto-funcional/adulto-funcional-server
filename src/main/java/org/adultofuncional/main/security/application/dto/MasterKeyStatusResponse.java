package org.adultofuncional.main.security.application.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de respuesta con el estado de la Master Key del usuario autenticado.
 *
 * <p>
 * Permite a los clientes web y móvil decidir si deben mostrar el flujo de
 * creación, verificación o acceso directo al gestor de contraseñas.
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 */
@Getter
@Builder
public class MasterKeyStatusResponse {

  /** Indica si la cuenta ya tiene una Master Key configurada. */
  private final boolean hasMasterKey;

  /** Indica si la Master Key fue verificada en la sesión actual del gestor. */
  private final boolean verified;
}
