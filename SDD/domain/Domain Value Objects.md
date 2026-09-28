# Value Objects y Enumeraciones — NexusMarket

## 1. Propósito

Este documento especifica los valores acotados del dominio de NexusMarket.

Todo atributo cuyo conjunto de valores está delimitado por el negocio se modela como enumeración y no como `String` abierto. El objetivo es que un estado inválido **no se pueda representar**: no existe forma de construir un producto en estado "publicadoo" ni un pedido en estado "en camino", porque esos valores no existen en el tipo.

Cada enumeración indica qué dice literalmente la especificación funcional y, cuando aplica, qué se infirió. Las inferencias se marcan como **SUPUESTO** y están también anotadas en el javadoc de la clase correspondiente.

---

## 2. Inventario de valores acotados

| Enumeración | Atributo que tipa | Origen en la especificación |
| :--- | :--- | :--- |
| `UserRole` | `User.role` | Sección 5, Participantes del Negocio |
| `UserStatus` | `User.status` | Dominio 1, atributo "Estado" |
| `CommercialStatus` | `Buyer.commercialStatus` | Dominio 2, atributo "Estado comercial" |
| `WarehouseType` | `Warehouse.warehouseType` | Dominio 4, clasificación de bodegas |
| `ProductType` | `Product.productType` | Dominio 5, atributo "Tipo de Producto" |
| `ProductStatus` | `Product.status` | Dominio 5, atributo "Estado" |
| `StockStatus` | `Inventory.stockStatus` | Sección 11, validación de inventario |
| `MovementType` | `InventoryMovement.movementType` | Dominio 6, "Movimientos" |
| `CartStatus` | `Cart.status` | Dominio 7, etapa "Carrito" |
| `OrderStatus` | `Order.status` | Dominio 7, ciclo de estados |
| `ShipmentStatus` | `Shipment.status` | OBJ-10 |
| `ReturnStatus` | `ReturnRefund.status` | OBJ-11 |

---

## 3. UserRole

Rol de negocio del usuario. La Restricción General RG-02 establece que **cada usuario tiene exactamente un rol**, por lo que es un atributo simple y no una colección.

| Valor | Participante |
| :--- | :--- |
| `BUYER` | Comprador |
| `SELLER` | Vendedor |
| `LOGISTICS_OPERATOR` | Operador Logístico |
| `ADMINISTRATOR` | Administrador |
| `SUPERVISOR` | Supervisor |

Los cinco valores están enumerados literalmente en la Sección 5. Cada uno tiene su puerto de entrada correspondiente en `domain/ports/in/`.

---

## 4. UserStatus

Condición operativa del usuario (Dominio 1).

| Valor | Significado |
| :--- | :--- |
| `ACTIVE` | Puede ejecutar operaciones |
| `BLOCKED` | No puede ejecutar ninguna operación |

El documento indica "Activo, Bloqueado, etc.". El "etc." no se desarrolla, por lo que **no se inventan valores adicionales**: se declaran únicamente los dos nombrados.

`ValidateUserStatusService` rechaza toda operación de un usuario que no esté `ACTIVE`, y `LoginService` impide incluso la autenticación.

---

## 5. CommercialStatus

Condición del comprador para realizar compras (Dominio 2).

| Valor | Significado |
| :--- | :--- |
| `ENABLED` | Habilitado para comprar |
| `SUSPENDED` | Suspendido comercialmente |

**SUPUESTO.** El documento define el atributo como "condición del comprador para realizar compras" pero no publica sus valores. La condición es binaria por definición —el comprador puede comprar o no puede—, por lo que se declaran exactamente dos valores y nada más allá de eso.

Solo el Administrador lo modifica, a través de `ChangeBuyerCommercialStatusService`; el comprador no lo edita desde su perfil.

---

## 6. WarehouseType

Clasificación de la bodega (Dominio 4).

| Valor | Significado |
| :--- | :--- |
| `MARKETPLACE` | Bodega propia del Marketplace |
| `SELLER` | Bodega de un vendedor |

Los dos valores están explícitos en el documento: "Se distinguen bodegas del Marketplace y bodegas de Vendedores". Las de tipo `SELLER` referencian a su dueño mediante `Warehouse.seller`.

---

## 7. ProductType

Naturaleza del producto (Dominio 5).

| Valor | Consecuencia en el negocio |
| :--- | :--- |
| `PHYSICAL` | Requiere inventario y despacho |
| `DIGITAL` | Entrega inmediata tras el pago, sin envío |

Es la enumeración con mayor impacto en los flujos: `CheckoutCartService` solo reserva inventario para productos físicos, `ConfirmOrderPaymentService` cierra como `DELIVERED` un pedido íntegramente digital, y `CreateShipmentService` rechaza generar envío para un pedido sin líneas físicas.

---

## 8. ProductStatus

Estado del producto en el catálogo (Dominio 5).

| Valor | Significado |
| :--- | :--- |
| `PUBLISHED` | Visible en el catálogo público y comprable |
| `SUSPENDED` | Retirado temporalmente de la vitrina |
| `DISCONTINUED` | Retirado definitivamente |

Los tres valores son literales del documento. `DISCONTINUED` es terminal: un producto descontinuado no se modifica ni se vuelve a publicar.

