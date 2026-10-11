# Notifier — Microservicio de notificaciones

Microservicio que escucha eventos en **Kafka** y envía correos electrónicos mediante la **API HTTP de Brevo**. Existe porque Render bloquea los puertos SMTP, por lo que el envío directo por SMTP desde `users` no funciona en la nube.

No tiene base de datos: recibe un evento, arma el correo y lo entrega.

## Flujo

```
users / cuentas ──(evento JSON)──► Kafka ──► notifier ──(HTTP)──► Brevo ──► correo
```

1. `users` o `cuentas` publican un evento en Kafka.
2. `notifier` lo consume, lo valida y construye el correo según el tipo.
3. Llama a la API de Brevo para enviarlo.

## Arquitectura (hexagonal / DDD)

```
com.jcaa.notifier
├── domain/                     Reglas de negocio, sin dependencias de frameworks
│   ├── model/                  Notification (entidad)
│   ├── valueobject/            DestinationEmail, DestinationName, SenderEmail,
│   │                           NotificationSubject, NotificationContent, NotificationId
│   ├── enums/                  NotificationType
│   └── exception/              Excepciones de negocio (DestinationInvalidException, ...)
├── application/
│   ├── port/in/                ProcessNotificationEventUseCase   (puerto de entrada)
│   ├── port/out/               SendNotificationPort              (puerto de salida)
│   └── service/                NotificationEventService, EmailMessageComposer
└── infrastructure/
    ├── entrypoint/kafka/       NotificationEventListener         (adaptador de entrada)
    ├── adapter/brevo/          BrevoEmailSenderAdapter           (adaptador de salida)
    └── config/                 BrevoClientConfig
```

Las dependencias van hacia adentro: `infrastructure → application → domain`.

## Formato del evento

Mensaje JSON publicado en el tópico:

```json
{
  "eventId": "uuid",
  "type": "USER_CREATED",
  "occurredAt": "2026-10-11T16:30:00Z",
  "recipient": { "email": "ana@correo.com", "name": "Ana" },
  "data": { "role": "MEMBER", "accountId": "123" }
}
```

| `type` | Correo que se envía |
|---|---|
| `USER_CREATED` | Cuenta de usuario creada |
| `USER_UPDATED` | Datos de la cuenta actualizados |
| `ACCOUNT_CREATED` | Cuenta lista (incluye `data.accountId`) |

El evento **no debe incluir contraseñas**. Los mensajes inválidos (JSON roto o datos incorrectos) se descartan con un aviso; si Brevo falla, Kafka reintenta.

## Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `BREVO_API_KEY` | Sí | Clave API de Brevo |
| `SENDER_EMAIL` | Sí | Remitente (debe estar verificado en Brevo) |
| `SENDER_NAME` | No | Nombre del remitente (por defecto `Gestion de Usuarios`) |
| `BREVO_API_URL` | No | URL de la API (por defecto `https://api.brevo.com`) |
| `KAFKA_BOOTSTRAP_SERVERS` | No | Servidor Kafka (por defecto `localhost:9092`) |
| `KAFKA_GROUP_ID` | No | Grupo de consumo (por defecto `notifier-group`) |
| `KAFKA_TOPICS` | No | Tópicos separados por coma (por defecto `users.events,accounts.events`) |
| `PORT` | No | Puerto HTTP (por defecto `8080`) |

Los secretos nunca se guardan en el repositorio: se pasan como variables de entorno.

## Ejecutar

Requiere Java 17.

```bash
./mvnw clean test
./mvnw spring-boot:run
```

En Windows se usa `mvnw.cmd`. Con las variables en PowerShell:

```powershell
$env:BREVO_API_KEY = "..."
$env:SENDER_EMAIL  = "remitente-verificado@correo.com"
.\mvnw.cmd spring-boot:run
```

Comprobación de salud: `GET /actuator/health` → `{"status":"UP"}`.

### Con Docker

```bash
docker build -t notifier .
docker run --rm -p 8080:8080 -e BREVO_API_KEY=... -e SENDER_EMAIL=... notifier
```

## Pruebas

```bash
./mvnw test
```

Cubren el dominio, la composición de correos, el caso de uso (con un puerto de salida falso) y el listener de Kafka.

## Despliegue en Render

- Tipo: Web Service con Docker (usa el `Dockerfile` de la raíz).
- Health check path: `/actuator/health`.
- Variables de entorno: las de la tabla anterior, más las credenciales de Aiven.

**Pendiente:** configuración de conexión segura a Kafka en Aiven (SSL/SASL) y definición final de los tópicos con los demás servicios.