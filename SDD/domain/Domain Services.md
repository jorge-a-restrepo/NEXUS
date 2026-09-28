# Servicios de Dominio — NexusMarket

## 1. Propósito

Este documento es el **catálogo de alto nivel** de los servicios que componen NexusMarket. Describe qué hace cada servicio y a qué subdominio pertenece.

El detalle de cada uno —precondiciones, validaciones, flujo, excepciones y efectos— está en los archivos por subdominio dentro de `SDD/domain/services/`.

---

## 2. Principios de diseño

### 2.1 Un servicio equivale a un caso de uso

Cada servicio resuelve una sola operación del negocio y expone un método `execute`. Un servicio con varios métodos solo aparece cuando se trata de la misma consulta con distintos criterios (`ConsultInventoryService`, por ejemplo).

### 2.2 El dominio primero

Los servicios reciben modelos de dominio y value objects. Nunca reciben DTOs REST, entidades de persistencia ni identificadores primitivos en lugar del modelo correspondiente.

### 2.3 Autorización antes que negocio

El orden de todo servicio autenticado es siempre el mismo:

```text
1. Autorizar     (rol y estado operativo del usuario)
2. Resolver      (recuperar las entidades autoritativas por sus puertos de salida)
3. Validar       (reglas del negocio sobre el estado real, no sobre lo que llegó)
4. Aplicar       (modificar estado)
5. Registrar     (movimientos y documentos derivados)
```

El paso 2 es crítico: el servicio **nunca confía en el estado que viene en el parámetro**. Recupera la entidad almacenada y valida sobre ella. Un pedido que llegue marcado como `PENDING_PAYMENT` desde el exterior no sirve de nada si el almacenado ya está `DELIVERED`.

### 2.4 Composición entre servicios

Un servicio puede invocar a otro cuando la regla pertenece al otro subdominio. `DispatchShipmentService` no cambia el estado del pedido por su cuenta: llama a `MarkOrderShippedService`, que es donde vive el ciclo de vida del pedido. Así una regla existe en un solo lugar.

---

## 3. Servicios de Autorización

| Servicio | Responsabilidad |
| :--- | :--- |
| `ValidateUserStatusService` | Exige usuario autenticado y en estado operativo (RG-01) |
| `ValidateRoleAuthorizationService` | Exige un rol determinado (RG-02, RG-03) |
| `AuthorizeBuyerOperationService` | Autoriza al comprador y devuelve su `Buyer` |
| `AuthorizeSellerOperationService` | Autoriza al vendedor y devuelve su `Seller` |
| `AuthorizeLogisticsOperatorOperationService` | Autoriza al operador logístico |
| `AuthorizeAdministratorOperationService` | Autoriza al administrador |
| `AuthorizeSupervisorOperationService` | Autoriza al supervisor |
| `AuthorizeInventoryOperationService` | Autoriza la administración de inventario (vendedor u operador) |
| `AuthorizeAdministrativeConsultationService` | Autoriza consultas consolidadas (administrador o supervisor) |
| `ValidateBuyerOwnershipService` | Verifica que un carrito, pedido, factura o devolución sea del comprador |
| `ValidateSellerOwnershipService` | Verifica que un producto o bodega sea del vendedor |

---

## 4. Servicios de Usuarios

| Servicio | Responsabilidad |
| :--- | :--- |
| `LoginService` | Autentica contra las credenciales almacenadas |
| `LogoutService` | Cierra la sesión del usuario autenticado |
| `RegisterBuyerUserService` | Auto-registro del comprador |
| `RegisterInternalUserService` | Alta de operadores logísticos, supervisores y administradores |
| `ChangeUserStatusService` | Activa o bloquea un usuario |
| `ConsultUserService` | Consulta de usuarios para la administración |

---

## 5. Servicios de Compradores

| Servicio | Responsabilidad |
| :--- | :--- |
| `ConsultBuyerProfileService` | Perfil propio del comprador |
| `UpdateBuyerProfileService` | Actualización de datos y direcciones |
| `ChangeBuyerCommercialStatusService` | Cambio del estado comercial (solo administrador) |
| `ConsultBuyersService` | Listado de compradores registrados |

