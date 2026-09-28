# Puertos de Salida — NexusMarket

## 1. Propósito

Los puertos de salida son las interfaces mediante las cuales el dominio se comunica con el mundo exterior: persistencia, hash de contraseñas y emisión de tokens.

**El dominio es dueño de estas interfaces.** Los servicios de dominio nunca dependen de MySQL, MongoDB, JPA, Spring, HTTP ni de ningún repositorio concreto. Cuando un servicio necesita información o capacidad fuera del dominio, la pide a través del puerto correspondiente; la implementación vive en la capa de adaptadores.

```text
Servicio de dominio
        |
        v
Puerto de salida        (domain/ports/out/)
        |
        v
Adaptador de salida     (adapters/persistence/...)
        |
        v
Recurso externo
```

---

## 2. Regla de parámetros

Los métodos de los puertos de salida trabajan con **modelos de dominio**, no con identificadores primitivos, DTOs ni entidades de persistencia.

Incorrecto:

```java
Optional<Product> findById(String productId);
void updateInventory(String productId, int quantity);
```

Correcto:

```java
Optional<Product> findByIdentifier(Product product);
Inventory update(Inventory inventory);
```

Esto mantiene al dominio independiente de cómo se represente una identidad en la base de datos y evita que los servicios manipulen atributos sueltos en lugar de conceptos del negocio.

---

## 3. Inventario de puertos

| Puerto | Responsabilidad |
| :--- | :--- |
| `UserRepositoryPort` | Usuarios en general y unicidad global |
| `BuyerRepositoryPort` | Compradores |
| `SellerRepositoryPort` | Vendedores |
| `WarehouseRepositoryPort` | Bodegas |
| `ProductRepositoryPort` | Catálogo de productos |
| `InventoryRepositoryPort` | Existencias por producto y bodega |
| `InventoryMovementRepositoryPort` | Histórico de movimientos |
| `CartRepositoryPort` | Carritos |
| `CartItemRepositoryPort` | Líneas del carrito |
| `OrderRepositoryPort` | Pedidos |
| `OrderItemRepositoryPort` | Líneas del pedido |
| `InvoiceRepositoryPort` | Facturas |
| `ShipmentRepositoryPort` | Envíos |
| `ReturnRefundRepositoryPort` | Devoluciones y reembolsos |
| `PasswordServicePort` | Hash y verificación de contraseñas |
| `TokenServicePort` | Emisión y validación de tokens de acceso |

---

## 4. UserRepositoryPort

Persistencia y consulta de usuarios de cualquier rol.

```java
User save(User user);
User update(User user);
Optional<User> findByIdentifier(User user);
Optional<User> findByEmail(User user);
Optional<User> findByIdentityDocument(User user);
boolean existsByEmail(User user);
boolean existsByIdentityDocument(User user);
List<User> findAll();
List<User> findAllByRole(UserRole role);
```

Las dos comprobaciones de existencia son el contrato con el que los servicios hacen cumplir la validación crítica de la Sección 11: **documento de identidad y correo únicos en toda la plataforma**. Por eso se consultan sobre el universo completo de usuarios y no sobre el de cada rol.

`findByEmail` es la búsqueda que usa `LoginService`.

---

## 5. BuyerRepositoryPort y SellerRepositoryPort

Ambos exponen la misma forma, tipada sobre su especialización:

```java
Buyer save(Buyer buyer);
Buyer update(Buyer buyer);
Optional<Buyer> findByIdentifier(Buyer buyer);
Optional<Buyer> findByEmail(Buyer buyer);
boolean existsByEmail(Buyer buyer);
boolean existsByIdentityDocument(Buyer buyer);
List<Buyer> findAll();
```

Están separados porque su ciclo de alta es distinto: el comprador se auto-registra (Sección 3.1) y el vendedor solo puede ser incorporado por el Administrador (Dominio 3).

---

## 6. WarehouseRepositoryPort

```java
Warehouse save(Warehouse warehouse);
Warehouse update(Warehouse warehouse);
Optional<Warehouse> findByIdentifier(Warehouse warehouse);
boolean existsByIdentifier(Warehouse warehouse);
List<Warehouse> findAll();
List<Warehouse> findAllByType(WarehouseType warehouseType);
List<Warehouse> findAllBySeller(Seller seller);
```

`findAllByType` responde a la distinción del Dominio 4 entre bodegas del Marketplace y de vendedores. `findAllBySeller` es lo que permite que un vendedor solo vea sus propias bodegas (RG-03).

---

## 7. ProductRepositoryPort

```java
Product save(Product product);
Product update(Product product);
Optional<Product> findByIdentifier(Product product);
boolean existsByIdentifier(Product product);
List<Product> findAll();
List<Product> findAllByStatus(ProductStatus productStatus);
List<Product> findAllByType(ProductType productType);
List<Product> findAllBySeller(Seller seller);
```

`findAllByStatus` es el contrato del catálogo público: solo productos `PUBLISHED` son visibles. `findAllBySeller` sostiene el aislamiento del vendedor sobre su propio catálogo.

---

## 8. InventoryRepositoryPort

