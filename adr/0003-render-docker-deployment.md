ADR 03: Despliegue Automatizado en Render mediante Contenedor Docker
Estado: Aceptado

Prioridad: Baja / Operativa (Crítica para la entrega final)

Contexto:
Se necesitaba un entorno de despliegue en la nube gratuito o de bajo costo para probar la aplicación en un ambiente real mediante Postman, simulando la entrega de producción.

Decisión:
Utilizar la plataforma Render configurada mediante un archivo Dockerfile optimizado para compilar y ejecutar una aplicación empaquetada en Java 17 con restricciones de memoria específicas (-Xmx300m).

Consecuencias:

Positivas: Control total del entorno de ejecución (garantizando Java 17 y las dependencias de Gradle), despliegues automatizados conectados directamente con GitHub y visibilidad de logs en tiempo real.

Negativas: Los tiempos de arranque en el plan gratuito pueden ser elevados (por restricciones de CPU/RAM compartida), requiriendo ajustes de timeout y gestión del ciclo de vida del contenedor.