# Adaptadores de persistencia (salida)

## 1. Propósito

Implementan los puertos de salida de `domain/ports/out/` sin que el dominio conozca JPA, MongoDB ni Spring Data. Siguiendo el patrón del proyecto de referencia, **cada adaptador define su propia entidad o documento (repository DTO), su mapper (dominio ↔ entidad) y su repositorio Spring Data**. Los modelos de dominio nunca se anotan con `@Entity` ni `@Document`.

```text
Servicio de dominio → Puerto de salida (interfaz) → Adaptador → Mapper → Repositorio Spring Data → MySQL / MongoDB
```

## 2. Qué se guarda en cada base

| Base | Datos | Justificación |
| :--- | :--- | :--- |
| **MySQL** `nexus_db` | Usuarios (los 5 roles), bodegas, productos y variantes, inventario, carritos, ítems de carrito, pedidos, ítems de pedido, facturas, envíos y devoluciones | Información estructurada y relacional: unicidad (RG-05), existencias que nunca son negativas (RG-06) y pedidos con estados (RG-08) |
| **MongoDB** `nexus_audit_db` | `inventory_movements` | **SUPUESTO (D1).** `InventoryMovement` es el histórico inmutable de cada cambio de stock: solo se inserta y se consulta, nunca se actualiza. Cumple el mismo papel que la bitácora del proyecto de referencia |

## 3. Estructura

```text
adapters/persistence/
├── DomainReferences.java        Referencias por identificador, conversión de enums, generación de UUID
├── jpa/
│   ├── entities/                11 entidades @Entity
│   ├── mappers/                 11 mappers estáticos
│   ├── repositories/            11 repositorios JpaRepository
│   ├── JpaReferenceResolver     Carga el objeto relacionado completo cuando el dominio lo necesita
│   └── 13 adaptadores           User, Buyer, Seller, Warehouse, Product, Inventory, Cart, CartItem,
│                                Order, OrderItem, Invoice, Shipment, ReturnRefund
└── mongodb/
    ├── documents/InventoryMovementDocument
    ├── mappers/InventoryMovementMongoMapper
    ├── repositories/InventoryMovementMongoRepository
    └── InventoryMovementMongoAdapter
```

## 4. Esquema relacional

| Tabla | Columnas | Clave / restricciones |
| :--- | :--- | :--- |
| `users` | identifier, identity_document, full_name, email, password (hash BCrypt), role, status, main_address, commercial_status | PK identifier; UNIQUE email; UNIQUE identity_document |
| `user_additional_addresses` | user_identifier, address | Colección de `Buyer.additionalAddresses` |
| `warehouses` | identifier, warehouse_type, seller_identifier | PK identifier |
| `products` | identifier, name, seller_identifier, product_type, status, base_price | PK identifier |
| `product_variants` | product_identifier, variant | Colección de `Product.variants` |
| `inventories` | id (técnico), product_identifier, warehouse_identifier, available_quantity, stock_status | UNIQUE (product_identifier, warehouse_identifier), RG-10 |
| `carts` | identifier, buyer_identifier, status | PK identifier |
| `cart_items` | id (técnico), cart_identifier, product_identifier, quantity | UNIQUE (cart_identifier, product_identifier) |
| `orders` | identifier, buyer_identifier, status | PK identifier |
| `order_items` | id (técnico), order_identifier, product_identifier, quantity, unit_price, warehouse_identifier | — |
| `invoices` | order_identifier, total_amount, issue_date | PK order_identifier (0..1 por pedido) |
| `shipments` | identifier, order_identifier, warehouse_identifier, logistics_operator_identifier, status | PK identifier |
| `return_refunds` | identifier, order_identifier, reason, status, refunded_amount | PK identifier |

Hibernate crea y actualiza las tablas a partir de las entidades (`spring.jpa.hibernate.ddl-auto=update`).

## 5. Decisiones de diseño

- **Herencia aplanada.** Los cinco roles se guardan en la tabla `users`; la columna `role` es el discriminador (RG-02). Al leer, el mapper crea la subclase que corresponde al rol (`BUYER` → `Buyer`…), porque los servicios de autorización verifican la subclase. `UserJpaAdapter`, `BuyerJpaAdapter` y `SellerJpaAdapter` comparten la tabla, y la unicidad de correo y documento se comprueba siempre contra todos los usuarios (RG-05).
- **Relaciones por identificador.** Como en el proyecto de referencia, las relaciones se guardan como el identificador del agregado relacionado (no `@ManyToOne`). Al leer se reconstruye una *referencia* (objeto con solo su identificador). `JpaReferenceResolver` carga el objeto completo solo donde un servicio lo lee: el producto de los ítems de carrito y de pedido (tipo y precio base), el producto y la bodega del inventario, y el pedido de facturas, envíos y devoluciones (su comprador, para validar la propiedad).
- **Enums como texto.** Se guardan con su nombre (`name()`), nunca por posición (ordinal), para que reordenar un enum no cambie el significado de los datos.
- **Identificadores generados.** El adaptador genera un UUID cuando `User`, `Order`, `Cart`, `Shipment` o `ReturnRefund` llegan sin identificador. `Product` y `Warehouse` reciben el identificador del cliente, que los servicios validan.
- **Claves naturales.** `Inventory` se ubica por el par (producto, bodega) y `CartItem` por el par (carrito, producto): guardar inserta o actualiza ese par sin duplicarlo. Su `id` numérico es técnico y no llega al dominio.
- **Contraseña.** Una actualización construida sin contraseña conserva el hash almacenado.
- **Datos desnormalizados en MongoDB.** El documento del movimiento guarda el producto, la bodega y el usuario responsable (identificador y rol), para leerlo sin consultar otras tablas.

## 6. Administrador inicial

**SUPUESTO.** Los usuarios internos (incluidos los administradores) solo los registra un administrador autenticado, y los vendedores solo los incorpora un administrador (RG-09). Sin un primer administrador ningún flujo podría empezar. `infrastructure/seed/DatabaseSeeder` crea uno al arrancar si no existe. Sus credenciales vienen de `nexus.seed.admin.*`, con valores académicos por defecto que se pueden sobrescribir con variables de entorno.

## 7. Errores

Si una restricción de la base rechaza una escritura (por ejemplo un correo duplicado que pasó la validación del dominio), `GlobalExceptionHandler` responde **409**, igual que una regla de negocio violada.