---

## 6. Servicios de Vendedores y Bodegas

| Servicio | Responsabilidad |
| :--- | :--- |
| `RegisterSellerService` | Incorporación del vendedor junto a su primera bodega |
| `ConsultSellerProfileService` | Perfil propio del vendedor |
| `ConsultSellersService` | Listado de vendedores registrados |
| `RegisterWarehouseService` | Alta de bodega |
| `UpdateWarehouseService` | Actualización de bodega |
| `ConsultWarehouseService` | Consulta de bodegas, propias o consolidadas |

---

## 7. Servicios de Catálogo

| Servicio | Responsabilidad |
| :--- | :--- |
| `RegisterProductService` | Alta del producto en el catálogo |
| `UpdateProductService` | Actualización de la información comercial |
| `PublishProductService` | Hace visible el producto en el catálogo público |
| `SuspendProductService` | Retira el producto de la vitrina |
| `DiscontinueProductService` | Retira el producto definitivamente |
| `ConsultCatalogService` | Catálogo público (solo publicados) |
| `ConsultProductService` | Producto individual del catálogo público |
| `ConsultSellerProductsService` | Productos propios del vendedor, en cualquier estado |

---

## 8. Servicios de Inventario

| Servicio | Responsabilidad |
| :--- | :--- |
| `RegisterInventoryMovementService` | Registra un movimiento en el histórico |
| `RegisterInventoryInflowService` | Ingreso de existencias |
| `ReserveInventoryService` | Reserva existencias para un pedido |
| `RegisterSaleOutflowService` | Registra la salida definitiva por venta |
| `AdjustInventoryService` | Corrección manual de existencias |
| `RegisterInventoryReturnService` | Reingreso de mercancía devuelta |
| `ConsultInventoryService` | Consulta por bodega, por producto, por par o consolidada |

---

## 9. Servicios de Carrito

| Servicio | Responsabilidad |
| :--- | :--- |
| `ConsultCartService` | Carrito activo del comprador |
| `AddCartItemService` | Agrega un producto al carrito |
| `UpdateCartItemQuantityService` | Cambia la cantidad de una línea |
| `RemoveCartItemService` | Elimina una línea |
| `CheckoutCartService` | Convierte el carrito en pedido |

---

## 10. Servicios de Pedidos y Facturación

| Servicio | Responsabilidad |
| :--- | :--- |
| `ConfirmOrderPaymentService` | Confirma el pago, factura y registra la salida por venta |
| `MarkOrderShippedService` | Transición a despachado |
| `MarkOrderDeliveredService` | Cierre del pedido |
| `ConsultOrderService` | Pedidos propios del comprador |
| `ConsultSellerOrdersService` | Pedidos que contienen productos del vendedor |
| `ConsultAllOrdersService` | Consolidado y cola de despacho |
| `GenerateInvoiceService` | Emisión de la factura |
| `ConsultInvoiceService` | Facturas propias del comprador |

---

## 11. Servicios de Logística

| Servicio | Responsabilidad |
| :--- | :--- |
| `CreateShipmentService` | Prepara un envío para un pedido pagado |
| `DispatchShipmentService` | Registra la salida física |
| `CompleteShipmentService` | Confirma la entrega y cierra el pedido |
| `ConsultShipmentService` | Consulta de envíos |

---

## 12. Servicios de Devoluciones y Reembolsos

| Servicio | Responsabilidad |
| :--- | :--- |
| `RequestReturnService` | Solicitud de devolución del comprador |
| `ApproveReturnService` | Aprobación por el administrador |
| `RejectReturnService` | Rechazo por el administrador |
| `ProcessRefundService` | Liquidación del reembolso |
| `ConsultReturnService` | Consulta de devoluciones |

---

## 13. Organización de la documentación detallada

```text
SDD/domain/services/
├── authorization-services.md
├── user-services.md
├── seller-warehouse-services.md
├── catalog-services.md
├── inventory-services.md
├── cart-services.md
├── order-services.md
├── shipment-services.md
└── returnrefund-services.md
```
