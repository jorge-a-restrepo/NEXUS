# Servicios de Pedidos y Facturación

## 1. Propósito

Cubre el Dominio 7 (Gestión de Pedidos), el OBJ-08 y el OBJ-09.

El pedido es el **compromiso comercial formal** entre comprador y Marketplace, y su ciclo de vida es el proceso central del sistema.

```text
PENDING_PAYMENT --> PAID --> SHIPPED --> DELIVERED
                     |                    (terminal)
                     +-- pedido íntegramente digital --> DELIVERED
```

**Validación crítica (Sección 11): un pedido finalizado no podrá ser modificado bajo ninguna circunstancia.** Ningún servicio del sistema transiciona un pedido fuera de `DELIVERED`, y los tres servicios que cambian estado lo verifican antes de tocar nada.

Cada transición valida el estado de origen exacto: no existe forma de saltar de `PENDING_PAYMENT` a `SHIPPED`.

---

## 2. ConfirmOrderPaymentService

**Responsabilidad.** Confirmar el pago del pedido e iniciar el alistamiento.

**Firma.** `Order execute(User user, Order order)`

Corresponde al paso "Transacción" del flujo del negocio.

**Autorización.** `AuthorizeBuyerOperationService` + `ValidateBuyerOwnershipService`.

**SUPUESTO.** La especificación indica que el pago se valida antes de iniciar la preparación, pero **no nombra al actor que lo confirma**. Se asume el Comprador, en coherencia con la Matriz de Responsabilidades, que marca "Gestión de Pedidos" para el Comprador. Trasladar esta responsabilidad a otro rol implica cambiar únicamente la autorización de este servicio.

**Flujo.**

