# IamFit - Backend

## Descripcion del proyecto

IamFit Backend es el conjunto de servicios encargados de manejar la logica principal de la aplicacion IamFit. Su objetivo es entregar APIs y procesos internos para autenticacion, usuarios, perfiles, alimentacion, rutinas, ejercicios y funcionalidades apoyadas por inteligencia artificial.

El backend esta organizado como una arquitectura de microservicios. Cada servicio se enfoca en una responsabilidad especifica y se comunica con otros componentes mediante HTTP, gRPC, mensajeria y bases de datos independientes.

Este proyecto permite que el frontend movil pueda registrar usuarios, iniciar sesion, consultar datos del perfil, gestionar rutinas, revisar informacion de alimentacion y utilizar capacidades de IA para recomendaciones o generacion de contenido relacionado con salud y entrenamiento.

## Tecnologias utilizadas

- **Java 21**: lenguaje principal utilizado en los microservicios.
- **Spring Boot**: framework base para crear APIs, servicios y configuraciones del backend.
- **Spring Security y OAuth2 Resource Server**: manejo de seguridad, validacion de tokens y proteccion de endpoints.
- **JWT**: mecanismo utilizado para autenticacion y autorizacion.
- **Spring Data JPA**: acceso y persistencia de datos relacionales.
- **PostgreSQL**: base de datos principal para los microservicios.
- **pgvector**: soporte para busquedas vectoriales en servicios relacionados con IA.
- **Redis**: cache compartida utilizada por servicios como autenticacion.
- **RabbitMQ**: mensajeria entre servicios y procesamiento asincronico.
- **gRPC y Protocol Buffers**: comunicacion interna entre microservicios.
- **Spring AI y Vertex AI Gemini**: integracion con inteligencia artificial generativa y embeddings.
- **Maven**: gestion de dependencias, compilacion y ejecucion de pruebas.
- **Docker y Docker Compose**: contenedorizacion y levantamiento local de servicios e infraestructura.
- **JUnit / Spring Boot Test**: pruebas automatizadas del backend.
- **Lombok**: reduccion de codigo repetitivo en entidades, DTOs y servicios.

## Estructura general del proyecto

- `autenticacion-seguridad/`: microservicio encargado de registro, inicio de sesion, sesiones, recuperacion de contrasena, JWT, roles y seguridad.
- `usuarios-perfiles/`: microservicio encargado de usuarios, perfiles y datos personales necesarios para el funcionamiento de la aplicacion.
- `ai-service/`: microservicio de inteligencia artificial generativa, embeddings y almacenamiento vectorial.
- `alimentacion-ia/`: microservicio encargado de alimentacion, planes de comida, registro de alimentos y apoyo de IA nutricional.
- `ejercicios-ia/`: microservicio encargado de ejercicios, rutinas y generacion o consulta de informacion asociada al entrenamiento.
- `docker-compose.yml`: configuracion para levantar los microservicios junto con PostgreSQL, pgvector, Redis y RabbitMQ.
- `logs.txt`: archivo de apoyo para registros o salida de ejecucion.

## Puertos principales

| Servicio                  | Puerto local | Descripcion                                |
| ------------------------- | ------------ | ------------------------------------------ |
| Autenticacion y seguridad | `8080`     | Login, registro, sesiones y seguridad      |
| Usuarios y perfiles       | `8081`     | Gestion de perfiles de usuario             |
| AI Service                | `8082`     | Funcionalidades de IA generativa           |
| Alimentacion IA           | `8083`     | Alimentacion, alimentos y planes de comida |
| Ejercicios IA             | `8084`     | Rutinas y ejercicios                       |
| Redis                     | `6380`     | Cache compartida                           |
| RabbitMQ                  | `5672`     | Mensajeria                                 |
| RabbitMQ Management       | `15672`    | Panel web de RabbitMQ                      |

## Estructura del equipo

| Integrante        | Rol              |
| ----------------- | ---------------- |
| Camilo Mena       | Jefe de Proyecto |
| Nicolas Rivera    | QA               |
| Lucciano Martinez | DevOps           |
| Benjamin Lizama   | Desarrollador    |

## Comandos principales

Levantar todos los servicios con Docker Compose:

```bash
docker compose up --build
```

Detener los servicios:

```bash
docker compose down
```

Ejecutar pruebas de un microservicio:

```bash
cd autenticacion-seguridad
./mvnw test
```

Compilar un microservicio:

```bash
cd autenticacion-seguridad
./mvnw clean package
```

Ejecutar un microservicio localmente:

```bash
cd autenticacion-seguridad
./mvnw spring-boot:run
```
