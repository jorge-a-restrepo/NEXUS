# Modelo de Dominio — NexusMarket

## Introducción

NexusMarket es una plataforma marketplace que funciona como intermediario entre compradores y vendedores.

El sistema permite administrar usuarios, productos, inventario, bodegas, carritos, pedidos y otros procesos relacionados con la compra y entrega de productos.

El modelo de dominio se construyó tomando como base la Especificación Funcional del Negocio de NexusMarket.

Cuando una clase o atributo no está definido directamente en el documento, se indica como inferido o como decisión de modelado.

# Jerarquía de Clases de Dominio

```text
Usuario (Abstracta)
├── Comprador
├── Vendedor
├── OperadorLogístico
├── Administrador
└── Supervisor
```

Las cinco clases anteriores heredan de Usuario porque representan los diferentes roles que pueden participar en el sistema.

Las demás clases del modelo no tienen una relación de herencia definida.

# Relaciones del Dominio



Usuario
   │
   ├── Comprador
   ├── Vendedor
   ├── OperadorLogístico
   ├── Administrador
   └── Supervisor

Administrador
   └── incorpora ─────────> Vendedor

Vendedor
   ├── tiene ─────────────> Bodega
   └── registra ──────────> Producto

Producto
   └── tiene ─────────────> Inventario

Bodega
   └── contiene ──────────> Inventario

Inventario
   └── registra ──────────> MovimientoInventario

Usuario
   └── realiza ───────────> MovimientoInventario

Comprador
   ├── tiene ─────────────> Carrito
   └── realiza ───────────> Pedido

Carrito
   └── contiene ──────────> ItemCarrito

ItemCarrito
   └── referencia ────────> Producto

Pedido
   ├── contiene ──────────> ItemPedido
   ├── genera ────────────> Factura
   ├── genera ────────────> Envío
   └── puede generar ─────> Devolución/Reembolso

ItemPedido
   └── referencia ────────> Producto

Envío
   ├── sale de ───────────> Bodega
   └── es gestionado por ─> OperadorLogístico


Relaciones del Dominio

| # | Clase | Multiplicidad | Relación | Multiplicidad | Clase |
|---|---|---:|---|---:|---|
| 1 | Administrador | 1 | Asociación | 0..N | Vendedor |
| 2 | Vendedor | 1 | Agregación | 1..N | Bodega |
| 3 | Vendedor | 1 | Agregación | 0..N | Producto |
| 4 | Producto | 1 | Asociación | 0..N | Inventario |
| 5 | Bodega | 1 | Asociación | 0..N | Inventario |
| 6 | Inventario | 1 | Composición | 0..N | MovimientoInventario |
| 7 | Usuario | 1 | Asociación | 0..N | MovimientoInventario |
| 8 | Comprador | 1 | Asociación | 0..N | Carrito |
| 9 | Carrito | 1 | Composición | 0..N | ItemCarrito |
| 10 | ItemCarrito | 1 | Asociación | 1 | Producto |
| 11 | Comprador | 1 | Asociación | 0..N | Pedido |
| 12 | Pedido | 1 | Composición | 1..N | ItemPedido |
| 13 | ItemPedido | 1 | Asociación | 1 | Producto |
| 14 | Pedido | 1 | Asociación | 0..1 | Factura |
| 15 | Pedido | 1 | Asociación | 0..N | Envío |
| 16 | Envío | N | Asociación | 1 | Bodega |
| 17 | Envío | N | Asociación | 1 | OperadorLogístico |
| 18 | Pedido | 1 | Asociación | 0..N | Devolución/Reembolso |

# Entidades

## Usuario

### Descripción

Representa a los usuarios que pueden interactuar con el sistema NexusMarket.

