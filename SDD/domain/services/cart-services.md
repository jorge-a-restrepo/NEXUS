# Servicios de Carrito

## 1. Propósito

Cubre el OBJ-07 y la etapa "Carrito" del Dominio 7: la selección provisional de productos que precede al pedido.

Dos reglas transversales:

- **Un comprador opera sobre un único carrito activo.** El carrito se resuelve siempre del usuario autenticado, nunca de un identificador recibido del exterior.
- **Solo un carrito `ACTIVE` admite modificaciones.** Un carrito `CONVERTED` ya es un pedido y modificarlo alteraría una compra en curso.

**Autorización.** Todos usan `AuthorizeBuyerOperationService` y, cuando recuperan el carrito, `ValidateBuyerOwnershipService`.

---

## 2. ConsultCartService

**Responsabilidad.** Devolver el carrito activo del comprador autenticado.

**Firma.** `Cart execute(User user)`

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Estado no operativo | `InvalidUserStatusException` |
| Sin carrito activo | `EntityNotFoundException` |

**Decisión de diseño.** No recibe identificador de carrito. Es la aplicación directa del Dominio 2: no existe forma de pedir el carrito de otro comprador porque no hay dónde indicarlo.

---

## 3. AddCartItemService

**Responsabilidad.** Agregar un producto al carrito activo.

**Firma.** `Cart execute(User user, CartItem cartItem)`

**Flujo.**

```text
1. Autorizar comprador
2. Validar que llegue producto y cantidad mayor que cero
3. Recuperar el producto almacenado
4. Rechazar si no está PUBLISHED
5. Resolver el carrito activo, o crearlo si no existe
6. ¿Ya hay una línea de ese producto?
        +-- sí --> sumar la cantidad a la línea existente
        +-- no --> crear la línea
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Línea o producto ausentes | `DomainException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Producto inexistente | `EntityNotFoundException` |
| Producto no `PUBLISHED` | `DomainException` |

**Reglas aplicadas.**

- *Dominio 5*: solo productos publicados se pueden comprar. La validación usa el producto **almacenado**, no el que llegó en el parámetro, que podría traer cualquier estado.
- *Creación implícita del carrito*: evita un paso previo de "crear carrito" y garantiza que nunca existan dos carritos activos para el mismo comprador.
- *Suma en lugar de duplicado*: dos líneas del mismo producto en un carrito serían ambiguas al convertirlo en pedido.

**Nota.** En esta etapa **no se reserva inventario**. El carrito es una selección provisional; el compromiso sobre el stock ocurre en el checkout. Reservar aquí inmovilizaría existencias por carritos que nunca se concretan.

---

## 4. UpdateCartItemQuantityService

**Responsabilidad.** Cambiar la cantidad de una línea del carrito.

**Firma.** `Cart execute(User user, CartItem cartItem)`

**Flujo.** Autoriza, recupera el carrito activo, valida propiedad y estado `ACTIVE`, localiza la línea por el par (carrito, producto) y fija la nueva cantidad.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Línea o producto ausentes | `DomainException` |
| Cantidad nula o menor o igual a cero | `DomainException` |
| Sin carrito activo | `EntityNotFoundException` |
| Carrito de otro comprador | `OwnershipViolationException` |
| Carrito no `ACTIVE` | `DomainException` |
| Línea inexistente en el carrito | `EntityNotFoundException` |

**Nota.** La cantidad se **fija**, no se suma: es una corrección explícita del comprador. Para eliminar la línea existe `RemoveCartItemService`; por eso cero no es una cantidad válida aquí.

---

## 5. RemoveCartItemService

**Responsabilidad.** Eliminar una línea del carrito activo.

**Firma.** `Cart execute(User user, CartItem cartItem)`

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Línea o producto ausentes | `DomainException` |
| Sin carrito activo | `EntityNotFoundException` |
| Carrito de otro comprador | `OwnershipViolationException` |
| Carrito no `ACTIVE` | `DomainException` |
| Línea inexistente | `EntityNotFoundException` |

---

## 6. CheckoutCartService

**Responsabilidad.** Convertir el carrito activo en un pedido formal.

**Firma.** `Order execute(User user)`

Corresponde al paso "Compra" del flujo del negocio. Es el servicio que articula carrito, inventario y pedido.

**Flujo.**

```text
1. Autorizar comprador y obtener su Buyer
2. Recuperar el carrito activo y validar propiedad
3. Rechazar si el carrito no está ACTIVE
4. Rechazar si el carrito no tiene líneas
5. Para cada línea:
        +-- producto PHYSICAL --> reservar inventario y guardar la bodega servida
        +-- producto DIGITAL  --> sin reserva, sin bodega
6. Crear el pedido en estado PENDING_PAYMENT
7. Para cada línea, crear el OrderItem con:
        - producto
        - cantidad
        - precio unitario CONGELADO del precio base actual
        - bodega que sirvió la línea (nula si es digital)
8. Marcar el carrito como CONVERTED
9. Devolver el pedido
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `BUYER` | `UnauthorizedOperationException` |
| Sin carrito activo | `EntityNotFoundException` |
| Carrito de otro comprador | `OwnershipViolationException` |
| Carrito no `ACTIVE` | `DomainException` |
| Carrito sin productos | `DomainException` |
| Producto de una línea ausente | `EntityNotFoundException` |
| Stock insuficiente, inexistente o dañado | `DomainException` (desde `ReserveInventoryService`) |

**Decisiones de diseño.**

*Las reservas van antes de crear el pedido.* Si alguna línea no tiene stock, la operación falla sin haber creado un pedido incompleto.

*Solo se reserva lo físico.* El Dominio 5 establece que los productos digitales se entregan de inmediato tras el pago y el Dominio 6 vincula el inventario a existencias físicas. Reservar inventario para un producto digital fallaría siempre, porque no tiene registros de existencias.

*El precio se congela.* La línea del pedido guarda el precio base vigente al comprar. Un cambio posterior en el catálogo no altera pedidos ya emitidos, y la factura se calcula sobre ese valor congelado.

*Se guarda la bodega servida.* Es lo que permite después registrar la salida por venta y planear el envío desde la bodega correcta.

*El carrito queda `CONVERTED`.* No se elimina: queda como historial y deja de admitir modificaciones.

**Limitación conocida.** El servicio no es transaccional a nivel de dominio. Si falla la creación de una línea después de haber reservado, las reservas previas quedan aplicadas. La compensación corresponde a la capa de infraestructura, con una transacción que abarque la operación completa.
