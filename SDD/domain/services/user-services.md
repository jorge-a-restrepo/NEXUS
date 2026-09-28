# Servicios de Usuarios y Compradores

## 1. Propósito

Cubre el Dominio 1 (Administración de Usuarios) y el Dominio 2 (Gestión de Compradores): autenticación, alta de usuarios, estado operativo y perfil del comprador.

Validación crítica que atraviesa todo el subdominio (Sección 11): **el documento de identidad y el correo electrónico deben ser únicos en la plataforma**. La unicidad se comprueba siempre contra el universo completo de usuarios mediante `UserRepositoryPort`, nunca contra el conjunto de un solo rol, porque un comprador y un vendedor no podrían compartir correo.

---

## 2. LoginService

**Responsabilidad.** Autenticar a un usuario contra sus credenciales almacenadas.

**Firma.** `User execute(User user)`

**Precondiciones.** Correo y contraseña presentes en el modelo recibido.

**Flujo.**

```text
1. Validar que lleguen correo y contraseña
2. userRepositoryPort.findByEmail(user)
3. passwordServicePort.matches(contraseña recibida, contraseña almacenada)
4. Validar que el estado sea ACTIVE
5. Devolver el usuario almacenado
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Credenciales nulas | `DomainException` |
| Correo ausente o vacío | `DomainException` |
| Contraseña ausente o vacía | `DomainException` |
| Usuario inexistente | `InvalidCredentialsException` |
| Contraseña incorrecta | `InvalidCredentialsException` |
| Estado distinto de `ACTIVE` | `InvalidUserStatusException` |

**Decisión de seguridad.** Un correo inexistente y una contraseña incorrecta producen **la misma excepción y el mismo mensaje**. Distinguirlos permitiría enumerar qué cuentas existen en la plataforma.

**Regla.** RG-01. Un usuario `BLOCKED` ni siquiera obtiene sesión, de modo que el bloqueo es efectivo antes de cualquier operación.

**Nota de arquitectura.** El servicio devuelve el `User` autenticado; la emisión del token la realiza el adaptador de entrega mediante `TokenServicePort`. La tecnología del token no pertenece al dominio.

---

## 3. LogoutService

**Responsabilidad.** Cerrar la sesión del usuario autenticado.

**Firma.** `void execute(User user)`

**Flujo.** Valida estado operativo y confirma que el usuario existe.

| Condición | Excepción |
| :--- | :--- |
| Estado no operativo | `InvalidUserStatusException` |
| Usuario inexistente | `EntityNotFoundException` |

**Nota.** La invalidación efectiva del token corresponde al adaptador de entrega. El dominio solo confirma que la sesión pertenece a un usuario real y operativo.

---

## 4. RegisterBuyerUserService

**Responsabilidad.** Registrar un comprador en la plataforma.

**Firma.** `Buyer execute(Buyer buyer)`

**Sin autenticación previa**: la Sección 3.1 incluye el registro de compradores entre los procesos públicos.

**Flujo.**

```text
1. Validar nombre, correo, documento y contraseña
2. Verificar unicidad de correo en toda la plataforma
3. Verificar unicidad de documento en toda la plataforma
4. Hashear la contraseña
5. Asignar rol BUYER y estado ACTIVE
6. Guardar
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Información ausente | `DomainException` |
| Nombre, correo, documento o contraseña vacíos | `DomainException` |
| Correo ya registrado | `DomainException` |
| Documento ya registrado | `DomainException` |

**Efectos.** El rol se asigna en el servicio, no se acepta del exterior: nadie puede auto-registrarse con un rol distinto de `BUYER`. La contraseña se almacena hasheada; el texto plano nunca llega al estado persistido.

**Regla.** RG-02 (un único rol) y Sección 11 (unicidad).

---

## 5. RegisterInternalUserService

**Responsabilidad.** Dar de alta usuarios internos del Marketplace.

