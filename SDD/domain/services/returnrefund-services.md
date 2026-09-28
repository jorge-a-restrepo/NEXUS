# Servicios de Devoluciones y Reembolsos

## 1. Propósito

Cubre el OBJ-11 (Administrar devoluciones y reembolsos) y la posventa del Marketplace.

La Matriz de Responsabilidades reparte el proceso entre dos roles: **"Gestión Reembolsos" marca Comprador y Administrador**. El comprador solicita; el administrador decide y liquida.

```text
REQUESTED --aprobar--> APPROVED --reembolsar--> REFUNDED
     |
     +---rechazar---> REJECTED
```

**Regla transversal.** Ninguno de estos servicios modifica el pedido. La Sección 11 establece que un pedido finalizado no se modifica bajo ninguna circunstancia, así que la devolución vive en su propia entidad y el pedido permanece `DELIVERED` e intacto.

---

## 2. RequestReturnService

**Responsabilidad.** Registrar la solicitud de devolución de un comprador.

**Firma.** `ReturnRefund execute(User user, ReturnRefund returnRefund)`

**Autorización.** `AuthorizeBuyerOperationService` + `ValidateBuyerOwnershipService`.

**Flujo.**

```text
1. Autorizar comprador
2. Validar que llegue el pedido y el motivo
3. Recuperar el pedido almacenado y validar propiedad
4. Rechazar si el pedido no está DELIVERED
5. Rechazar si ya existe una solicitud para ese pedido
6. Crear la solicitud en estado REQUESTED, sin monto reembolsado
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Solicitud sin pedido | `DomainException` |
| Motivo ausente o vacío | `DomainException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido de otro comprador | `OwnershipViolationException` |
| Pedido no `DELIVERED` | `DomainException` |
| Ya existe una solicitud para ese pedido | `DomainException` |

**Decisiones de diseño.**

*Solo pedidos entregados.* Devolver algo que aún no se recibió no tiene sentido de negocio; un pedido no entregado se gestiona por la vía logística, no por la de posventa.

*Una solicitud por pedido.* Evita solicitudes duplicadas que podrían derivar en dos reembolsos sobre la misma compra.

*El monto se ignora al solicitar.* Aunque llegue un valor en el parámetro, se descarta: el monto lo determina el Administrador al liquidar, contra la factura. El comprador no define cuánto se le devuelve.

---

## 3. ApproveReturnService

**Responsabilidad.** Aprobar una solicitud de devolución.

**Firma.** `ReturnRefund execute(User user, ReturnRefund returnRefund)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Transición.** `REQUESTED --> APPROVED`

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `ADMINISTRATOR` | `UnauthorizedOperationException` |
| Solicitud sin pedido | `DomainException` |
| Solicitud inexistente | `EntityNotFoundException` |
| Estado distinto de `REQUESTED` | `DomainException` |

**Nota.** La aprobación no reingresa mercancía al inventario. Ese reingreso lo ejecuta el Operador Logístico con `RegisterInventoryReturnService` cuando la mercancía llega físicamente a la bodega: aprobar y recibir son hechos distintos y pueden estar separados por días.

---

## 4. RejectReturnService

**Responsabilidad.** Rechazar una solicitud de devolución.

**Firma.** `ReturnRefund execute(User user, ReturnRefund returnRefund)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Transición.** `REQUESTED --> REJECTED`

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `ADMINISTRATOR` | `UnauthorizedOperationException` |
| Solicitud sin pedido | `DomainException` |
| Solicitud inexistente | `EntityNotFoundException` |
| Estado distinto de `REQUESTED` | `DomainException` |

**Efectos.** Ninguno sobre el inventario ni sobre el dinero. `REJECTED` es terminal: una solicitud rechazada no se reabre, se presenta una nueva si corresponde.

---

## 5. ProcessRefundService

**Responsabilidad.** Liquidar el reembolso de una devolución aprobada.

**Firma.** `ReturnRefund execute(User user, ReturnRefund returnRefund)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Transición.** `APPROVED --> REFUNDED`

**Flujo.**

```text
1. Autorizar administrador
2. Recuperar la solicitud almacenada
3. Rechazar si no está APPROVED
4. Recuperar la factura del pedido
5. Determinar el monto:
        +-- si se indicó uno, usarlo
        +-- si no, reembolsar el total facturado
6. Validar que sea mayor que cero y no supere lo facturado
7. Fijar el monto y pasar a REFUNDED
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `ADMINISTRATOR` | `UnauthorizedOperationException` |
| Solicitud sin pedido | `DomainException` |
| Solicitud inexistente | `EntityNotFoundException` |
| Estado distinto de `APPROVED` | `DomainException` |
| Pedido sin factura | `EntityNotFoundException` |
| Monto facturado inválido | `DomainException` |
| Monto a reembolsar menor o igual a cero | `DomainException` |
| **Monto a reembolsar mayor que el facturado** | `DomainException` |

**Regla clave.** El monto se valida **contra la factura**, no contra un valor libre. Es la protección financiera del subdominio: no se puede devolver más dinero del que efectivamente se cobró. Admitir un monto parcial cubre la devolución de parte de un pedido; omitirlo reembolsa el total.

**Orden obligatorio.** Solo se reembolsa lo aprobado. No existe camino de `REQUESTED` directo a `REFUNDED`: la decisión del Administrador es un paso separado y explícito.

---

## 6. ConsultReturnService

**Responsabilidad.** Consultar devoluciones según el rol.

**Firmas.**

```java
List<ReturnRefund> executeMyReturns(User user);
List<ReturnRefund> executePending(User user);
List<ReturnRefund> executeAll(User user);
```

**Autorización.**

| Método | Autorización | Alcance |
| :--- | :--- | :--- |
| `executeMyReturns` | `AuthorizeBuyerOperationService` | Solo las del comprador autenticado (Dominio 2) |
| `executePending` | `AuthorizeAdministrativeConsultationService` | Las que están en `REQUESTED` |
| `executeAll` | `AuthorizeAdministrativeConsultationService` | Todas (OBJ-12) |

`executePending` es la bandeja de trabajo del Administrador: las solicitudes que esperan decisión.

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado para el alcance pedido | `UnauthorizedOperationException` |

**Nota.** El listado del comprador consulta por el comprador autenticado, así que las devoluciones ajenas nunca se cargan en memoria.
