# Servicios de Inventario

## 1. Propósito

Cubre el Dominio 6 (Gestión del Inventario) y el OBJ-06.

Tres reglas gobiernan todo el subdominio:

| Regla | Origen |
| :--- | :--- |
| El inventario está vinculado obligatoriamente a un producto **y** una bodega | Dominio 6 |
| **No se permiten existencias negativas bajo ninguna circunstancia** | Dominio 6 |
| No se puede reservar inventario inexistente o marcado como dañado | Sección 11 |

El inventario es **distribuido**: un mismo producto puede tener existencias en varias bodegas, y cada combinación (producto, bodega) es un registro propio que no debe duplicarse.

**Autorización.** La Matriz de Responsabilidades asigna "Administración Inventario" al Vendedor y al Operador Logístico, por lo que los servicios de administración usan `AuthorizeInventoryOperationService`, que admite ambos roles.

---

## 2. Efecto de cada tipo de movimiento

| `MovementType` | Documento | Efecto sobre la cantidad disponible |
| :--- | :--- | :--- |
| `INBOUND` | Ingreso | Aumenta |
| `RESERVATION` | Reserva | Disminuye |
| `SALE_OUTBOUND` | Salida por venta | Ninguno: la reserva ya descontó |
| `ADJUSTMENT` | Ajuste | Aumenta o disminuye |
| `RETURN` | Devolución | Aumenta |

**Por qué `SALE_OUTBOUND` no descuenta.** El descuento ocurre al reservar, en el momento del checkout, para que dos compradores no puedan comprometer la misma unidad mientras uno de los dos paga. La salida por venta deja constancia de que esa reserva se concretó; volver a descontar contaría la unidad dos veces.

---

## 3. RegisterInventoryMovementService

**Responsabilidad.** Registrar un movimiento en el histórico de trazabilidad.

**Firma.** `InventoryMovement execute(Inventory inventory, MovementType movementType, Integer quantity, User responsibleUser)`

**Alcance.** Servicio interno: lo invocan los demás servicios de inventario y no está expuesto en ningún puerto de entrada.

**Efectos.** Crea el movimiento con la fecha del sistema y el usuario responsable.

| Condición | Excepción |
| :--- | :--- |
| Inventario ausente | `DomainException` |
| Tipo de movimiento ausente | `DomainException` |

**Inmutabilidad.** El puerto de salida no expone `update` ni `delete` para los movimientos. Una corrección se registra como un `ADJUSTMENT` nuevo; el histórico nunca se reescribe.

---

## 4. RegisterInventoryInflowService

**Responsabilidad.** Registrar el ingreso de existencias de un producto en una bodega.

**Firma.** `Inventory execute(User user, InventoryMovement inventoryMovement)`

**Autorización.** `AuthorizeInventoryOperationService`.

**Flujo.**

```text
1. Autorizar (vendedor u operador logístico)
2. Validar cantidad mayor que cero
3. Validar que el inventario referencie producto y bodega
4. Buscar el registro por el par (producto, bodega)
        |
        +-- existe   --> sumar la cantidad
        |
        +-- no existe --> crear el registro con esa cantidad y estado AVAILABLE
5. Registrar el movimiento INBOUND
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado | `UnauthorizedOperationException` |
| Movimiento o inventario ausentes | `DomainException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Producto o bodega ausentes | `DomainException` |

**Decisión de diseño.** La creación del registro cuando el par no existe es lo que evita duplicados: no hay una operación separada de "crear inventario" que alguien pueda ejecutar dos veces para el mismo par. Si no se indica estado de stock, se asume `AVAILABLE`.

---

## 5. ReserveInventoryService

**Responsabilidad.** Reservar existencias de un producto para un pedido en confirmación.

**Firma.** `Inventory execute(User user, Product product, Integer quantity)`

**Alcance.** Servicio interno invocado por `CheckoutCartService`. No lleva autorización por rol propia porque el servicio llamador ya autorizó al comprador; aquí se aplican exclusivamente las reglas de stock.

**Flujo.**

```text
1. Validar producto y cantidad mayor que cero
2. Recuperar todos los registros de inventario del producto
3. Recorrer las bodegas:
        - descartar las que tengan stock DAMAGED
        - tomar la primera con cantidad suficiente
4. Si ninguna alcanza --> rechazar
5. Descontar la cantidad
6. Registrar el movimiento RESERVATION
7. Devolver el inventario afectado
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Producto ausente | `EntityNotFoundException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Producto sin inventario registrado | `DomainException` |
| Ninguna bodega con existencias suficientes | `DomainException` |

