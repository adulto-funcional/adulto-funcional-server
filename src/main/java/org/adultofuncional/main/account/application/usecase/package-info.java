/**
 * Casos de uso del módulo de cuentas de usuario.
 *
 * <p>
 * Contiene los servicios de aplicación que orquestan las operaciones de
 * consulta, actualización y eliminación de cuentas. Cada caso de uso
 * coordina el dominio
 * ({@link org.adultofuncional.main.account.domain.model.Account})
 * y el puerto de repositorio
 * ({@link org.adultofuncional.main.account.domain.repository.AccountRepository})
 * sin depender de la infraestructura REST o JPA.
 *
 * <h2>Casos de uso incluidos</h2>
 * <ul>
 * <li>{@link org.adultofuncional.main.account.application.usecase.GetAccountUseCase}
 * —
 * Obtiene los datos no sensibles de una cuenta por su ID.</li>
 * <li>{@link org.adultofuncional.main.account.application.usecase.UpdateAccountUseCase}
 * —
 * Actualiza nombres, apellidos, teléfono y email de una cuenta, validando la
 * unicidad del nuevo email.</li>
 * <li>{@link org.adultofuncional.main.account.application.usecase.ChangePasswordUseCase}
 * —
 * Cambia la contraseña de inicio de sesión validando la contraseña actual y
 * persistiendo solo el nuevo hash Argon2.</li>
 * <li>{@link org.adultofuncional.main.account.application.usecase.DeleteAccountUseCase}
 * —
 * Elimina una cuenta y todos sus datos asociados en cascada (movimientos,
 * gastos fijos, eventos, contraseñas).</li>
 * </ul>
 *
 * <h2>Consideraciones</h2>
 * <ul>
 * <li>Todas las operaciones de escritura ({@code update}, {@code delete}) se
 * ejecutan con {@code @Transactional} para garantizar atomicidad.</li>
 * <li>La validación de ownership se realiza en el controlador antes de invocar
 * estos casos de uso.</li>
 * <li>Los datos sensibles nunca se exponen. La contraseña solo se modifica a
 * través de {@code ChangePasswordUseCase}; la Master Key se modifica en el
 * módulo de seguridad.</li>
 * </ul>
 *
 * @author Miguel Angel Blandon Montes
 * @since 0.0.1
 * @see org.adultofuncional.main.account.application.dto
 * @see org.adultofuncional.main.account.domain.model.Account
 * @see org.adultofuncional.main.account.domain.repository.AccountRepository
 */
package org.adultofuncional.main.account.application.usecase;
