# Puertos de Entrada — NexusMarket

## 1. Propósito

Los puertos de entrada definen los contratos a través de los cuales el exterior —un controlador REST, por ejemplo— invoca al dominio.

En esta arquitectura **los puertos de entrada se organizan estrictamente por rol**, más un puerto público para las operaciones que no requieren autenticación previa. Esto tiene una consecuencia de diseño importante: la Restricción General RG-03 ("ningún participante podrá administrar información fuera de su rol") deja de ser una condición dispersa en el código y se convierte en una frontera visible. Si una operación no está en el puerto de un rol, ese rol sencillamente no puede invocarla.

---

## 2. Principios

### 2.1 Un rol, una interfaz

| Puerto | Rol (`UserRole`) |
| :--- | :--- |
| `PublicAccessPort` | Público / sin autenticar |
| `BuyerPort` | `BUYER` |
| `SellerPort` | `SELLER` |
| `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` |
| `AdministratorPort` | `ADMINISTRATOR` |
| `SupervisorPort` | `SUPERVISOR` |

RG-02 establece un único rol por usuario, así que no hay solapamiento posible: un usuario se resuelve contra exactamente un puerto.

### 2.2 El usuario autenticado siempre viaja como primer parámetro

Todos los métodos, salvo los del puerto público, reciben el modelo de dominio `User` reconstruido a partir del token. Los servicios lo usan para dos cosas distintas y complementarias:

- **Autorizar**: validar rol y estado operativo.
- **Resolver**: obtener la información *propia* del usuario sin que el llamador pueda indicar de quién.

Por eso `consultMyCart(User user)` no recibe un identificador de carrito: el carrito se deduce del comprador autenticado, y así no existe forma de pedir el carrito ajeno.

### 2.3 Modelos de dominio, no primitivos

Los parámetros son modelos de dominio y value objects, nunca DTOs, entidades de persistencia ni atributos sueltos.

Correcto:

```java
Cart addItemToCart(User user, CartItem cartItem);
```

Incorrecto:

```java
Cart addItemToCart(String userId, String productId, int quantity);
```

---

## 3. PublicAccessPort

Operaciones accesibles sin autenticación previa.

```java
public interface PublicAccessPort {
    User login(User user);
    void logout(User user);
    Buyer registerBuyer(Buyer buyer);
    List<Product> consultPublicCatalog();
    Product consultPublicProduct(Product product);
}
```

| Operación | Servicio | Regla |
| :--- | :--- | :--- |
| `login` | `LoginService` | RG-01 |
| `logout` | `LogoutService` | — |
| `registerBuyer` | `RegisterBuyerUserService` | Sección 3.1 |
| `consultPublicCatalog` | `ConsultCatalogService` | Dominio 5, solo `PUBLISHED` |
| `consultPublicProduct` | `ConsultProductService` | Dominio 5 |

**El registro de vendedores está deliberadamente ausente.** El Dominio 3 establece que los vendedores no pueden auto-registrarse; son incorporados por el Administrador. Al no existir el método en este puerto, la regla no depende de una validación que alguien pueda olvidar.

---

## 4. BuyerPort

```java
public interface BuyerPort {
    Buyer consultMyProfile(User user);
    Buyer updateMyProfile(User user, Buyer buyer);

    List<Product> consultCatalog(User user);
    Product consultProduct(User user, Product product);

    Cart consultMyCart(User user);
    Cart addItemToCart(User user, CartItem cartItem);
    Cart updateCartItemQuantity(User user, CartItem cartItem);
    Cart removeItemFromCart(User user, CartItem cartItem);
    Order checkoutCart(User user);

    Order confirmOrderPayment(User user, Order order);
    List<Order> consultMyOrders(User user);
    Order consultMyOrder(User user, Order order);

    List<Invoice> consultMyInvoices(User user);
    Invoice consultOrderInvoice(User user, Order order);

    ReturnRefund requestReturn(User user, ReturnRefund returnRefund);
    List<ReturnRefund> consultMyReturns(User user);
}
```

El Dominio 2 establece que el comprador nunca administra información de otros compradores ni inventarios. Ninguna operación de inventario aparece aquí, y las que reciben un `Order` o un `Invoice` pasan por `ValidateBuyerOwnershipService` antes de devolver nada.

**SUPUESTO** sobre `confirmOrderPayment`: el documento indica que el pago se valida antes de iniciar el alistamiento, pero no nombra al actor que lo confirma. Se asume el comprador, en coherencia con la matriz de responsabilidades, que asigna la gestión de pedidos al Comprador. Trasladar la responsabilidad a otro rol implica únicamente mover el método de puerto y cambiar la autorización del servicio.

---

## 5. SellerPort