```java
Inventory save(Inventory inventory);
Inventory update(Inventory inventory);
Optional<Inventory> findByProductAndWarehouse(Product product, Warehouse warehouse);
boolean existsByProductAndWarehouse(Product product, Warehouse warehouse);
List<Inventory> findAllByProduct(Product product);
List<Inventory> findAllByWarehouse(Warehouse warehouse);
List<Inventory> findAll();
```

La búsqueda principal es por el **par (producto, bodega)**, no por un identificador. Esa es la clave real del Dominio 6: el inventario está obligatoriamente vinculado a un producto y a una bodega, y ese par no debe generar registros duplicados.

`findAllByProduct` es lo que permite a `ReserveInventoryService` recorrer las bodegas que tienen el producto cuando el inventario está distribuido.

---

## 9. InventoryMovementRepositoryPort

```java
InventoryMovement save(InventoryMovement inventoryMovement);
List<InventoryMovement> findAllByInventory(Inventory inventory);
List<InventoryMovement> findAllByType(MovementType movementType);
List<InventoryMovement> findAllByResponsibleUser(User user);
```

**No expone `update` ni `delete`.** Los movimientos son el histórico de trazabilidad del inventario: se agregan, nunca se corrigen. Una corrección de existencias se registra como un movimiento `ADJUSTMENT` nuevo.

---

## 10. CartRepositoryPort y CartItemRepositoryPort

```java
Cart save(Cart cart);
Cart update(Cart cart);
Optional<Cart> findActiveByBuyer(Buyer buyer);
List<Cart> findAllByBuyer(Buyer buyer);
```

```java
CartItem save(CartItem cartItem);
CartItem update(CartItem cartItem);
void delete(CartItem cartItem);
Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
List<CartItem> findAllByCart(Cart cart);
```

La búsqueda del carrito es **por comprador y estado activo**, nunca por un identificador que venga del exterior: así un comprador no puede pedir el carrito de otro.

`findByCartAndProduct` es lo que permite que agregar un producto ya presente aumente su cantidad en lugar de duplicar la línea.

---

## 11. OrderRepositoryPort y OrderItemRepositoryPort

```java
Order save(Order order);
Order update(Order order);
Optional<Order> findByIdentifier(Order order);
List<Order> findAllByBuyer(Buyer buyer);
List<Order> findAllByStatus(OrderStatus orderStatus);
List<Order> findAll();
```

```java
OrderItem save(OrderItem orderItem);
List<OrderItem> findAllByOrder(Order order);
List<OrderItem> findAllByProduct(Product product);
```

`OrderItemRepositoryPort` **no expone `update`**: el precio unitario es una fotografía congelada al momento de la compra, de modo que un cambio posterior de precio en el catálogo no altera pedidos ya cerrados.

`findAllByStatus` alimenta la cola de despacho del Operador Logístico (`PAID`), y `findAllByProduct` permite al vendedor ver los pedidos que contienen sus productos.

---

## 12. InvoiceRepositoryPort

```java
Invoice save(Invoice invoice);
Optional<Invoice> findByOrder(Order order);
List<Invoice> findAllByBuyer(Buyer buyer);
List<Invoice> findAll();
```

Sin `update`: una factura emitida es un registro inmutable. `findByOrder` cumple dos funciones: recuperar la factura del comprador y garantizar que un pedido no se facture dos veces.

---

## 13. ShipmentRepositoryPort

```java
Shipment save(Shipment shipment);
Shipment update(Shipment shipment);
List<Shipment> findAllByOrder(Order order);
List<Shipment> findAllByWarehouse(Warehouse warehouse);
List<Shipment> findAllByStatus(ShipmentStatus shipmentStatus);
List<Shipment> findAllByLogisticsOperator(LogisticsOperator logisticsOperator);
List<Shipment> findAll();
```

La consulta por pedido devuelve siempre una **lista**, porque el inventario distribuido del Dominio 6 hace que un mismo pedido pueda requerir más de un despacho.

---

## 14. ReturnRefundRepositoryPort

```java
ReturnRefund save(ReturnRefund returnRefund);
ReturnRefund update(ReturnRefund returnRefund);
Optional<ReturnRefund> findByOrder(Order order);
List<ReturnRefund> findAllByBuyer(Buyer buyer);
List<ReturnRefund> findAllByStatus(ReturnStatus returnStatus);
List<ReturnRefund> findAll();
```

`findByOrder` devuelve un `Optional` porque un pedido admite una sola solicitud de devolución. `findAllByStatus` alimenta la bandeja de pendientes del Administrador.

---

## 15. PasswordServicePort

```java
String hash(String rawPassword);
boolean matches(String rawPassword, String hashedPassword);
```

RG-01 exige que toda operación la ejecute un usuario autenticado, lo que implica almacenar y verificar credenciales. El dominio **nunca conoce el algoritmo de hash**: solo depende de este contrato. La contraseña en texto plano jamás llega al estado persistido.

---

## 16. TokenServicePort

```java
String generateToken(User user);
boolean validateToken(String token);
User reconstructUser(String token);
```

El token transporta la información suficiente para reconstruir el modelo de dominio `User`, que es el objeto que recibe todo puerto de entrada. La tecnología concreta del token pertenece al adaptador de entrega; el dominio solo define qué necesita de él.

`LoginService` valida las credenciales y devuelve el `User`; el adaptador REST es quien emite el token a partir de ese resultado.
