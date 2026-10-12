# Notifier — Microservicio de notificaciones

Microservicio que escucha eventos en **Kafka** y envía correos electrónicos mediante la **API HTTP de Brevo**. Existe porque Render bloquea los puertos SMTP, por lo que el envío directo por SMTP desde `users` no funciona en la nube.

No tiene base de datos: recibe un evento, arma el correo, lo entrega y publica el resultado en Kafka.

## Flujo

```
users / cuentas ──(evento)──► Kafka ──► notifier ──(HTTP)──► Brevo ──► correo
                                           │
users ◄── NOTIFICATION_SENT / FAILED ── Kafka ◄──┘
```

1. `users` o `cuentas` publican un evento en `users.events` o `accounts.events`.
2. `notifier` lo consume, lo valida y construye el correo según el tipo.
3. Llama a la API de Brevo para enviarlo.
4. Publica el resultado en `notifications.events` (`NOTIFICATION_SENT` o `NOTIFICATION_FAILED`) para que `users` pase el usuario de `notificando` a `notificado`.

## Arquitectura (hexagonal / DDD)

```
com.jcaa.notifier
├── domain/                     Reglas de negocio, sin dependencias de frameworks
│   ├── model/                  Notification (entidad)
│   ├── valueobject/            DestinationEmail, DestinationName, SenderEmail,
│   │                           NotificationSubject, NotificationContent, NotificationId
│   ├── enums/                  NotificationType
│   └── exception/              DomainException y una excepción por validación
│                               (DestinationInvalidException, SenderInvalidException, ...);
│                               NotificationDeliveryException cuando falla Brevo
├── application/
│   ├── port/in/                ProcessNotificationEventUseCase       (puerto de entrada)
│   ├── port/out/               SendNotificationPort                  (puerto de salida: correo)
│   │                           PublishNotificationResultPort         (puerto de salida: resultado)
│   └── service/                NotificationEventService, EmailMessageComposer
└── infrastructure/
    ├── entrypoint/kafka/       NotificationEventListener             (adaptador de entrada)
    ├── adapter/brevo/          BrevoEmailSenderAdapter               (adaptador de salida)
    ├── adapter/kafka/          KafkaNotificationResultPublisher      (adaptador de salida)
    └── config/                 BrevoClientConfig, KafkaErrorConfig, NotificationFailureRecoverer
```

Las dependencias van hacia adentro: `infrastructure → application → domain`.

## Evento de entrada

Mensaje JSON publicado en `users.events` o `accounts.events`:

```json
{
  "eventId": "uuid",
  "type": "USER_CREATED",
  "occurredAt": "2026-10-11T16:30:00Z",
  "recipient": { "email": "ana@correo.com", "name": "Ana" },
  "data": { "userId": "usr-1", "role": "MEMBER" }
}
```

| `type` | Correo que se envía | Campos de `data` que usa |
|---|---|---|
| `USER_CREATED` | Cuenta de usuario creada | `role` |
| `USER_UPDATED` | Datos de la cuenta actualizados | `role`, `status` |
| `ACCOUNT_CREATED` | Cuenta lista | `accountNumber` (se muestra enmascarado) |

Reglas:

- `data.userId` es obligatorio para que `users` reciba la confirmación. Sin él el correo se envía, pero no se publica el resultado.
- El evento **no debe incluir contraseñas** ni datos sensibles.
- Los mensajes inválidos (JSON roto o datos incorrectos) se descartan con un aviso en el log, porque reintentarlos no los arregla.

## Evento de resultado

El notificador publica en `notifications.events` (la clave del mensaje es el `userId`):

```json
{
  "eventId": "uuid nuevo",
  "type": "NOTIFICATION_SENT",
  "occurredAt": "2026-10-11T16:30:05Z",
  "data": {
    "userId": "usr-1",
    "sourceEventId": "uuid del evento original",
    "notificationType": "USER_CREATED"
  }
}
```

Si no se pudo entregar, el `type` es `NOTIFICATION_FAILED` y `data` incluye `reason`:

| `reason` | Causa |
|---|---|
| `BREVO_REJECTED` | Brevo respondió 4xx (api key inválida, remitente no verificado, etc.) |
| `BREVO_UNAVAILABLE` | Red caída, timeout o 5xx de Brevo |

## Resiliencia

Si Brevo falla, Kafka reintenta el mensaje: 1 intento inicial + 4 reintentos, con 2 s entre cada uno. Al agotarse se publica `NOTIFICATION_FAILED`, para que `users` no deje al usuario en `notificando`.

## Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `BREVO_API_KEY` | Sí | Clave API de Brevo |
| `SENDER_EMAIL` | Sí | Remitente (debe estar verificado en Brevo) |
| `SENDER_NAME` | No | Nombre del remitente (por defecto `Gestion de Usuarios`) |
| `BREVO_API_URL` | No | URL de la API (por defecto `https://api.brevo.com`) |
| `KAFKA_BOOTSTRAP_SERVERS` | No | Servidor Kafka (por defecto `localhost:9092`) |
| `KAFKA_GROUP_ID` | No | Grupo de consumo (por defecto `notifier-group`) |
| `KAFKA_TOPICS` | No | Tópicos de entrada separados por coma (por defecto `users.events,accounts.events`) |
| `KAFKA_RESULT_TOPIC` | No | Tópico de resultados (por defecto `notifications.events`) |
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

Cubren el dominio (`Notification`), la composición de correos, el caso de uso (con puertos de salida falsos), el listener de Kafka y el recoverer que publica `NOTIFICATION_FAILED`.

## Despliegue en Render

- Tipo: Web Service con Docker (usa el `Dockerfile` de la raíz).
- Health check path: `/actuator/health`.
- Variables de entorno: las de las tablas anteriores, incluidas las de Aiven

## Conexión a Kafka en Aiven (perfil `cloud`)

Por defecto el servicio usa `localhost:9092` sin seguridad (desarrollo local). Para conectarse a Aiven se activa el perfil `cloud` (`SPRING_PROFILES_ACTIVE=cloud`), que usa `SASL_SSL` con SCRAM-SHA-256 y el certificado CA de Aiven en formato PEM.

| Variable | Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `cloud` |
| `KAFKA_BOOTSTRAP_SERVERS` | `host:puerto` del servicio de Aiven (puerto SASL) |
| `KAFKA_USERNAME` / `KAFKA_PASSWORD` | Credenciales de Aiven |
| `KAFKA_CA_PATH` | Ruta del archivo PEM con el certificado CA |
| `KAFKA_SASL_MECHANISM` | Opcional. Por defecto `SCRAM-SHA-256` |
| `KAFKA_LOGIN_MODULE` | Opcional. Para `PLAIN`: `org.apache.kafka.common.security.plain.PlainLoginModule` |

Tópicos en Aiven: `users.events` y `accounts.events` (entrada) y `notifications.events` (resultado).

El certificado y las contraseñas nunca van en el repositorio (`*.pem` está en `.gitignore`). En Render el certificado se sube como Secret File (`/etc/secrets/aiven-ca.pem`).