Es la clase base para los diferentes roles definidos en el sistema.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| identificador | Cadena | Identificador único del usuario. |
| documentoIdentidad | Cadena | Documento de identidad del usuario. |
| nombreCompleto | Cadena | Nombre completo del usuario. |
| correoElectronico | Cadena | Correo electrónico del usuario. |
| rol | RolUsuario | Rol asignado al usuario dentro del sistema. |
| estado | EstadoUsuario | Estado del usuario dentro del sistema. |
| fechaRegistro | Fecha | Fecha en la que se registra el usuario. |

### Relaciones

- Un Usuario puede tener uno de los roles definidos para el sistema.
- Un Usuario puede registrar movimientos de inventario como responsable de la operación.

---

## Comprador

### Descripción

Representa al usuario que realiza compras de productos dentro de NexusMarket.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| direccionPrincipal | Cadena | Dirección principal del comprador. |
| direccionesAdicionales | Lista | Otras direcciones registradas por el comprador. |
| estadoComercial | EstadoComercial | Estado comercial del comprador. |

### Relaciones

- Un Comprador puede tener varios Carritos.
- Un Comprador puede realizar varios Pedidos.

---

## Vendedor

### Descripción

Representa al usuario encargado de registrar y administrar sus propios productos.

Los vendedores son incorporados al sistema por un Administrador.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| nombreComercial | Cadena | Nombre comercial del vendedor. |
| fechaIncorporacion | Fecha | Fecha en la que el vendedor es incorporado al sistema. |
| administradorResponsable | Administrador | Administrador que incorpora al vendedor. |
| bodegasAsociadas | Lista | Bodegas asociadas al vendedor. |

### Relaciones

- Un Vendedor es incorporado por un Administrador.
- Un Vendedor tiene una o más Bodegas.
- Un Vendedor puede registrar varios Productos.

---

## OperadorLogístico

### Descripción

Representa al usuario encargado de las actividades relacionadas con la operación logística.

### Relaciones

- Un OperadorLogístico puede estar asociado a varios Envíos.

---

## Administrador

### Descripción

Representa al usuario encargado de administrar vendedores y bodegas dentro del sistema.

### Relaciones

- Un Administrador puede incorporar varios Vendedores.

---

## Supervisor

### Descripción

Representa al usuario encargado de realizar funciones de consulta y seguimiento dentro del sistema.

### Relaciones

No tiene relaciones propias adicionales definidas en el documento.

---

## Bodega

### Descripción

Representa un lugar donde se almacena el inventario de los productos.

El sistema distingue entre bodegas del Marketplace y bodegas de vendedores.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| tipoBodega | TipoBodega | Indica si la bodega pertenece al Marketplace o a un Vendedor. |
| vendedorPropietario | Vendedor | Vendedor propietario cuando corresponde. |
| ubicacion | Cadena | Ubicación de la bodega. |
| estado | EstadoBodega | Estado de la bodega. |

### Relaciones

- Una Bodega puede estar asociada a un Vendedor.
- Una Bodega puede tener registros de Inventario.
- Una Bodega puede ser utilizada como origen de Envíos.

---

## Producto

### Descripción

Representa un producto publicado por un vendedor dentro del catálogo de NexusMarket.

Los productos pueden ser físicos o digitales.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| tipoProducto | TipoProducto | Indica si el producto es Físico o Digital. |
| variantes | Lista | Lista de variantes del producto, como color, talla o modelo. |
| estado | EstadoProducto | Estado del producto: Publicado, Suspendido o Descontinuado. |
| precioBase | Decimal | Precio base del producto. |

### Relaciones

- Un Producto pertenece a un Vendedor.
- Un Producto puede estar relacionado con registros de Inventario.
- Un Producto puede aparecer en Items de Carrito.
- Un Producto puede aparecer en Items de Pedido.

### Nota

Las variantes se manejan como una lista dentro de Producto y no como una clase independiente.

---

## Inventario

### Descripción

Representa las existencias de un Producto en una Bodega determinada.

El inventario es distribuido entre diferentes bodegas.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| producto | Producto | Producto al que pertenece el inventario. |
| bodega | Bodega | Bodega donde se encuentra el inventario. |
| cantidadDisponible | Entero | Cantidad disponible del producto. |
| estadoStock | EstadoStock | Estado actual del stock. |

