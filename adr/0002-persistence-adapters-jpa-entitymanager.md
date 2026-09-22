ADR 02: Uso de Adaptadores de Persistencia basados en JPA y EntityManager
Estado: Aceptado

Prioridad: Media (Crítica para desacoplar el dominio de la persistencia)

Contexto:
Siguiendo los principios de la Arquitectura Hexagonal, el dominio de la aplicación no debe conocer detalles de librerías de persistencia externas (como Hibernate o JPA), por lo que se requería una estrategia para mapear las entidades de base de datos sin contaminar las reglas de negocio.

Decisión:
Implementar puertos de salida en el dominio y adaptadores de persistencia en la infraestructura utilizando JPA y EntityManager para realizar operaciones de guardado, consultas y manejo de entidades relacionales hacia PostgreSQL.

Consecuencias:

Positivas: El núcleo del negocio permanece totalmenteagnóstico a la tecnología de bases de datos, permitiendo cambiar el ORM o la base de datos en el futuro sin alterar la lógica de negocio.

Negativas: Se requiere escribir código repetitivo (boilerplate) para mapear manualmente los datos entre los modelos de dominio y las entidades JPA.