```text
1. Autorizar comprador
2. Recuperar el pedido almacenado y validar propiedad
3. Rechazar si el estado no es PENDING_PAYMENT
4. Transicionar a PAID
5. Emitir la factura (GenerateInvoiceService)
6. Registrar la salida por venta de cada línea física
7. ¿Todas las líneas son digitales?
        +-- sí --> transicionar a DELIVERED
        +-- no --> el pedido continúa al flujo logístico
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido de otro comprador | `OwnershipViolationException` |
| Estado distinto de `PENDING_PAYMENT` | `DomainException` |
| Pedido ya facturado | `DomainException` (desde `GenerateInvoiceService`) |

**Efectos.**

- Pedido en `PAID`, o en `DELIVERED` si es íntegramente digital.
- Factura emitida.
- Un movimiento `SALE_OUTBOUND` por cada línea física, usando la bodega grabada en el checkout. Las líneas digitales no producen movimiento porque nunca consumieron inventario.

**Regla del Dominio 5.** Los productos digitales son de entrega inmediata tras el pago: un pedido sin líneas físicas no tiene nada que despachar, así que se cierra en el mismo acto en lugar de quedar esperando un envío que nunca llegaría.

---

## 3. MarkOrderShippedService

**Responsabilidad.** Registrar que el pedido salió físicamente de la bodega.

**Firma.** `Order execute(User user, Order order)`

**Autorización.** `AuthorizeLogisticsOperatorOperationService`.

**Transición.** `PAID --> SHIPPED`

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido `DELIVERED` | `DomainException` |
| Estado distinto de `PAID` | `DomainException` |

**Nota de diseño.** `DispatchShipmentService` no cambia el estado del pedido por su cuenta: invoca este servicio. La regla del ciclo de vida del pedido vive en un solo lugar.

---

## 4. MarkOrderDeliveredService

**Responsabilidad.** Cerrar el pedido tras confirmarse la entrega.

**Firma.** `Order execute(User user, Order order)`

**Autorización.** `AuthorizeLogisticsOperatorOperationService`.

**Transición.** `SHIPPED --> DELIVERED`

Corresponde al paso "Cierre" del flujo del negocio.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido ya `DELIVERED` | `DomainException` |
| Estado distinto de `SHIPPED` | `DomainException` |

**Regla.** A partir de aquí el pedido es inmutable. Lo único que puede ocurrir después es una solicitud de devolución, que crea una entidad `ReturnRefund` aparte y **no modifica el pedido**.

---

## 5. ConsultOrderService

**Responsabilidad.** Consultar los pedidos del comprador autenticado.

**Firmas.**

```java
List<Order> executeMyOrders(User user);
Order execute(User user, Order order);
```

**Autorización.** `AuthorizeBuyerOperationService` + `ValidateBuyerOwnershipService` en la consulta individual.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido de otro comprador | `OwnershipViolationException` |

**Nota.** El listado consulta por el comprador autenticado, así que los pedidos ajenos nunca llegan a memoria. La consulta individual sí recibe un identificador del exterior y por eso valida propiedad de forma explícita.

---

## 6. ConsultSellerOrdersService

**Responsabilidad.** Devolver los pedidos que contienen productos del vendedor autenticado.

**Firma.** `List<Order> execute(User user)`

**Autorización.** `AuthorizeSellerOperationService`.

**Flujo.**

```text
1. Autorizar vendedor y obtener su Seller
2. Recuperar sus productos
3. Para cada producto, recuperar las líneas de pedido que lo contienen
4. Reunir los pedidos de esas líneas, sin repetir
```

**Regla.** La Matriz de Responsabilidades involucra al Vendedor en la gestión de pedidos, y RG-03 limita esa participación a lo propio: el vendedor alcanza un pedido **porque uno de sus productos fue comprado en él**, no porque pueda ver los pedidos del sistema.

Un pedido que contenga varios productos del mismo vendedor aparece una sola vez; la comparación se hace por el identificador del pedido.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `SELLER` | `UnauthorizedOperationException` |

---

## 7. ConsultAllOrdersService

**Responsabilidad.** Consultas consolidadas y cola de despacho.

**Firmas.**

```java
List<Order> execute(User user);                       // consolidado
Order executeByIdentifier(User user, Order order);    // pedido individual
List<Order> executeOrdersToDispatch(User user);       // cola de despacho
```

**Autorización.**

| Método | Autorización | Quién |
| :--- | :--- | :--- |
| `execute` | `AuthorizeAdministrativeConsultationService` | Administrador o supervisor (OBJ-12) |
| `executeByIdentifier` | `AuthorizeAdministrativeConsultationService` | Administrador o supervisor |
| `executeOrdersToDispatch` | `AuthorizeLogisticsOperatorOperationService` | Operador logístico |

La cola de despacho son los pedidos en estado `PAID`: los que ya se pagaron y esperan salir de bodega.

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado | `UnauthorizedOperationException` |
| Pedido inexistente | `EntityNotFoundException` |

---

## 8. GenerateInvoiceService

**Responsabilidad.** Emitir la factura de un pedido pagado (OBJ-09).

**Firma.** `Invoice execute(Order order)`

**Alcance.** Servicio interno invocado por `ConfirmOrderPaymentService`; la autorización la realiza el servicio llamador.

**Flujo.**

```text
1. Verificar que el pedido no tenga factura previa
2. Recuperar sus líneas
3. Sumar precio unitario congelado x cantidad de cada línea
4. Crear la factura con el total y la fecha de emisión
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Pedido ausente | `EntityNotFoundException` |
| Pedido ya facturado | `DomainException` |
| Pedido sin líneas | `DomainException` |
| Alguna línea sin precio o sin cantidad | `DomainException` |
| Total menor o igual a cero | `DomainException` |

**Regla.** El total se calcula con el **precio congelado en la línea**, no con el precio actual del catálogo. Así la factura refleja lo que el comprador aceptó pagar, aunque el vendedor haya cambiado el precio después.

**Inmutabilidad.** El puerto de facturas no expone `update`: una factura emitida no se modifica. Un pedido solo puede facturarse una vez, lo que impide doble cobro si la confirmación de pago se invoca dos veces.

---

## 9. ConsultInvoiceService

**Responsabilidad.** Consultar las facturas del comprador autenticado.

**Firmas.**

```java
List<Invoice> execute(User user);
Invoice executeByOrder(User user, Order order);
```

**Autorización.** `AuthorizeBuyerOperationService` + `ValidateBuyerOwnershipService`.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Pedido de otro comprador | `OwnershipViolationException` |
| Factura inexistente | `EntityNotFoundException` |
| Factura de otro comprador | `OwnershipViolationException` |

**Nota.** La consulta por pedido valida propiedad **dos veces**: sobre el pedido recibido y sobre la factura recuperada. Son dos entidades distintas y la segunda no se deduce de la primera sin comprobarlo.
