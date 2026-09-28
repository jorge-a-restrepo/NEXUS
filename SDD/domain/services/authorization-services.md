# Servicios de Autorización

## 1. Propósito

Este subdominio hace cumplir las tres Restricciones Generales de la especificación:

| Código | Restricción |
| :--- | :--- |
| RG-01 | Toda operación debe ejecutarse por un usuario autenticado |
| RG-02 | Cada usuario tendrá un único rol dentro del sistema |
| RG-03 | Ningún participante podrá administrar información fuera de su rol |

La autorización se resuelve en dos niveles distintos y complementarios:

- **Por rol**: ¿este usuario puede ejecutar esta clase de operación?
- **Por propiedad**: ¿este recurso concreto le pertenece?

El primero sin el segundo permitiría que un comprador leyera el pedido de otro comprador: el rol sería correcto, el recurso ajeno.

---

## 2. ValidateUserStatusService

**Responsabilidad.** Exige que la operación la ejecute un usuario autenticado y en estado operativo.

**Firma.** `void execute(User user)`

**Validaciones.**

| Condición | Excepción |
| :--- | :--- |
| `user` es nulo | `UnauthorizedOperationException` |
| `user.status` es nulo | `InvalidUserStatusException` |
| `user.status` distinto de `ACTIVE` | `InvalidUserStatusException` |

**Regla.** RG-01 y Dominio 1. Un usuario `BLOCKED` no ejecuta ninguna operación: la restricción se aplica en cada petición y no solo al autenticarse, de modo que bloquear a un usuario surte efecto de inmediato aunque tenga un token vigente.

---

## 3. ValidateRoleAuthorizationService

**Responsabilidad.** Exige que el usuario tenga el rol requerido por la operación.

**Firma.** `void execute(User user, UserRole requiredRole)`

**Validaciones.**

| Condición | Excepción |
| :--- | :--- |
| `user` es nulo | `UnauthorizedOperationException` |
| `user.role` es nulo | `UnauthorizedOperationException` |
| `user.role` distinto del requerido | `UnauthorizedOperationException` |

**Regla.** RG-02 y RG-03. La comparación es por igualdad exacta, no por jerarquía: no existen roles que "contengan" a otros. Un administrador no puede ejecutar operaciones de comprador.

---

## 4. Autorizadores por rol

Cinco servicios con la misma estructura: validan estado, validan rol y devuelven la especialización concreta del usuario.

| Servicio | Rol exigido | Devuelve |
| :--- | :--- | :--- |
| `AuthorizeBuyerOperationService` | `BUYER` | `Buyer` |
| `AuthorizeSellerOperationService` | `SELLER` | `Seller` |
| `AuthorizeLogisticsOperatorOperationService` | `LOGISTICS_OPERATOR` | `LogisticsOperator` |
| `AuthorizeAdministratorOperationService` | `ADMINISTRATOR` | `Administrator` |
| `AuthorizeSupervisorOperationService` | `SUPERVISOR` | `Supervisor` |

**Firma.** `<Especialización> execute(User user)`

**Flujo.**

```text
ValidateUserStatusService.execute(user)
        |
        v
ValidateRoleAuthorizationService.execute(user, ROL)
        |
        v
¿user es instancia de la especialización?
        |
        +-- no --> UnauthorizedOperationException
        |
        v
devuelve la especialización
```

**Por qué devuelven la especialización.** El servicio que llama necesita casi siempre el `Buyer` o el `Seller`, no el `User` genérico: para resolver su carrito, sus productos o sus pedidos. Devolverlo aquí evita que cada servicio repita la conversión y garantiza que esa conversión siempre vaya precedida de la validación.

La comprobación de tipo no es redundante con la del rol: protege contra un usuario cuyo rol dice `BUYER` pero cuya instancia no es un `Buyer`, situación que indicaría una inconsistencia en la reconstrucción del usuario desde el token.

---

## 5. AuthorizeInventoryOperationService

**Responsabilidad.** Autoriza las operaciones de administración de inventario.

**Firma.** `void execute(User user)`

**Validaciones.**

