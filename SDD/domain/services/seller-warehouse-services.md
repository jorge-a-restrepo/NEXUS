# Servicios de Vendedores y Bodegas

## 1. Propósito

Cubre el Dominio 3 (Gestión de Vendedores) y el Dominio 4 (Gestión de Bodegas).

La regla que define este subdominio es explícita en el documento: **"Los vendedores no pueden auto-registrarse; son incorporados por el Administrador"**. Y el paso "Incorporación" del flujo del negocio la completa: *el Administrador registra al vendedor y su primera bodega*.

---

## 2. RegisterSellerService

**Responsabilidad.** Incorporar un vendedor junto a su primera bodega.

**Firma.** `Seller execute(User user, Seller seller, Warehouse firstWarehouse)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Por qué recibe dos modelos.** La firma hace estructuralmente imposible crear un vendedor sin bodega. La regla del paso "Incorporación" no depende de que alguien recuerde crear la bodega después: si no llega, la operación no procede.

**Flujo.**

```text
1. Autorizar administrador
2. Validar datos del vendedor: nombre, correo, documento, contraseña
3. Validar que llegue la primera bodega con identificador
4. Verificar unicidad de correo y documento en toda la plataforma
5. Verificar unicidad del identificador de bodega
6. Hashear la contraseña
7. Asignar rol SELLER y estado ACTIVE
8. Guardar el vendedor
9. Marcar la bodega como tipo SELLER y asociarla al vendedor guardado
10. Guardar la bodega
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Vendedor ausente | `DomainException` |
| Primera bodega ausente | `DomainException` |
| Nombre, correo, documento o contraseña vacíos | `DomainException` |
| Identificador de bodega vacío | `DomainException` |
| Correo o documento ya registrados | `DomainException` |
| Identificador de bodega ya existente | `DomainException` |

**Efectos.**

- Vendedor con rol `SELLER` y estado `ACTIVE`.
- Bodega de tipo `SELLER` asociada a ese vendedor mediante `Warehouse.seller`.

El rol se asigna dentro del servicio y no se acepta del exterior. La asociación de la bodega con su dueño es la que después permite a `ValidateSellerOwnershipService` y a `ConsultWarehouseService` aislar lo propio de cada vendedor (RG-03).

---

## 3. ConsultSellerProfileService

**Responsabilidad.** Devolver el perfil del vendedor autenticado.

**Firma.** `Seller execute(User user)`

**Autorización.** `AuthorizeSellerOperationService`.

**Decisión de diseño.** Igual que con el comprador, no recibe identificador: el perfil se resuelve del usuario autenticado.

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `SELLER` | `UnauthorizedOperationException` |
| Vendedor inexistente | `EntityNotFoundException` |

---

## 4. ConsultSellersService

**Responsabilidad.** Listar los vendedores registrados.

**Firma.** `List<Seller> execute(User user)`

**Autorización.** `AuthorizeAdministrativeConsultationService`: Administrador (OBJ-02) y Supervisor (OBJ-12).

| Condición | Excepción |
| :--- | :--- |
| Rol distinto de `ADMINISTRATOR` o `SUPERVISOR` | `UnauthorizedOperationException` |

---

## 5. RegisterWarehouseService

**Responsabilidad.** Dar de alta una bodega.

**Firma.** `Warehouse execute(User user, Warehouse warehouse)`

**Autorización.** `AuthorizeAdministratorOperationService`. La Matriz de Responsabilidades asigna la administración de bodegas al Administrador.

**Flujo.**

```text
1. Autorizar administrador
2. Validar identificador y tipo de bodega
3. Verificar que el identificador no exista
4. Guardar
```

**Validaciones y excepciones.**

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Bodega ausente | `DomainException` |
| Identificador vacío | `DomainException` |
| Tipo no indicado | `DomainException` |
| Identificador ya existente | `DomainException` |

**Por qué el identificador debe ser único.** El inventario se vincula al par (producto, bodega). Dos bodegas con el mismo identificador harían ambiguo ese vínculo y romperían la trazabilidad del Dominio 6.

**Por qué el tipo es obligatorio.** El Dominio 4 distingue bodegas del Marketplace de bodegas de vendedores; una bodega sin clasificar no pertenece a ninguna de las dos categorías que el negocio reconoce.

---

## 6. UpdateWarehouseService

**Responsabilidad.** Actualizar la información de una bodega existente.

**Firma.** `Warehouse execute(User user, Warehouse warehouse)`

**Autorización.** `AuthorizeAdministratorOperationService`.

**Campo no modificable.** El `identifier` es la identidad de la bodega y los registros de inventario están amarrados a él: cambiarlo dejaría existencias apuntando a una bodega que ya no existe con ese nombre.

| Condición | Excepción |
| :--- | :--- |
| Solicitante no administrador | `UnauthorizedOperationException` |
| Bodega ausente | `DomainException` |
| Bodega inexistente | `EntityNotFoundException` |

---

## 7. ConsultWarehouseService

**Responsabilidad.** Consultar bodegas, con alcance distinto según quién pregunte.

**Firmas.**

```java
List<Warehouse> executeAll(User user);          // consolidado
List<Warehouse> executeMyWarehouses(User user); // propias del vendedor
Warehouse execute(User user, Warehouse warehouse);
```

**Autorización.**

| Método | Autorización | Alcance |
| :--- | :--- | :--- |
| `executeAll` | `AuthorizeAdministrativeConsultationService` | Todas las bodegas (OBJ-04, OBJ-12) |
| `executeMyWarehouses` | `AuthorizeSellerOperationService` | Solo las del vendedor autenticado (RG-03) |
| `execute` | `AuthorizeAdministrativeConsultationService` | Una bodega concreta |

| Condición | Excepción |
| :--- | :--- |
| Rol no autorizado para el alcance pedido | `UnauthorizedOperationException` |
| Bodega inexistente | `EntityNotFoundException` |

**Decisión de diseño.** El aislamiento del vendedor no se implementa filtrando después de traer todo: `executeMyWarehouses` consulta el puerto con el vendedor autenticado (`findAllBySeller`), de modo que las bodegas ajenas nunca llegan a memoria. Un filtro posterior es una fuga esperando a que alguien lo olvide.
