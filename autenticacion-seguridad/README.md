# Instalación de Redis
---
Usamos Docker para no instalar Redis directamente en la máquina.

**Requisito:** tener Docker Desktop instalado y abierto.
- Windows y Mac: https://docs.docker.com/desktop/
- Linux: puede que necesites `sudo usermod -aG docker $USER` y reiniciar sesión para no usar `sudo` en cada comando.
  *(Después de ejecutarlo, puede que necesites cerrar sesión y volver a entrar (o reiniciar) para que los cambios de grupo se apliquen.)*

--- 
Una vez con Docker abierto, en la terminal dentro de la carpeta `autenticacion-seguridad/` corre este comando:

`docker compose up -d`

Para verificar que quedó corriendo:

- `docker ps` y debe aparecer nomall-redis con estado **Up**

- `docker exec -it nomall-redis redis-cli ping` Debe responder: **PONG**


Otros comandos útiles:


`docker compose down` -> detener Redis
`docker logs nomall-redis` -> ver logs

La conexión ya está configurada en `application-dev.yaml`:

```
spring:
  data:
    redis:
      host: localhost
      port: 6379
```