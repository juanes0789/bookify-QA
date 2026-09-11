src/main/java/udea/fabrica/bookify/
│
├── domain/                      ───► [NÚCLEO/CORE] Sin dependencias de Spring ni BD
│   ├── model/                   ───► Entidades puras del negocio
│   │   ├── Availability.java
│   │   ├── Booking.java
│   │   ├── BookingStatus.java   ───► Enums (CONFIRMED, CANCELED)
│   │   └── Service.java
│   │
│   ├── port/                    ───► Contratos/Interfaces (Puertos)
│   │   ├── in/                  ───► Casos de Uso (Entrada)
│   │   │   ├── CreateAvailabilityUseCase.java
│   │   │   ├── CreateBookingUseCase.java
│   │   │   ├── CancelBookingUseCase.java
│   │   │   └── GetAvailabilityUseCase.java
│   │   │
│   │   └── out/                 ───► Interfaces de Repositorio (Salida)
│   │       ├── AvailabilityRepositoryPort.java
│   │       └── BookingRepositoryPort.java
│   │
│   ├── service/                 ───► Implementación de la lógica de negocio
│   │   ├── AvailabilityService.java
│   │   └── BookingService.java
│   │
│   └── exception/               ───► Excepciones propias del dominio
│       ├── DoubleBookingException.java
│       └── ScheduleConflictException.java
│
└── infrastructure/              ───► [ADAPTADORES] Dependencias de Spring Boot, BD, Web
    │
    ├── adapter/
    │   ├── input/
    │   │   └── rest/            ───► Adaptadores de entrada (HTTP / REST Controllers)
    │   │       ├── controller/
    │   │       │   ├── AvailabilityController.java (HU-01)
    │   │       │   ├── BookingController.java      (HU-03, HU-04)
    │   │       │   └── CatalogController.java      (HU-02, HU-05)
    │   │       │
    │   │       ├── dto/
    │   │       │   ├── request/ ───► CreateBookingRequest.java, etc.
    │   │       │   └── response/───► BookingResponse.java, etc.
    │   │       │
    │   │       └── mapper/      ───► Conversión entre DTOs y Objetos del Dominio
    │   │
    │   └── output/
    │       └── persistence/     ───► Adaptadores de salida (Supabase / Spring Data JPA)
    │           ├── entity/      ───► Entidades anotadas con @Entity / @Table
    │           │   ├── AvailabilityEntity.java
    │           │   └── BookingEntity.java
    │           │
    │           ├── repository/  ───► Interfaces que extienden de JpaRepository
    │           │   ├── SpringDataAvailabilityRepository.java
    │           │   └── SpringDataBookingRepository.java
    │           │
    │           ├── adapter/     ───► Implementación del puerto de salida de dominio
    │           │   ├── AvailabilityPersistenceAdapter.java
    │           │   └── BookingPersistenceAdapter.java
    │           │
    │           └── mapper/      ───► Conversión entre Domain Model y JPA Entity
    │
    └── config/                  ───► Configuración general (Beans, OpenAPI/Swagger, Cors)
        └── BeanConfiguration.java


Explicación rápida
domain/ (Cero dependencias de framework):

Aquí viven las reglas del sistema. No debe llevar anotaciones de Spring (@Service, @Autowired, @Entity) ni imports de jakarta.persistence.*.

Si el Dev 2 va a resolver la validación de doble reserva, esa regla matemática/temporal vive pura en domain/service/BookingService.java.

infrastructure/adapter/input/rest/ (Controladores HTTP):

Cada desarrollador crea sus Controllers según la asignación de HUs. Reciben Request DTOs, llaman al Puerto de Entrada (port/in), mapean el resultado a un Response DTO y devuelven la respuesta HTTP (200 OK, 201 Created, 400 Bad Request).

infrastructure/adapter/output/persistence/ (Persistencia / Supabase):

Aquí van las Entidades de JPA (@Entity) que mapean exactamente las tablas de Supabase.

La clase Adapter implementa la interfaz declarada en domain/port/out/ conectándola con la interfaz JpaRepository.

infrastructure/config/BeanConfiguration.java:

Dado que las clases en domain/service/ no llevan @Service, se registran como Beans de Spring en esta clase de configuración para que puedan inyectarse normalmente en los Controllers.