### Relaciones

- Un Inventario está asociado a un Producto.
- Un Inventario está asociado a una Bodega.
- Un Inventario puede tener varios MovimientosInventario.

### Regla

La cantidad disponible del inventario no puede ser negativa.

---

## MovimientoInventario

### Descripción

Representa un movimiento realizado sobre el inventario.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| inventario | Inventario | Inventario sobre el que se realiza el movimiento. |
| tipoMovimiento | TipoMovimiento | Tipo de movimiento realizado. |
| cantidad | Entero | Cantidad involucrada en el movimiento. |
| fecha | Fecha | Fecha en que se realiza el movimiento. |
| referencia | Cadena | Referencia relacionada con el movimiento. |
| usuarioResponsable | Usuario | Usuario que realiza la operación. |

### Relaciones

- Un MovimientoInventario pertenece a un Inventario.
- Un MovimientoInventario es realizado por un Usuario.

### Tipos de movimiento

- Ingreso
- Reserva
- SalidaPorVenta
- Ajuste
- Devolución

---

## Carrito

### Descripción

Representa los productos que un comprador selecciona antes de realizar un pedido.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| comprador | Comprador | Comprador al que pertenece el carrito. |
| fechaCreacion | Fecha | Fecha de creación del carrito. |
| estado | EstadoCarrito | Estado actual del carrito. |

### Relaciones

- Un Carrito pertenece a un Comprador.
- Un Carrito contiene ItemsCarrito.

---

## ItemCarrito

### Descripción

Representa un producto incluido dentro de un Carrito.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| carrito | Carrito | Carrito al que pertenece el ítem. |
| producto | Producto | Producto seleccionado. |
| cantidad | Entero | Cantidad del producto seleccionada. |

### Relaciones

- Un ItemCarrito pertenece a un Carrito.
- Un ItemCarrito representa un Producto.

---

## Pedido

### Descripción

Representa una compra realizada por un Comprador.

El pedido pasa por diferentes estados durante su ciclo de vida.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| comprador | Comprador | Comprador que realiza el pedido. |
| estado | EstadoPedido | Estado actual del pedido. |
| fechaCreacion | Fecha | Fecha de creación del pedido. |
| montoTotal | Decimal | Valor total del pedido. |

### Relaciones

- Un Pedido pertenece a un Comprador.
- Un Pedido contiene uno o más ItemsPedido.
- Un Pedido puede tener una Factura.
- Un Pedido puede tener uno o varios Envíos.
- Un Pedido puede tener varias Devoluciones/Reembolsos.

### Estados del pedido

```text
Carrito
→ Pendiente de Pago
→ Pagado
→ Despachado
→ Entregado/Finalizado
```

### Regla

Un pedido finalizado no puede modificarse.

---

## ItemPedido

### Descripción

Representa un producto que forma parte de un Pedido.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| pedido | Pedido | Pedido al que pertenece el ítem. |
| producto | Producto | Producto comprado. |
| cantidad | Entero | Cantidad comprada. |
| precioUnitario | Decimal | Precio del producto en el momento de la compra. |

### Relaciones

- Un ItemPedido pertenece a un Pedido.
- Un ItemPedido representa un Producto.

### Nota

El precioUnitario se guarda como referencia del precio utilizado en el momento de la compra.

---

# Clases Inferidas

Las siguientes clases se incluyen porque los objetivos funcionales del documento mencionan facturación, envíos/logística y devoluciones/reembolsos, pero estos dominios no están desarrollados con atributos propios en la especificación fuente.

## Factura

### Descripción

Clase inferida para representar la facturación asociada a un Pedido.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| pedido | Pedido | Pedido relacionado con la factura. |
| montoTotal | Decimal | Valor total facturado. |
| fechaEmision | Fecha | Fecha de emisión de la factura. |

### Relaciones