**SUPUESTO.** El flujo del negocio separa el paso "Catálogo" (registro) del paso "Publicación" (visibilidad), pero la enumeración no ofrece un estado de borrador. Un producto recién registrado queda en `SUSPENDED` —es decir, aún no visible— y pasa a `PUBLISHED` mediante `PublishProductService`. No se inventó un cuarto valor.

---

## 9. StockStatus

Condición operativa de las existencias de un registro de inventario.

| Valor | Significado |
| :--- | :--- |
| `AVAILABLE` | Existencias disponibles para la venta |
| `DAMAGED` | Existencias dañadas, no reservables |

**SUPUESTO.** La Sección 11 nombra explícitamente el estado dañado ("no se puede reservar inventario inexistente o marcado como 'Dañado'") y el Dominio 6 describe el inventario como "existencias disponibles". De ahí salen los dos valores. El documento no publica el catálogo completo de condiciones, por lo que no se declara ninguno más.

---

## 10. MovementType

Tipo de movimiento de inventario (Dominio 6).

| Valor | Documento | Efecto sobre la cantidad disponible |
| :--- | :--- | :--- |
| `INBOUND` | Ingreso | Aumenta |
| `RESERVATION` | Reserva | Disminuye |
| `SALE_OUTBOUND` | Salida por venta | Ninguno: la reserva ya descontó |
| `ADJUSTMENT` | Ajuste | Aumenta o disminuye |
| `RETURN` | Devolución | Aumenta |

Los cinco valores están enumerados literalmente en el documento. Ningún movimiento puede dejar la cantidad disponible por debajo de cero.

---

## 11. CartStatus

Estado del carrito de compras (Dominio 7).

| Valor | Significado |
| :--- | :--- |
| `ACTIVE` | En uso, admite modificaciones |
| `CONVERTED` | Ya se transformó en pedido |
| `ABANDONED` | Descartado sin convertirse |

**SUPUESTO.** El documento describe el carrito como "selección provisional de productos" dentro del ciclo del pedido, pero no publica sus estados. `ACTIVE` y `CONVERTED` son necesarios para el flujo de checkout; `ABANDONED` cubre el carrito que nunca se convierte.

Solo un carrito `ACTIVE` admite agregar, modificar o eliminar líneas.

---

## 12. OrderStatus

Ciclo de vida del pedido (Dominio 7).

| Valor | Documento |
| :--- | :--- |
| `PENDING_PAYMENT` | Pendiente de Pago |
| `PAID` | Pagado |
| `SHIPPED` | Despachado |
| `DELIVERED` | Entregado / Finalizado |

Transiciones permitidas, verificadas una por una en los servicios:

```text
PENDING_PAYMENT --> PAID --> SHIPPED --> DELIVERED
```

`DELIVERED` es **terminal**. La Sección 11 establece que un pedido finalizado no puede modificarse bajo ninguna circunstancia, por lo que ningún servicio del sistema transiciona un pedido fuera de ese estado.

El documento lista además "Carrito" como primera etapa del ciclo. En este modelo el carrito es una entidad propia (`Cart`) con su propio estado, porque tiene atributos y comportamiento distintos a los del pedido. La equivalencia es: `Cart.ACTIVE` corresponde a la etapa "Carrito", y el pedido nace cuando el carrito se convierte.

---

## 13. ShipmentStatus

Estado del envío (OBJ-10).

| Valor | Significado |
| :--- | :--- |
| `IN_PROGRESS` | Creado o en tránsito |
| `COMPLETED` | Entrega confirmada |

**SUPUESTO.** El documento incluye la gestión de envíos en el alcance pero no desarrolla el dominio ni sus estados. Se declaran los dos estados mínimos que el flujo exige.

Como el inventario es distribuido, un pedido puede requerir varios envíos: el pedido se cierra como `DELIVERED` solo cuando **todos** sus envíos están `COMPLETED`.

---

## 14. ReturnStatus

Estado de la devolución y su reembolso (OBJ-11).

| Valor | Significado |
| :--- | :--- |
| `REQUESTED` | Solicitada por el comprador |
| `APPROVED` | Aprobada por el Administrador |
| `REJECTED` | Rechazada, sin reembolso |
| `REFUNDED` | Reembolso liquidado |

**SUPUESTO.** El documento incluye devoluciones y reembolsos en el alcance y asigna su gestión al Comprador y al Administrador en la matriz de responsabilidades, pero no publica los estados. Los cuatro declarados son los que exige ese flujo de aprobación.

Transiciones permitidas:

```text
REQUESTED --> APPROVED --> REFUNDED
REQUESTED --> REJECTED
```

---

## 15. Atributos que siguen siendo texto libre

No todo `String` es una obsesión por primitivos. Estos atributos son texto libre por naturaleza y así deben permanecer:

| Atributo | Razón |
| :--- | :--- |
| `User.identifier`, `User.identityDocument` | Identificadores externos, sin catálogo |
| `User.fullName`, `User.email` | Datos de identidad |
| `User.password` | Almacena el hash producido por `PasswordServicePort` |
| `Buyer.mainAddress`, `Buyer.additionalAddresses` | Direcciones de entrega |
| `Product.name`, `Product.variants` | Descripción comercial; el Dominio 5 define las variantes como lista |
| `Warehouse.identifier` | Identificador externo |
| `ReturnRefund.reason` | Motivo escrito por el comprador |
