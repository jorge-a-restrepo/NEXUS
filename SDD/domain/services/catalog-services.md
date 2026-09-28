# Servicios de Catálogo

## 1. Propósito

Cubre el Dominio 5 (Gestión del Catálogo) y el OBJ-05.

El catálogo distingue **productos físicos**, que requieren inventario y despacho, de **productos digitales**, de entrega inmediata tras el pago. Esa distinción, tipada en `ProductType`, condiciona después todo el flujo de compra.

Ciclo de vida del producto:

```text
Registro  -->  SUSPENDED  --publish-->  PUBLISHED  --suspend-->  SUSPENDED
                    |                        |
                    +--discontinue--> DISCONTINUED <--discontinue--+
                                         (terminal)
```

**SUPUESTO.** El flujo del negocio separa el paso "Catálogo" (registro) del paso "Publicación" (visibilidad), pero `ProductStatus` solo declara tres valores y ninguno significa borrador. Un producto recién registrado queda `SUSPENDED` —no visible— hasta que se publica. No se inventó un cuarto estado.

---

## 2. RegisterProductService

**Responsabilidad.** Registrar un producto nuevo en el catálogo del vendedor.

**Firma.** `Product execute(User user, Product product)`

**Autorización.** `AuthorizeSellerOperationService`. La Matriz de Responsabilidades marca "Registro Productos" únicamente para el Vendedor.

**Flujo.**

```text
1. Autorizar vendedor y obtener su Seller
2. Validar identificador, nombre, tipo y precio base
3. Verificar que el identificador no exista
4. Asociar el producto al vendedor autenticado
5. Fijar estado SUSPENDED
6. Guardar
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `SELLER` | `UnauthorizedOperationException` |
| Producto ausente | `DomainException` |
| Identificador o nombre vacíos | `DomainException` |
| Tipo de producto no indicado | `DomainException` |
| Precio base nulo o menor o igual a cero | `DomainException` |
| Identificador ya existente | `DomainException` |

**Efectos.** El producto queda asociado al vendedor autenticado —no al que venga en el parámetro— y en estado no visible. El precio base debe ser estrictamente positivo porque es el valor que se congelará en la línea de pedido al comprar.

---

## 3. UpdateProductService

**Responsabilidad.** Actualizar la información comercial de un producto existente.

**Firma.** `Product execute(User user, Product product)`

**Autorización.** `AuthorizeSellerOperationService` + `ValidateSellerOwnershipService`.

**Flujo.**

```text
1. Autorizar vendedor
2. Recuperar el producto almacenado
3. Validar propiedad: el producto es de este vendedor
4. Rechazar si está DISCONTINUED
5. Aplicar los cambios recibidos
6. Actualizar
```

**Campos modificables.** Nombre, precio base y variantes.

**Campos no modificables.** Identificador (es la identidad), tipo de producto (cambiaría si requiere inventario y despacho, con pedidos ya emitidos de por medio), estado (tiene sus propios servicios de transición) y vendedor.

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `SELLER` | `UnauthorizedOperationException` |
| Producto de otro vendedor | `OwnershipViolationException` |
| Producto inexistente | `EntityNotFoundException` |
| Producto `DISCONTINUED` | `DomainException` |
| Precio base menor o igual a cero | `DomainException` |

**Nota.** La validación de propiedad se hace sobre el producto **almacenado**, no sobre el recibido: un parámetro puede llegar con cualquier vendedor dentro.

---

## 4. PublishProductService

**Responsabilidad.** Hacer visible el producto en el catálogo público.

**Firma.** `Product execute(User user, Product product)`

**Autorización.** `AuthorizeSellerOperationService` + `ValidateSellerOwnershipService`.

**Transición.** `SUSPENDED --> PUBLISHED`

| Condición | Excepción |
| :--- | :--- |
| Producto de otro vendedor | `OwnershipViolationException` |
| Producto inexistente | `EntityNotFoundException` |
| Producto `DISCONTINUED` | `DomainException` |
| Producto ya `PUBLISHED` | `DomainException` |

**Regla.** Corresponde al paso "Publicación" del flujo del negocio: los productos se hacen visibles en el catálogo público como un acto explícito del vendedor, no como consecuencia automática del registro.

---

## 5. SuspendProductService

**Responsabilidad.** Retirar el producto de la vitrina sin descontinuarlo.

**Firma.** `Product execute(User user, Product product)`

**Transición.** `PUBLISHED --> SUSPENDED`

| Condición | Excepción |
| :--- | :--- |
| Producto de otro vendedor | `OwnershipViolationException` |
| Producto inexistente | `EntityNotFoundException` |
| Producto no `PUBLISHED` | `DomainException` |

---

## 6. DiscontinueProductService

**Responsabilidad.** Retirar el producto definitivamente del catálogo.

**Firma.** `Product execute(User user, Product product)`

**Transición.** Cualquier estado `--> DISCONTINUED`

| Condición | Excepción |
| :--- | :--- |
| Producto de otro vendedor | `OwnershipViolationException` |
| Producto inexistente | `EntityNotFoundException` |
| Producto ya `DISCONTINUED` | `DomainException` |

**Regla.** `DISCONTINUED` es **terminal**. Ningún servicio transiciona un producto fuera de ese estado, y `UpdateProductService` rechaza modificarlo. Los pedidos que ya contienen ese producto no se ven afectados, porque la línea de pedido guarda su propia fotografía del precio.

---

## 7. ConsultCatalogService

**Responsabilidad.** Devolver el catálogo público.

**Firma.** `List<Product> execute()`

**Sin autenticación.** Es alcanzable desde `PublicAccessPort` y también desde `BuyerPort`.

**Regla.** Devuelve exclusivamente productos `PUBLISHED`. Al no recibir criterio alguno del exterior, no existe forma de pedirle que exponga productos suspendidos o descontinuados.

---

## 8. ConsultProductService

**Responsabilidad.** Devolver un producto individual del catálogo público.

**Firma.** `Product execute(Product product)`

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Producto inexistente | `EntityNotFoundException` |
| Producto no `PUBLISHED` | `EntityNotFoundException` |

**Decisión de seguridad.** Un producto que existe pero no está publicado se reporta como **no encontrado**, no como "no autorizado". Distinguir ambos casos permitiría a cualquiera averiguar qué productos tiene un vendedor sin publicar.

---

## 9. ConsultSellerProductsService

**Responsabilidad.** Devolver los productos propios del vendedor, en cualquier estado.

**Firma.** `List<Product> execute(User user)`

**Autorización.** `AuthorizeSellerOperationService`.

**Regla.** RG-03. A diferencia del catálogo público, aquí sí aparecen los productos suspendidos y descontinuados, porque son del propio vendedor. El alcance lo impone la consulta al puerto (`findAllBySeller` con el vendedor autenticado), no un filtro posterior.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `SELLER` | `UnauthorizedOperationException` |
