# Seguridad: autenticación con JWT y autorización por rol

## 1. Propósito

Cumple **RG-01** (toda operación requiere un usuario autenticado) y **RG-02** (cada usuario tiene un único rol). Los adaptadores de seguridad viven en `application/adapters/security/`. El dominio no depende de Spring Security ni de jjwt: solo conoce `PasswordServicePort` y `TokenServicePort`.

## 2. Componentes

| Clase | Rol | Implementa |
| :--- | :--- | :--- |
| `BCryptPasswordServiceAdapter` | Hash y verificación de contraseñas con BCrypt | `PasswordServicePort` |
| `JwtProvider` | Emite, valida y lee tokens JWT (HS256, librería jjwt 0.12.6) | `TokenServicePort` |
| `mappers/JwtClaimsMapper` | Convierte `User` ↔ claims del token | — |
| `dtos/AuthenticatedUserPrincipal` | Envuelve el `User` reconstruido para Spring Security (`ROLE_<rol>`) | `UserDetails` |
| `JwtAuthenticationFilter` | Lee `Authorization: Bearer <token>` en cada petición y arma el contexto de seguridad | `OncePerRequestFilter` |
| `SecurityConfig` | Reglas por ruta y rol, sesión sin estado, bean `PasswordEncoder` | — |

## 3. Estructura del token

Un JWT son tres cadenas separadas por puntos: **header** (algoritmo HS256), **payload** (claims) y **signature** (HMAC del header y el payload con el secreto).

| Claim | Contenido |
| :--- | :--- |
| `sub` | `User.identifier` |
| `jti` | UUID único del token |
| `iat` / `exp` | Emisión y expiración (`jwt.expiration-ms`, por defecto 3 600 000 ms = 1 hora) |
| `role` | `UserRole` |
| `status` | `UserStatus` |
| `email`, `fullName` | Datos de identificación del usuario |
| `commercialStatus` | Solo si el usuario es `Buyer` |

**Decisión.** El token no incluye documento de identidad, direcciones ni contraseña: cualquiera que tenga el token puede leer su payload.

**Reconstrucción por subclase.** `JwtClaimsMapper.toDomain` crea la subclase que corresponde al rol (`BUYER` → `Buyer`, `SELLER` → `Seller`, `LOGISTICS_OPERATOR` → `LogisticsOperator`, `ADMINISTRATOR` → `Administrator`, `SUPERVISOR` → `Supervisor`), porque los servicios de autorización verifican la subclase (por ejemplo, `user instanceof Buyer`).

## 4. Flujo de login

```text
POST /api/v1/auth/login {email, password}
  → AuthRestController → UserRestMapper.toDomain
  → PublicAccessPort.login → PublicAccessUseCaseImpl → LoginService
      1. userRepositoryPort.findByEmail
      2. passwordServicePort.matches   (BCrypt)
      3. estado ACTIVE
      4. tokenServicePort.generateToken
  ← AuthenticationResult(user, token)
  ← 200 {token, tokenType: "Bearer", expiresIn, user}
```

## 5. Flujo de una petición protegida

```text
GET /api/v1/buyer/profile   Authorization: Bearer <token>
  → JwtAuthenticationFilter: extrae el token → validateToken → reconstructUser
      → si status = ACTIVE: AuthenticatedUserPrincipal en el SecurityContext
  → SecurityConfig: /api/v1/buyer/** exige ROLE_BUYER (si no, 403)
  → BuyerRestController recibe @AuthenticationPrincipal(expression = "user") User
  → BuyerPort.consultMyProfile(user) → servicios de dominio (vuelven a validar rol, estado y propiedad)
```

## 6. Reglas por ruta

| Ruta | Acceso |
| :--- | :--- |
| `/api/v1/auth/**` | Público (login, logout, registro de comprador) |
| `/api/v1/public/**` | Público (catálogo, solo `PUBLISHED`) |
| `/actuator/health`, `/error` | Público |
| `/api/v1/buyer/**` | `BUYER` |
| `/api/v1/seller/**` | `SELLER` |
| `/api/v1/logistics-operator/**` | `LOGISTICS_OPERATOR` |
| `/api/v1/administrator/**` | `ADMINISTRATOR` |
| `/api/v1/supervisor/**` | `SUPERVISOR` |
| Cualquier otra | Autenticado |

## 7. Respuestas

| Situación | HTTP |
| :--- | :--- |
| Credenciales incorrectas | 401 (`InvalidCredentialsException`) |
| Sin token, token inválido o expirado en ruta protegida | 403 |
| Token válido pero rol distinto al de la ruta | 403 |
| Usuario no `ACTIVE` | 403 |

## 8. Configuración

| Propiedad | Variable en Docker | Valor por defecto |
| :--- | :--- | :--- |
| `jwt.secret` | `JWT_SECRET` | Valor académico de al menos 32 bytes (público, solo para el curso) |
| `jwt.expiration-ms` | `JWT_EXPIRATION_MS` | `3600000` |

## 9. Decisiones y supuestos

- **Logout sin estado (SUPUESTO).** El token no se invalida en el servidor; el cliente lo descarta y expira solo. Invalidarlo exigiría un atributo de versión de token en `User` que la especificación no define.
- **Sin consulta a la base de datos por petición.** El usuario se reconstruye desde el token; un cambio de rol o de estado aplica desde el siguiente login. Los servicios que necesitan datos completos los cargan con el `identifier` del token.
