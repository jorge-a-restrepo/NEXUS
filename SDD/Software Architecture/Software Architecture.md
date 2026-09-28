# Arquitectura de Software — NexusMarket

## 1. Visión general

NexusMarket implementa una **Arquitectura Hexagonal (Puertos y Adaptadores)** combinada con principios de **Diseño Dirigido por el Dominio (DDD)**.

El objetivo es aislar las reglas del negocio de cualquier tecnología concreta: el dominio no sabe si la información llega por HTTP, si se guarda en una base relacional o no relacional, ni qué framework arranca la aplicación. Esa independencia es la que permite que las reglas de la especificación funcional se lean directamente en el código.

---

## 2. Principios aplicados

- El dominio primero: las reglas viven en `domain/`, nunca en los adaptadores.
- Inversión de dependencias: el dominio define las interfaces, los adaptadores las implementan.
- Fronteras explícitas entre capas.
- Alta cohesión por subdominio, bajo acoplamiento entre subdominios.
- Un servicio de dominio equivale a un caso de uso del negocio.

---

## 3. Estructura de paquetes

```text
nexus/src/main/java/application/
│
├── NexusApplication.java
│
├── adapters/
│   └── useCases/                     <-- Implementaciones de los puertos de entrada
│       ├── PublicAccessUseCaseImpl.java
│       ├── BuyerUseCaseImpl.java
│       ├── SellerUseCaseImpl.java
│       ├── LogisticsOperatorUseCaseImpl.java
│       ├── AdministratorUseCaseImpl.java
│       └── SupervisorUseCaseImpl.java
│
└── domain/
    ├── (modelos de dominio y value objects)
    ├── exceptions/                   <-- Excepciones de negocio
    ├── ports/
    │   ├── in/                       <-- Puertos de entrada por rol
    │   └── out/                      <-- Puertos de salida
    └── services/                     <-- Servicios de dominio por subdominio
        ├── authorization/
        ├── user/
        ├── buyer/
        ├── seller/
        ├── warehouse/
        ├── catalog/
        ├── inventory/
        ├── cart/
        ├── order/
        ├── invoice/
        ├── shipment/
        └── returnrefund/
```

Las capas de persistencia (`adapters/persistence/`) y de entrega REST (`adapters/rest/`) no forman parte de esta etapa: los puertos de salida ya definen su contrato para que se implementen sin tocar el dominio.

---

## 4. Flujo de una operación

```text
Adaptador de entrega (REST)
        |
        v
Puerto de entrada por rol  (domain/ports/in/)
        |
        v
Caso de uso               (adapters/useCases/)
        |
        v
Servicio de dominio       (domain/services/)
        |
        +--> valida autorización por rol y estado (RG-01, RG-02, RG-03)
        +--> valida las reglas del negocio
        |
        v
Puerto de salida          (domain/ports/out/)
        |
        v
Adaptador de salida       (persistencia, seguridad)
```

Ejemplo concreto del checkout:

```text
BuyerPort.checkoutCart(user)
        |
        v
BuyerUseCaseImpl
        |
        v
CheckoutCartService
        |
        +--> AuthorizeBuyerOperationService     (rol BUYER y estado ACTIVE)
        +--> ValidateBuyerOwnershipService      (el carrito es suyo)
        +--> ReserveInventoryService            (stock disponible, no dañado, nunca negativo)
        |
        v
CartRepositoryPort / OrderRepositoryPort / OrderItemRepositoryPort
```

---

## 5. Responsabilidad de cada capa

### 5.1 Modelos de dominio

Representan las entidades del negocio y sus relaciones. No conocen persistencia ni transporte.

### 5.2 Value Objects y enumeraciones

Encapsulan los valores acotados del negocio (estados, tipos, roles). Evitan que un estado inválido pueda siquiera representarse.

### 5.3 Puertos de entrada (`domain/ports/in/`)

Definen qué puede hacer **cada rol**. Un rol equivale a una interfaz. Esto hace que el permiso sea una decisión de diseño visible y no una condición dispersa en el código.

### 5.4 Servicios de dominio (`domain/services/`)

Contienen la lógica del negocio. Un servicio equivale a un caso de uso y expone un método `execute`. Reciben modelos de dominio, nunca DTOs, entidades de persistencia ni identificadores sueltos.

### 5.5 Puertos de salida (`domain/ports/out/`)

Interfaces que el dominio usa para hablar con el exterior: persistencia, hash de contraseñas y emisión de tokens. El dominio las declara; los adaptadores las implementan.

### 5.6 Casos de uso (`adapters/useCases/`)

Implementan los puertos de entrada e inyectan los servicios de dominio. **No contienen reglas de negocio**: traducen la llamada del puerto al servicio correspondiente.

### 5.7 Excepciones (`domain/exceptions/`)

Todas heredan de `DomainException`. El dominio nunca lanza excepciones de framework ni de persistencia.

---

## 6. Regla de dependencias

```text
adapters  ---->  domain
domain    -X->   adapters
```

El dominio no importa nada de los adaptadores. La única concesión es la anotación `@Service` de Spring sobre los servicios, usada para el registro automático de beans siguiendo el proyecto de referencia del curso; puede retirarse declarando los beans en una clase de configuración sin cambiar una sola regla de negocio.

---

## 7. Trazabilidad con la especificación funcional

| Elemento de la especificación | Dónde vive en el código |
| :--- | :--- |
| RG-01 (operación autenticada) | `LoginService`, `ValidateUserStatusService` |
| RG-02 (un único rol por usuario) | `UserRole`, `ValidateRoleAuthorizationService` |
| RG-03 (no actuar fuera del rol) | `domain/ports/in/` + `domain/services/authorization/` |
| Dominio 2 (aislamiento del comprador) | `ValidateBuyerOwnershipService` |
| Dominio 3 (alta de vendedores) | `RegisterSellerService` |
| Dominio 5 (catálogo, físico vs digital) | `domain/services/catalog/`, `ProductType` |
| Dominio 6 (inventario distribuido, sin negativos) | `domain/services/inventory/` |
| Dominio 7 (ciclo del pedido, inmutabilidad) | `domain/services/order/`, `OrderStatus` |
| Validaciones críticas (§11) | `ReserveInventoryService`, `MarkOrderDeliveredService`, servicios de registro |
| Matriz de responsabilidades (§12) | Puertos de entrada por rol |
