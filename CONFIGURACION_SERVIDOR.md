# 🔧 Configuración del Servidor Backend para Conexión desde Android

## ⚠️ PROBLEMA COMÚN

Por defecto, Spring Boot escucha solo en `localhost` (127.0.0.1), lo que significa que **solo acepta conexiones desde la misma máquina**. Para que el dispositivo Android pueda conectarse, el servidor debe escuchar en **todas las interfaces de red**.

## ✅ SOLUCIÓN

### Opción 1: Configuración en `application.properties` (RECOMENDADO)

Agrega estas líneas en tu archivo `src/main/resources/application.properties`:

```properties
# Escuchar en todas las interfaces de red (0.0.0.0)
server.address=0.0.0.0
server.port=8080
```

### Opción 2: Configuración en `application.yml`

Si usas YAML, agrega en `src/main/resources/application.yml`:

```yaml
server:
  address: 0.0.0.0
  port: 8080
```

### Opción 3: Variables de Entorno

Puedes configurarlo también con variables de entorno:

```bash
# Windows (PowerShell)
$env:SERVER_ADDRESS="0.0.0.0"
$env:SERVER_PORT="8080"

# Linux/Mac
export SERVER_ADDRESS=0.0.0.0
export SERVER_PORT=8080
```

## 🔥 Configuración del Firewall de Windows

Después de configurar el servidor, asegúrate de que el firewall de Windows permita conexiones entrantes en el puerto 8080:

### Método 1: PowerShell (Administrador)

```powershell
New-NetFirewallRule -DisplayName "Spring Boot 8080" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow
```

### Método 2: Interfaz Gráfica

1. Abre "Firewall de Windows Defender"
2. Click en "Configuración avanzada"
3. Click en "Reglas de entrada" → "Nueva regla"
4. Selecciona "Puerto" → Siguiente
5. Selecciona "TCP" y escribe "8080" → Siguiente
6. Selecciona "Permitir la conexión" → Siguiente
7. Marca todos los perfiles → Siguiente
8. Nombre: "Spring Boot 8080" → Finalizar

## 📱 Verificar la IP del Servidor

Para encontrar la IP de tu servidor en la red:

### Windows (PowerShell)
```powershell
ipconfig | findstr /i "IPv4"
```

### Linux/Mac
```bash
ifconfig | grep "inet "
# o
ip addr show
```

Busca la IP que comienza con:
- `192.168.x.x`
- `10.x.x.x`
- `172.16.x.x` a `172.31.x.x`

## 🧪 Probar la Conexión

Después de configurar el servidor, prueba desde otro dispositivo en la misma red:

```bash
# Desde otro dispositivo en la misma red
curl http://TU_IP_SERVIDOR:8080/actuator/health

# Ejemplo:
curl http://10.120.137.9:8080/actuator/health
```

Si funciona, el servidor está correctamente configurado.

## 🚀 Reiniciar el Servidor

Después de hacer los cambios, **reinicia el servidor Spring Boot** para que los cambios surtan efecto.

## 📝 Notas Importantes

1. **Seguridad**: Escuchar en `0.0.0.0` hace que el servidor sea accesible desde cualquier dispositivo en la red. Esto es adecuado para desarrollo, pero en producción considera usar un firewall o proxy reverso.

2. **Red Local**: Asegúrate de que tanto el servidor como el dispositivo Android estén en la **misma red WiFi**.

3. **Puerto**: Si cambias el puerto, actualiza también la configuración en la app Android o usa el puerto por defecto (8080).

## ✅ Verificación Final

Una vez configurado, deberías ver en los logs del servidor:

```
Tomcat started on port 8080 (http) with context path '/'
```

Y la app Android debería poder conectarse automáticamente.

