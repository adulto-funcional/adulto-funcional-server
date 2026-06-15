# Seguridad y Master Key

Autor: Miguel Angel Blandon Montes

## 1. Qué es la Master Key

La **Master Key** es una clave independiente de la contraseña de inicio de
sesión. Su función es proteger el acceso al gestor de contraseñas y derivar la
clave AES usada para cifrar cada credencial guardada.

El sistema maneja dos credenciales distintas:

| Credencial | Para qué sirve | Cómo se almacena |
| ---------- | -------------- | ---------------- |
| Contraseña de login | Autenticar al usuario en la aplicación | Hash Argon2 en `accounts.account_password` |
| Master Key | Desbloquear y cifrar el gestor de contraseñas | Hash Argon2 en `accounts.account_master_key` |

La Master Key **nunca se devuelve al cliente** y **nunca se guarda en texto
plano en MariaDB**. Después de una verificación correcta, se mantiene
temporalmente en el servicio de sesión del gestor para poder cifrar o descifrar
credenciales durante la sesión activa.

## 2. Para qué sirve

La Master Key permite separar el acceso general a la cuenta del acceso al
gestor de contraseñas.

Esto evita que un usuario autenticado con JWT pueda leer credenciales guardadas
sin un segundo factor lógico. Aunque el JWT sea válido, el backend exige que la
Master Key esté verificada antes de crear, listar, ver, actualizar o eliminar
contraseñas almacenadas.

## 3. Cómo funciona

### 3.1 Creación

Una cuenta puede registrarse con o sin Master Key.

- Si el usuario la envía durante el registro, se guarda su hash Argon2.
- Si no la envía, puede configurarla después con el endpoint dedicado.

Flujo posterior al registro:

```text
Cliente autenticado
  -> POST /api/security/master-key
  -> backend hashea la Master Key con Argon2
  -> guarda el hash en accounts.account_master_key
  -> marca la Master Key como verificada en sesión
```

### 3.2 Verificación

Antes de usar el gestor de contraseñas, el usuario debe verificar la Master Key.

```text
Cliente autenticado
  -> POST /api/security/master-key/verify
  -> backend compara la clave recibida contra el hash Argon2
  -> si coincide, guarda temporalmente la Master Key en la sesión del gestor
```

### 3.3 Cifrado de credenciales

Cuando se guarda una contraseña externa:

1. El backend toma la Master Key verificada en sesión.
2. Genera un salt aleatorio por credencial.
3. Deriva una clave AES-256 con PBKDF2-HMAC-SHA256.
4. Genera un IV aleatorio.
5. Cifra la contraseña con AES-GCM.
6. Guarda `salt`, `iv` y `ciphertext` en la tabla `passwords`.

La contraseña en texto plano solo existe durante la ejecución de la petición.

### 3.4 Cambio de Master Key

Cambiar la Master Key requiere conocer la clave actual.

El backend no puede reemplazar solamente el hash, porque las credenciales
existentes fueron cifradas con una clave derivada desde la Master Key anterior.
Por eso el cambio ejecuta un recifrado completo:

```text
PATCH /api/security/master-key
  -> verifica Master Key actual
  -> lista credenciales de la cuenta
  -> descifra cada credencial con la clave actual
  -> cifra cada credencial con la clave nueva
  -> actualiza hash Argon2 de la nueva Master Key
  -> marca la nueva Master Key como verificada
```

Si alguna credencial no puede descifrarse, la transacción falla y no se persiste
el cambio.

### 3.5 Cierre de sesión del gestor

El cierre de sesión de Master Key elimina la clave temporal del servicio de
sesión del gestor, pero no elimina el hash almacenado ni las credenciales.

```text
DELETE /api/security/master-key/session
```

Después de cerrarla, el usuario debe verificar nuevamente la Master Key antes
de usar el gestor.

## 4. Endpoints

Todos los endpoints requieren JWT válido.

| Método | Ruta | Descripción |
| ------ | ---- | ----------- |
| `GET` | `/api/security/master-key/status` | Indica si la cuenta tiene Master Key y si está verificada en sesión |
| `POST` | `/api/security/master-key` | Crea la Master Key después del registro |
| `POST` | `/api/security/master-key/verify` | Verifica la Master Key y desbloquea el gestor |
| `PATCH` | `/api/security/master-key` | Cambia la Master Key y recifra credenciales |
| `DELETE` | `/api/security/master-key/session` | Cierra la sesión interna de Master Key |

Existe además un endpoint de compatibilidad:

| Método | Ruta | Estado |
| ------ | ---- | ------ |
| `POST` | `/api/security/passwords/master-key/verify` | Compatible con clientes antiguos; delega al flujo nuevo |

## 5. Contratos

### Crear o verificar Master Key

Request:

```json
{
  "masterKey": "ClaveMaestra12"
}
```

Response:

```json
{
  "status": 200,
  "message": "Master Key verificada exitosamente",
  "data": {
    "hasMasterKey": true,
    "verified": true
  }
}
```

### Cambiar Master Key

Request:

```json
{
  "currentMasterKey": "ClaveActual12",
  "newMasterKey": "ClaveNueva123"
}
```

Response:

```json
{
  "status": 200,
  "message": "Master Key actualizada exitosamente",
  "data": {
    "hasMasterKey": true,
    "verified": true
  }
}
```

## 6. Reglas de error

| Caso | Código | Mensaje esperado |
| ---- | ------ | ---------------- |
| Crear Master Key cuando ya existe | `409` | `La cuenta ya tiene una Master Key configurada` |
| Verificar una cuenta sin Master Key | `401` | `La cuenta no tiene una Master Key configurada` |
| Master Key incorrecta | `401` | `Master Key incorrecta` |
| Cambiar con clave actual incorrecta | `401` | `Master Key actual incorrecta` |
| Cambiar usando la misma clave | `400` | `La nueva Master Key debe ser diferente a la actual` |
| Usar gestor sin verificar Master Key | `403` | `Master Key no verificada` |

## 7. Nota sobre recuperación

La Master Key protege material cifrado. Si el usuario olvida la Master Key, el
backend no puede descifrar las credenciales existentes porque no conoce la clave
en texto plano.

Por seguridad, un flujo de recuperación futuro debe elegir explícitamente entre:

- exigir la Master Key actual para conservar y recifrar credenciales;
- o permitir un reset destructivo que elimine las credenciales cifradas antiguas.

No debe prometer recuperación de contraseñas guardadas sin la Master Key
original.