```java
public interface SellerPort {
    Seller consultMyProfile(User user);

    Product registerProduct(User user, Product product);
    Product updateProduct(User user, Product product);
    Product publishProduct(User user, Product product);
    Product suspendProduct(User user, Product product);
    Product discontinueProduct(User user, Product product);
    List<Product> consultMyProducts(User user);

    List<Warehouse> consultMyWarehouses(User user);
    Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement);
    Inventory adjustInventory(User user, InventoryMovement inventoryMovement);
    List<Inventory> consultMyInventory(User user);
    Inventory consultProductInventory(User user, Product product, Warehouse warehouse);

    List<Order> consultMyOrders(User user);
}
```

La matriz de responsabilidades asigna al vendedor el registro de productos y la administración de inventario. Todas las operaciones sobre un producto concreto validan propiedad mediante `ValidateSellerOwnershipService`: un vendedor no puede publicar, suspender ni descontinuar el producto de otro.

`consultMyOrders` devuelve los pedidos que contienen productos suyos, no todos los pedidos del sistema.

---

## 6. LogisticsOperatorPort

```java
public interface LogisticsOperatorPort {
    List<Inventory> consultWarehouseInventory(User user, Warehouse warehouse);
    Inventory registerInventoryInflow(User user, InventoryMovement inventoryMovement);
    Inventory adjustInventory(User user, InventoryMovement inventoryMovement);
    Inventory registerReturnToInventory(User user, InventoryMovement inventoryMovement);

    List<Order> consultOrdersToDispatch(User user);
    Shipment createShipment(User user, Shipment shipment);
    Shipment dispatchShipment(User user, Shipment shipment);
    Shipment completeShipment(User user, Shipment shipment);
    List<Shipment> consultShipments(User user);
}
```

Es el único rol que puede recibir mercancía devuelta al inventario (`registerReturnToInventory`) y el único que opera el ciclo del envío. La cola de despacho son los pedidos en estado `PAID`.

---

## 7. AdministratorPort

```java
public interface AdministratorPort {
    Seller registerSeller(User user, Seller seller, Warehouse firstWarehouse);
    Warehouse registerWarehouse(User user, Warehouse warehouse);
    Warehouse updateWarehouse(User user, Warehouse warehouse);
    List<Warehouse> consultWarehouses(User user);

    User registerUser(User user, User newUser);
    User changeUserStatus(User user, User targetUser, UserStatus newStatus);
    List<User> consultUsers(User user);
    List<Buyer> consultBuyers(User user);
    List<Seller> consultSellers(User user);
    Buyer changeBuyerCommercialStatus(User user, Buyer buyer);

    ReturnRefund approveReturn(User user, ReturnRefund returnRefund);
    ReturnRefund rejectReturn(User user, ReturnRefund returnRefund);
    ReturnRefund processRefund(User user, ReturnRefund returnRefund);
    List<ReturnRefund> consultReturns(User user);

    List<Order> consultAllOrders(User user);
    List<Inventory> consultAllInventory(User user);
}
```

`registerSeller` recibe **dos** modelos de dominio porque el paso "Incorporación" del flujo del negocio establece que el Administrador registra al vendedor *y su primera bodega*. La firma hace imposible crear un vendedor sin bodega.

`changeBuyerCommercialStatus` lleva el nuevo estado dentro del propio `Buyer`, coherente con la regla de no pasar atributos sueltos.

---

## 8. SupervisorPort

```java
public interface SupervisorPort {
    List<Order> consultOrders(User user);
    Order consultOrder(User user, Order order);
    List<Inventory> consultInventory(User user);
    List<Shipment> consultShipments(User user);
    List<ReturnRefund> consultReturns(User user);
    List<Product> consultCatalog(User user);
}
```

La Sección 5 define al Supervisor como "perfil de consulta y seguimiento operativo". **Todas las operaciones son de lectura**: ningún método de este puerto alcanza un servicio que modifique estado. Es una restricción verificable leyendo la interfaz, sin necesidad de auditar la implementación.

---

## 9. Implementación

Cada puerto se implementa en `adapters/useCases/` por una única clase:

| Puerto | Implementación |
| :--- | :--- |
| `PublicAccessPort` | `PublicAccessUseCaseImpl` |
| `BuyerPort` | `BuyerUseCaseImpl` |
| `SellerPort` | `SellerUseCaseImpl` |
| `LogisticsOperatorPort` | `LogisticsOperatorUseCaseImpl` |
| `AdministratorPort` | `AdministratorUseCaseImpl` |
| `SupervisorPort` | `SupervisorUseCaseImpl` |

Estas clases **no contienen reglas de negocio**: inyectan los servicios de dominio y traducen cada llamada del puerto a la invocación correspondiente. Toda la lógica vive en `domain/services/`.
