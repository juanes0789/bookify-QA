ADR 01: Adopción de Arquitectura Hexagonal (Ports & Adapters)
Estado: Aceptado

Prioridad: Alta (Crítica para el diseño y mantenibilidad)

Contexto:
Necesitábamos construir una aplicación robusta en Spring Boot capaz de desacoplar la lógica de negocio del framework, de la base de datos relacional y de las interfaces de entrada (REST API). El objetivo es facilitar la prueba de unidades de negocio sin acoplarse a detalles de infraestructura.

Decisión:
Implementar Arquitectura Hexagonal. Se estructuró el proyecto separándolo estrictamente en tres capas: Dominio (reglas de negocio puras, modelos y puertos de entrada/salida), Aplicación/Servicios (casos de uso) e Infraestructura (controladores REST, adaptadores de persistencia con JPA/Hibernate y configuración de Beans).

Consecuencias:

Positivas: Alta testabilidad de la lógica de negocio, independencia de la base de datos y facilidad para cambiar frameworks o interfaces web en el futuro.

Negativas: Mayor cantidad de clases repetidas (mappers, DTOs, entidades de dominio vs entidades JPA) y curva de aprendizaje inicial más alta para estructurar los paquetes.