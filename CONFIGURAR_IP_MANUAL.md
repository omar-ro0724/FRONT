# 🔧 Configurar IP del Servidor Manualmente

Si la búsqueda automática no encuentra el servidor, puedes configurar la IP manualmente.

## 📱 Opción 1: Usando código (Desarrollador)

Si eres desarrollador y tienes acceso al código, puedes configurar la IP manualmente en el código:

```kotlin
// En cualquier parte del código donde tengas acceso al contexto
import com.example.app.Interfaces.RetrofitClient.RetrofitClient
import com.example.app.Utils.NetworkConfigManager

// Configurar IP manualmente
NetworkConfigManager.setServerIpManually("192.168.100.9") // Reemplaza con la IP de tu servidor

// O usando RetrofitClient
RetrofitClient.setServerIpManually("192.168.100.9")
```

## 🔍 Encontrar la IP del Servidor

### En Windows (PowerShell):
```powershell
ipconfig | findstr /i "IPv4"
```

Busca la IP que corresponde a tu red WiFi (generalmente `192.168.x.x` o `10.x.x.x`).

### Ejemplo:
Si el servidor muestra:
```
IPv4 Address. . . . . . . . . . . : 192.168.100.9
```

Entonces usa `192.168.100.9` como IP del servidor.

## ✅ Verificar que Funciona

Después de configurar la IP manualmente, intenta hacer login nuevamente. La app debería conectarse al servidor.

## 🔄 Resetear Configuración

Si quieres que la app busque automáticamente de nuevo:

```kotlin
NetworkConfigManager.resetConfiguration()
```

## 📝 Notas

- La IP se guarda en SharedPreferences, por lo que persistirá entre sesiones
- Si cambias de red, es posible que necesites cambiar la IP
- La búsqueda automática seguirá funcionando si no hay IP guardada