| Condición | Excepción |
| :--- | :--- |
| Estado no operativo | `InvalidUserStatusException` |
| Rol distinto de `SELLER` y `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |

**Regla.** La Matriz de Responsabilidades (§12) marca "Administración Inventario" para el Vendedor **y** el Operador Logístico. Los autorizadores de rol único no sirven aquí, por eso existe este servicio.

---

## 6. AuthorizeAdministrativeConsultationService

**Responsabilidad.** Autoriza consultas consolidadas de información operativa.

**Firma.** `void execute(User user)`

**Validaciones.**

| Condición | Excepción |
| :--- | :--- |
| Estado no operativo | `InvalidUserStatusException` |
| Rol distinto de `ADMINISTRATOR` y `SUPERVISOR` | `UnauthorizedOperationException` |

**Regla.** OBJ-12 asigna al Administrador la consolidación de información administrativa, y la Sección 5 define al Supervisor como perfil de consulta y seguimiento. Ambos leen; **ninguno modifica** a través de los servicios que usan esta autorización.

---

## 7. ValidateBuyerOwnershipService

**Responsabilidad.** Verifica que el recurso pertenezca al comprador que lo solicita.

**Firmas.**

```java
void execute(Buyer buyer, Cart cart);
void execute(Buyer buyer, Order order);
void execute(Buyer buyer, Invoice invoice);
void execute(Buyer buyer, ReturnRefund returnRefund);
```

**Resolución del dueño.**

| Recurso | Dueño |
| :--- | :--- |
| `Cart` | `cart.buyer` |
| `Order` | `order.buyer` |
| `Invoice` | `invoice.order.buyer` |
| `ReturnRefund` | `returnRefund.order.buyer` |

La comparación es por `identifier`, que es el atributo de identidad de la jerarquía `User`.

**Validaciones.**

| Condición | Excepción |
| :--- | :--- |
| Recurso nulo | `EntityNotFoundException` |
| Pedido asociado ausente (factura o devolución) | `EntityNotFoundException` |
| Comprador solicitante sin identificar | `OwnershipViolationException` |
| Dueño del recurso sin identificar | `OwnershipViolationException` |
| Identificadores distintos | `OwnershipViolationException` |

**Regla.** Dominio 2: "El comprador nunca administrará información de otros compradores ni inventarios". La validación **falla cerrado**: si no puede identificar a alguna de las dos partes, rechaza. Un control de propiedad que ante la duda permite el acceso no es un control.

---

## 8. ValidateSellerOwnershipService

**Responsabilidad.** Verifica que el producto o la bodega pertenezcan al vendedor que opera sobre ellos.

**Firmas.**

```java
void execute(Seller seller, Product product);
void execute(Seller seller, Warehouse warehouse);
```

**Resolución del dueño.** `product.seller` y `warehouse.seller`. La comparación es por `identifier`, igual que en el caso del comprador, y también falla cerrado.

**Regla.** RG-03 y Dominio 3. Es el servicio que impide que un vendedor publique, suspenda o descontinúe el producto de otro, y se invoca en los cuatro servicios del ciclo de vida del producto sobre la entidad **almacenada**, no sobre la que llegó como parámetro.

---

## 9. Dónde se aplica cada uno

| Servicio de autorización | Invocado desde |
| :--- | :--- |
| `AuthorizeBuyerOperationService` | Carrito, pedidos propios, facturas propias, devoluciones, perfil del comprador |
| `AuthorizeSellerOperationService` | Catálogo propio, bodegas propias, pedidos del vendedor |
| `AuthorizeLogisticsOperatorOperationService` | Envíos, devolución al inventario, cola de despacho |
| `AuthorizeAdministratorOperationService` | Alta de vendedores y usuarios, bodegas, estados, devoluciones |
| `AuthorizeInventoryOperationService` | Ingreso, ajuste y consulta de inventario |
| `AuthorizeAdministrativeConsultationService` | Consolidados de pedidos, inventario, envíos, devoluciones, compradores y vendedores |
| `ValidateBuyerOwnershipService` | `CheckoutCartService`, `ConfirmOrderPaymentService`, `ConsultOrderService`, `ConsultInvoiceService`, `RequestReturnService`, servicios de carrito |
| `ValidateSellerOwnershipService` | `UpdateProductService`, `PublishProductService`, `SuspendProductService`, `DiscontinueProductService` |