- Una Factura está asociada a un Pedido.

### Nota

Los atributos de esta clase son inferidos y no están definidos directamente en el documento fuente.

---

## Envío

### Descripción

Clase inferida para representar el proceso de despacho y entrega de un pedido.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| pedido | Pedido | Pedido que se está enviando. |
| bodegaOrigen | Bodega | Bodega desde la cual se realiza el envío. |
| operadorLogistico | OperadorLogístico | Operador encargado del envío. |
| estado | EstadoEnvio | Estado actual del envío. |
| fechaDespacho | Fecha | Fecha en la que se realiza el despacho. |
| fechaEntrega | Fecha | Fecha de entrega cuando corresponde. |

### Relaciones

- Un Envío pertenece a un Pedido.
- Un Envío tiene una Bodega como origen.
- Un Envío puede estar asociado a un OperadorLogístico.

### Nota

La clase y sus atributos son inferidos a partir del alcance funcional de logística y envíos.

---

## Devolución/Reembolso

### Descripción

Clase inferida para representar una devolución o reembolso relacionado con un Pedido.

### Atributos

| Atributo | Tipo | Descripción |
|---|---|---|
| pedido | Pedido | Pedido relacionado con la devolución o reembolso. |
| motivo | Cadena | Motivo de la devolución o reembolso. |
| estado | EstadoDevolucionReembolso | Estado de la solicitud. |
| montoReembolsado | Decimal | Valor asociado al reembolso. |

### Relaciones

- Una Devolución/Reembolso está asociada a un Pedido.

### Nota

La clase es inferida porque el documento menciona devoluciones y reembolsos dentro del alcance, pero no desarrolla sus atributos.

# Reglas de Negocio

## RG-01

Toda operación requiere un usuario autenticado.

## RG-02

Cada usuario tiene un único rol.

## RG-03

Ningún participante administra información fuera de su rol.

## RG-04

El comprador no administra información de otros compradores ni inventarios.

## RG-05

El documento de identidad y el correo electrónico deben ser únicos en toda la plataforma.

## RG-06

No se permiten existencias de inventario negativas.

## RG-07

No se puede reservar inventario inexistente o marcado como Dañado.

## RG-08

Un pedido finalizado no puede modificarse.

## RG-09

Los vendedores son incorporados por un Administrador y deben contar con una bodega inicial.

## RG-10

El inventario debe estar relacionado con un Producto y una Bodega.

# Decisiones de Modelado

## Variante

Variante no se modela como una clase independiente.

El documento describe las variantes como una lista dentro de Producto, por lo que se mantienen como un atributo de Producto.

## Precio base

Se mantiene `precioBase` como atributo de Producto para representar el precio base del producto.

Este atributo se considera una decisión de modelado porque no está desarrollado explícitamente como atributo dentro del documento fuente.

## Carrito y Pedido

Carrito y Pedido se manejan como clases separadas.

El Carrito representa la selección de productos antes de realizar el pedido y el Pedido representa la compra.

## Devolución/Reembolso

Devolución/Reembolso se mantiene como una sola clase debido al nivel de detalle de la especificación fuente.

Separar ambas en clases diferentes agregaría mayor detalle al modelo que no está definido en el documento.

## Clases inferidas

Factura, Envío y Devolución/Reembolso son clases inferidas.

Se incluyen porque los procesos correspondientes aparecen dentro del alcance funcional del sistema, aunque el documento fuente no desarrolla estos dominios con el mismo nivel de detalle que usuarios, catálogo, inventario y pedidos.

# Resumen

El modelo de dominio de NexusMarket está compuesto por 17 clases.

La clase Usuario funciona como clase base para los diferentes roles del sistema. El resto de las clases representan los conceptos principales relacionados con vendedores, productos, bodegas, inventario, carritos y pedidos.

Las clases Factura, Envío y Devolución/Reembolso se mantienen identificadas como inferidas debido a que la especificación fuente no desarrolla estos dominios con atributos propios.