**Firma.** `User execute(User user, User newUser)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Roles permitidos.** `LOGISTICS_OPERATOR`, `SUPERVISOR` y `ADMINISTRATOR`.

**Roles rechazados.** `BUYER` y `SELLER`. El comprador se auto-registra y el vendedor tiene su propio servicio de incorporación junto a su primera bodega; permitir crearlos aquí saltaría esas reglas.

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Usuario solicitante no administrador | `UnauthorizedOperationException` |
| Información ausente o campos vacíos | `DomainException` |
| Rol no asignado | `DomainException` |
| Rol `BUYER` o `SELLER` | `DomainException` |
| Correo o documento ya registrados | `DomainException` |

**Efectos.** Contraseña hasheada y estado `ACTIVE`.

---

## 6. ChangeUserStatusService

**Responsabilidad.** Activar o bloquear un usuario.

**Firma.** `User execute(User user, User targetUser, UserStatus newStatus)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Flujo.** Recupera el usuario almacenado, verifica que el estado solicitado sea distinto del actual y lo actualiza.

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Usuario objetivo nulo o inexistente | `EntityNotFoundException` |
| Estado nuevo nulo | `DomainException` |
| El usuario ya tiene ese estado | `DomainException` |

**Efecto en cadena.** Bloquear a un usuario surte efecto inmediato: `ValidateUserStatusService` rechaza sus operaciones en la siguiente petición, aunque conserve un token válido.

---

## 7. ConsultUserService

**Responsabilidad.** Consultar usuarios para la administración de la plataforma.

**Firmas.**

```java
List<User> executeAll(User user);
List<User> executeByRole(User user, UserRole role);
User execute(User user, User targetUser);
```

**Autorización.** `AuthorizeAdministratorOperationService` en las tres.

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Usuario consultado inexistente | `EntityNotFoundException` |

---

## 8. ConsultBuyerProfileService

**Responsabilidad.** Devolver el perfil del comprador autenticado.

**Firma.** `Buyer execute(User user)`

**Autorización.** `AuthorizeBuyerOperationService`.

**Decisión de diseño.** No recibe identificador alguno: el perfil se resuelve del usuario autenticado. Así no existe forma de solicitar el perfil de otro comprador, en cumplimiento del Dominio 2.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Comprador inexistente | `EntityNotFoundException` |

---

## 9. UpdateBuyerProfileService

**Responsabilidad.** Actualizar los datos de contacto y entrega del comprador autenticado.

**Firma.** `Buyer execute(User user, Buyer buyer)`

**Autorización.** `AuthorizeBuyerOperationService`.

**Campos modificables.** Nombre completo, correo, dirección principal y direcciones adicionales (Dominio 2).

**Campos no modificables por esta vía.**

| Campo | Razón |
| :--- | :--- |
| `identityDocument` | Es identidad, no dato de contacto |
| `role` | RG-02: el rol no se cambia desde el perfil |
| `status` | Lo administra el Administrador |
| `commercialStatus` | Lo otorga la plataforma, tiene su propio servicio |
| `password` | Requiere un flujo propio de cambio de credenciales |

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Información ausente | `DomainException` |
| Comprador inexistente | `EntityNotFoundException` |
| Correo nuevo ya registrado por otro usuario | `DomainException` |

La unicidad del correo se vuelve a verificar solo si cambia, para no rechazar una actualización que conserva el mismo correo.

---

## 10. ChangeBuyerCommercialStatusService

**Responsabilidad.** Cambiar el estado comercial de un comprador.

**Firma.** `Buyer execute(User user, Buyer buyer)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Decisión de diseño.** El nuevo estado viaja dentro del propio `Buyer`, coherente con la regla de no pasar atributos sueltos. Al ser `CommercialStatus` una enumeración, un valor inválido no puede siquiera construirse.

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Comprador nulo o inexistente | `EntityNotFoundException` |
| Estado comercial no indicado | `DomainException` |

**Regla.** Dominio 2: el estado comercial es la condición del comprador para realizar compras, y la otorga la plataforma, no el propio comprador.

---

## 11. ConsultBuyersService

**Responsabilidad.** Listar los compradores registrados.

**Firma.** `List<Buyer> execute(User user)`

**Autorización.** `AuthorizeAdministrativeConsultationService`: Administrador (OBJ-03) y Supervisor (OBJ-12).

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `ADMINISTRATOR` o `SUPERVISOR` | `UnauthorizedOperationException` |
