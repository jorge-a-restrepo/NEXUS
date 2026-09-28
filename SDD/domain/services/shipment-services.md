# Servicios de Logística

## 1. Propósito

Cubre el OBJ-10 (Gestión de los procesos logísticos) y el paso "Logística" del flujo del negocio: empaque, despacho y transporte del pedido.

Dos reglas definen el subdominio:

- **Los envíos existen solo para productos físicos.** El Dominio 5 establece que los digitales se entregan de inmediato tras el pago.
- **Un pedido puede requerir varios envíos.** El inventario es distribuido (Dominio 6), así que las líneas de un mismo pedido pueden salir de bodegas distintas.

**Autorización.** Todo el ciclo del envío es exclusivo del Operador Logístico (`AuthorizeLogisticsOperatorOperationService`), salvo las consultas consolidadas.

```text
CreateShipment   -->  IN_PROGRESS
       |
DispatchShipment -->  el pedido pasa a SHIPPED
       |
CompleteShipment -->  COMPLETED
                      y si todos los envíos están completos,
                      el pedido pasa a DELIVERED
```

---

## 2. CreateShipmentService

**Responsabilidad.** Preparar un envío para un pedido pagado.

**Firma.** `Shipment execute(User user, Shipment shipment)`

**Flujo.**

```text
1. Autorizar operador logístico y obtener su LogisticsOperator
2. Validar que el envío referencie pedido y bodega
3. Recuperar el pedido almacenado
4. Rechazar si el pedido no está PAID
5. Verificar que la bodega exista
6. Verificar que el pedido contenga al menos una línea física
7. Asignar el operador autenticado y el estado IN_PROGRESS
8. Guardar
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Envío sin pedido | `DomainException` |
| Envío sin bodega de despacho | `DomainException` |
| Pedido inexistente | `EntityNotFoundException` |
| Pedido no `PAID` | `DomainException` |
| Bodega inexistente | `EntityNotFoundException` |
| Pedido sin productos físicos | `DomainException` |

**Decisiones de diseño.**

*Solo desde `PAID`.* Un pedido pendiente de pago no se despacha, y uno ya despachado o entregado no genera envíos nuevos.

*La bodega es obligatoria.* Es lo que hace operable el inventario distribuido: sin saber de qué bodega sale, el envío no se puede preparar ni rastrear.

*El operador es el autenticado*, no el que venga en el parámetro: el responsable del despacho es quien lo ejecuta.

*Se verifica que haya líneas físicas.* Intentar despachar un pedido íntegramente digital indica un error de flujo: ese pedido ya debió cerrarse como `DELIVERED` al confirmarse el pago.

---

## 3. DispatchShipmentService

**Responsabilidad.** Registrar la salida física del envío y mover el pedido a despachado.

**Firma.** `Shipment execute(User user, Shipment shipment)`

**Flujo.**

```text
1. Autorizar operador logístico
2. Localizar el envío IN_PROGRESS del pedido
3. Invocar MarkOrderShippedService sobre el pedido
4. Devolver el envío
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Envío sin pedido | `DomainException` |
| Pedido sin envíos | `EntityNotFoundException` |
| Sin envío en curso | `EntityNotFoundException` |
| Envío no `IN_PROGRESS` | `DomainException` |
| Pedido no `PAID` | `DomainException` (desde `MarkOrderShippedService`) |

**Nota de diseño.** El servicio **no cambia el estado del pedido por su cuenta**: delega en `MarkOrderShippedService`, que es donde vive el ciclo de vida del pedido. Si esa transición se rechaza, el despacho tampoco procede.

El envío permanece `IN_PROGRESS` porque sigue en tránsito: `COMPLETED` significa entregado, no despachado.

---

## 4. CompleteShipmentService

**Responsabilidad.** Confirmar la entrega de un envío y, cuando corresponde, cerrar el pedido.

**Firma.** `Shipment execute(User user, Shipment shipment)`

**Flujo.**

```text
1. Autorizar operador logístico
2. Recuperar los envíos del pedido
3. Localizar el envío IN_PROGRESS
4. Marcarlo COMPLETED
5. ¿Todos los envíos del pedido están COMPLETED?
        +-- sí --> MarkOrderDeliveredService sobre el pedido
        +-- no --> el pedido sigue SHIPPED, faltan entregas
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Envío sin pedido | `DomainException` |
| Pedido sin envíos | `EntityNotFoundException` |
| Sin envío en curso | `EntityNotFoundException` |
| Pedido no `SHIPPED` | `DomainException` (desde `MarkOrderDeliveredService`) |

**Regla clave.** El pedido se cierra **solo cuando todos sus envíos están completos**. Cerrarlo con la primera entrega daría por finalizado un pedido cuyas demás cajas siguen en tránsito, y como `DELIVERED` es terminal e inmutable, ese error no tendría corrección posible.

---

## 5. ConsultShipmentService

**Responsabilidad.** Consultar envíos con distintos alcances.

**Firmas.**

```java
List<Shipment> executeInProgress(User user);
List<Shipment> executeByOrder(User user, Order order);
List<Shipment> executeConsolidated(User user, ShipmentStatus shipmentStatus);
List<Shipment> executeAll(User user);
```

**Autorización.**

| Método | Autorización | Quién |
| :--- | :--- | :--- |
| `executeInProgress` | `AuthorizeLogisticsOperatorOperationService` | Operador logístico |
| `executeByOrder` | `AuthorizeLogisticsOperatorOperationService` | Operador logístico |
| `executeConsolidated` | `AuthorizeAdministrativeConsultationService` | Administrador o supervisor |
| `executeAll` | `AuthorizeAdministrativeConsultationService` | Administrador o supervisor |

`executeInProgress` es la bandeja de trabajo del operador: los envíos pendientes de completar.

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado | `UnauthorizedOperationException` |
| Pedido ausente | `EntityNotFoundException` |

Ninguno de estos métodos modifica estado.
