# 📖 Bookify (Reserva PLUS) - Backend API

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-Build-blue?style=flat-square&logo=gradle)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Container-blue?style=flat-square&logo=docker)
![Render](https://img.shields.io/badge/Render-Deployed-black?style=flat-square&logo=render)

Sistema backend para la gestión de reservas y servicios (**Bookify / Reserva PLUS**), desarrollado como parte del **Sprint 1** para el módulo de Arquitectura de Software en Fábrica de Software (Universidad de Antioquia).

## 🏗 Arquitectura

El proyecto está diseñado bajo los principios de la **Arquitectura Hexagonal (Ports & Adapters)**, garantizando el aislamiento total de la lógica de negocio respecto a los detalles de infraestructura.

- **Dominio:** Contiene los modelos de negocio puros y las interfaces (puertos) de entrada y salida.
- **Aplicación (Casos de Uso):** Orquesta la lógica de negocio coordinando el dominio con los puertos de salida.
- **Infraestructura (Adaptadores):** Implementa las interfaces de entrada (Controladores REST) y de salida (Adaptadores de persistencia mediante JPA y `EntityManager`).

## 🚀 Tecnologías y Herramientas

- **Lenguaje:** Java 17
- **Framework:** Spring Boot
- **Gestor de Dependencias:** Gradle
- **Base de Datos:** PostgreSQL gestionado en Supabase (Conexión mediante Transaction Pooler puerto 6543 y SSL).
- **Despliegue:** Contenedor Docker desplegado automáticamente en Render.
- **Testing:** JUnit (Pruebas unitarias) y Postman (Pruebas de integración End-to-End).

## ⚙️ Funcionalidades Implementadas (Sprint 1)

El sistema expone una API RESTful con los siguientes endpoints principales:

- **Catálogo de Servicios:** `GET /api/v1/catalog/services`
- **Gestión de Disponibilidad:** `POST /api/v1/disponibilidades`
- **Creación de Reservas (HU-03):** `POST /api/v1/bookings`
- **Cancelación de Reservas (HU-04):** `PATCH /api/v1/bookings/{id}/cancel`
- **Historial de Reservas por Cliente:** `GET /api/v1/reservas/historial` (Requiere Header `X-Cliente-Id`)

## 🛠️ Ejecución Local

### Prerrequisitos
- Java 17+
- Gradle
- Docker (Opcional, para contenedorización local)

### Pasos
1. Clonar el repositorio.
2. Configurar las variables de entorno para la base de datos (Supabase) en tu archivo `application.properties` o `.env`:
   ```properties
   SPRING_DATASOURCE_URL=jdbc:postgresql://:6543/postgres?sslmode=require
   SPRING_DATASOURCE_USERNAME=
   SPRING_DATASOURCE_PASSWORD=
   ```
3. Ejecutar el proyecto mediante Gradle:
  ```bash
  ./gradlew bootRun
  ```

## ☁️ Despliegue en Producción

La aplicación se encuentra dockerizada e integrada mediante un pipeline de CI/CD continuo en Render.

- **URL Base de la API:** `https://bookify-pb92.onrender.com`

> **Nota:** Debido a las características del plan gratuito de Render, el primer request puede tomar unos segundos adicionales en responder mientras el contenedor sale del estado de inactividad (Cold Start).

## 📄 Decisiones Arquitectónicas (ADR)

En el directorio `docs/adr/` (o adjuntos en la entrega) se encuentra el registro de las decisiones arquitectónicas tomadas durante este sprint:

1. `0001-hexagonal-architecture.md`: Adopción de la Arquitectura Hexagonal.
2. `0002-persistence-adapters-jpa-entitymanager.md`: Uso de adaptadores de persistencia para aislar JPA.
3. `0003-docker-render-deployment.md`: Estrategia de contenedorización y despliegue.