**Cumplimiento de las reglas críticas.**

- *Inventario inexistente*: si el producto no tiene registros, se rechaza.
- *Stock dañado*: las bodegas con `StockStatus.DAMAGED` se saltan por completo.
- *Sin negativos*: solo se toma de una bodega que ya tiene cantidad suficiente, de modo que la resta nunca puede bajar de cero.

**Por qué devuelve el `Inventory`.** El checkout necesita saber **de qué bodega** salió cada línea para grabarla en el `OrderItem`: esa bodega es la que después permite registrar la salida por venta y planear el envío.

**Limitación conocida.** La reserva se satisface desde una sola bodega por línea. Una línea cuya cantidad supere el stock de cada bodega individual se rechaza aunque la suma total alcance. Repartir una línea entre bodegas exigiría dividir la línea del pedido, lo que la especificación no plantea.

---

## 6. RegisterSaleOutflowService

**Responsabilidad.** Dejar constancia de la salida definitiva por venta.

**Firma.** `Inventory execute(User user, Inventory inventory, Integer quantity)`

**Alcance.** Servicio interno invocado por `ConfirmOrderPaymentService`.

**Efectos.** Registra un movimiento `SALE_OUTBOUND`. **No modifica la cantidad disponible**, por la razón explicada en la sección 2.

| Condición | Excepción |
| :--- | :--- |
| Inventario, producto o bodega ausentes | `EntityNotFoundException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Registro de inventario inexistente | `EntityNotFoundException` |

---

## 7. AdjustInventoryService

**Responsabilidad.** Aplicar una corrección manual a las existencias.

**Firma.** `Inventory execute(User user, InventoryMovement inventoryMovement)`

**Autorización.** `AuthorizeInventoryOperationService`.

**Flujo.**

```text
1. Autorizar
2. Validar cantidad distinta de cero (puede ser positiva o negativa)
3. Recuperar el registro por el par (producto, bodega)
4. Calcular cantidad actual + ajuste
5. Rechazar si el resultado es negativo
6. Aplicar y registrar el movimiento ADJUSTMENT
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado | `UnauthorizedOperationException` |
| Movimiento o inventario ausentes | `DomainException` |
| Cantidad nula o igual a cero | `DomainException` |
| Producto o bodega ausentes | `DomainException` |
| Registro inexistente | `EntityNotFoundException` |
| El ajuste dejaría existencias negativas | `DomainException` |

**Regla.** Es el único servicio que admite cantidades negativas, y precisamente por eso es donde la prohibición de existencias negativas se verifica de forma explícita antes de aplicar nada.

---

## 8. RegisterInventoryReturnService

**Responsabilidad.** Reingresar al inventario la mercancía devuelta.

**Firma.** `Inventory execute(User user, InventoryMovement inventoryMovement)`

**Autorización.** `AuthorizeLogisticsOperatorOperationService`, rol único.

**Por qué solo el Operador Logístico.** La recepción física de mercancía devuelta es una operación de bodega. El Administrador aprueba la devolución y liquida el reembolso, pero no recibe la mercancía.

**Efectos.** Aumenta la cantidad disponible y registra un movimiento `RETURN`.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `LOGISTICS_OPERATOR` | `UnauthorizedOperationException` |
| Movimiento o inventario ausentes | `DomainException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Registro inexistente | `EntityNotFoundException` |

---

## 9. ConsultInventoryService

**Responsabilidad.** Consultar existencias con distintos criterios y alcances.

**Firmas.**

```java
List<Inventory> executeByWarehouse(User user, Warehouse warehouse);
List<Inventory> executeByProduct(User user, Product product);
Inventory execute(User user, Product product, Warehouse warehouse);
List<Inventory> executeConsolidated(User user);
```

**Autorización.**

| Método | Autorización | Quién |
| :--- | :--- | :--- |
| `executeByWarehouse` | `AuthorizeInventoryOperationService` | Vendedor u operador |
| `executeByProduct` | `AuthorizeInventoryOperationService` | Vendedor u operador |
| `execute` | `AuthorizeInventoryOperationService` | Vendedor u operador |
| `executeConsolidated` | `AuthorizeAdministrativeConsultationService` | Administrador o supervisor (OBJ-12) |

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado | `UnauthorizedOperationException` |
| Producto o bodega ausentes | `EntityNotFoundException` |
| Registro inexistente (consulta por par) | `EntityNotFoundException` |

Ninguno de estos métodos modifica existencias.
