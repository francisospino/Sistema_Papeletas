# Sistema de Papeletas de Salida de Bienes

Aplicación web (Spring Boot + MySQL) que digitaliza la "Papeleta de salida de bienes" en
papel: registrar la salida de un equipo, ver el listado en tiempo casi real desde varias
computadoras de la misma red, registrar el retorno de bienes prestados temporalmente, y
recibir un correo automático cada vez que se registra una papeleta nueva.

Pensado para correr en **una PC central de la red local** (por ejemplo, la de Soporte
Tecnológico) a la que las demás ~10 personas se conectan desde su navegador.

## Stack

- Java 21 (compilado con el JDK 25 instalado, `--release 21`)
- Spring Boot 3.3 (Web, Data JPA, Thymeleaf, Mail, Validation)
- MySQL 8
- Sin frontend build aparte: las vistas son plantillas Thymeleaf con JS simple (fetch +
  `setInterval` cada 5s) para que el listado se actualice solo, sin recargar la página.

## 1. Preparar la base de datos

Instala MySQL en la PC que hará de servidor (o usa uno que ya tengas, p.ej. XAMPP) y crea la
base de datos:

```sql
CREATE DATABASE papeletas_db CHARACTER SET utf8mb4;
```

## 2. Generar el App Password de Gmail (para las notificaciones por correo)

1. Entra a https://myaccount.google.com/apppasswords con la cuenta Gmail que enviará los
   correos (activa antes la verificación en 2 pasos si no la tienes).
2. Genera una contraseña de aplicación de 16 dígitos.
3. Guárdala, la usarás como `MAIL_PASSWORD` abajo.

## 3. Configurar variables de entorno

Antes de levantar la app, define (ajusta a tus valores reales):

```bash
export DB_USERNAME=root
export DB_PASSWORD=tu_password_mysql
export MAIL_USERNAME=tu_cuenta@gmail.com
export MAIL_PASSWORD=abcdabcdabcdabcd   # el App Password de 16 dígitos, sin espacios
export NOTIFICACION_DESTINATARIOS=soporte@tuempresa.com,otro@tuempresa.com
```

En Windows (PowerShell):

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="tu_password_mysql"
$env:MAIL_USERNAME="tu_cuenta@gmail.com"
$env:MAIL_PASSWORD="abcdabcdabcdabcd"
$env:NOTIFICACION_DESTINATARIOS="soporte@tuempresa.com,otro@tuempresa.com"
```

`NOTIFICACION_DESTINATARIOS` es opcional: solo sirve para precargar la lista la primera
vez. Una vez que la app arrancó, los correos se administran desde la pantalla
**Configuración** dentro del sistema, sin volver a tocar variables de entorno.

## 4. Ejecutar la aplicación

Este proyecto trae **Maven Wrapper**, así que no necesitas instalar Maven aparte:

```bash
./mvnw spring-boot:run
```

En Windows (cmd/PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

También puedes abrir la carpeta `sistema-papeletas/` directamente en IntelliJ IDEA (detecta
el `pom.xml` y usa su Maven embebido) y correr `PapeletasApplication` con Run ▶.

La primera vez, Hibernate crea las tablas solo (`ddl-auto=update`), no hace falta correr
ningún script SQL de estructura.

## 5. Acceder desde las demás computadoras de la red

1. En la PC servidor, averigua su IP local:
   - Windows: `ipconfig` (busca "Dirección IPv4", algo como `192.168.1.50`)
2. Desde cualquier otra PC de la misma red WiFi/LAN, abre en el navegador:

   ```
   http://192.168.1.50:8080
   ```

   (cambia la IP por la real de tu servidor).
3. Si Windows Defender Firewall bloquea la conexión, permite el puerto 8080 para redes
   privadas cuando aparezca el aviso al iniciar la app (o agrégalo manualmente en
   "Firewall de Windows Defender" → "Configuración avanzada" → regla de entrada para el
   puerto TCP 8080).

## 6. Que quede disponible 24/7 (arranque automático)

Si cierras la ventana de PowerShell, apagas la PC, o hay un corte de luz, el sistema se cae
para todos hasta que alguien vuelva a correr el comando a mano. Para que no dependa de eso,
en la PC servidor hay que dejar **dos cosas** configuradas para que arranquen solas con
Windows: MySQL y la app.

**A. MySQL como servicio de Windows (arranca solo, sin abrir el panel de XAMPP)**

En el Panel de Control de XAMPP, junto a los módulos Apache/MySQL hay una columna angosta a
la izquierda con casillas — marca la de **MySQL** ("Service"). Te va a pedir permisos de
administrador; acéptalos. Desde ese momento, MySQL arranca solo cada vez que prende Windows,
sin necesidad de abrir XAMPP.

**B. La app como servicio de Windows (arranca sola, sin abrir PowerShell)**

La forma más simple es con [NSSM](https://nssm.cc/download) (una herramienta gratuita que
convierte cualquier comando en un servicio de Windows):

1. Descarga NSSM y descomprime `nssm.exe` en, por ejemplo, `C:\nssm\`.
2. Abre PowerShell **como administrador** y corre:
   ```powershell
   C:\nssm\nssm.exe install PapeletasApp
   ```
3. Se abre una ventana:
   - **Path**: la ruta a `java.exe` (verifícala con `(Get-Command java).Source`)
   - **Arguments**: `-jar "C:\ruta\completa\sistema-papeletas-1.0.0.jar"`
   - **Startup directory**: la carpeta donde está el `.jar`
   - En la pestaña **Environment**, agrega las variables (una por línea):
     ```
     DB_USERNAME=root
     DB_PASSWORD=tu_password_mysql
     MAIL_USERNAME=tu_cuenta@gmail.com
     MAIL_PASSWORD=tu_app_password
     NOTIFICACION_DESTINATARIOS=correo1@tuempresa.com
     ```
4. Clic en **Install service**.
5. Arráncalo con:
   ```powershell
   nssm start PapeletasApp
   ```

Con esto, la app arranca sola al prender la PC, y si llega a fallar, NSSM la reinicia
automáticamente. Ya no hace falta dejar ninguna ventana abierta.

**C. Que la PC nunca se apague ni se duerma sola**

En esa PC: *Configuración → Sistema → Energía y batería* → desactiva la suspensión
automática ("Nunca" en pantalla y equipo). Así, aunque nadie la toque, sigue corriendo.

Con A + B + C, esa PC funciona como un servidor real: si hay un corte de luz y vuelve, todo
arranca solo sin que nadie tenga que hacer nada.

## Funcionalidad

- **Registrar papeleta** (`/papeletas/nueva`): formulario con los mismos campos de la
  papeleta física (tipo de salida, área que autoriza, destino, responsable, traslado
  temporal/definitivo, y la tabla de bienes con cantidad/descripción/marca/modelo/
  serie/estado). El número de papeleta **se asigna automáticamente** (correlativo de 6
  dígitos, empezando en `000001`), no se ingresa a mano. Al guardar, envía el correo de
  notificación automáticamente.
- **Listado** (`/papeletas`): tabla que se refresca sola cada 5 segundos vía AJAX (todas
  las personas conectadas ven las papeletas nuevas sin recargar), con filtros por número,
  responsable, destino y estado. La columna "Bienes" muestra el detalle completo de cada
  bien entregado (cantidad, descripción, marca, modelo y serie).
- **Registrar retorno**: botón en el listado para las papeletas de traslado temporal que
  siguen activas; marca la fecha de retorno y cambia el estado a "RETORNADA".
- **Exportar a Excel**: botón en el listado que descarga un `.xlsx` con las papeletas
  visibles (respeta los filtros aplicados).
- **Imprimir**: botón que abre el diálogo de impresión del navegador con una vista
  simplificada (sin menú, filtros ni botones de acción).
- **Reportes** (`/papeletas/reportes`): bienes actualmente fuera (temporales sin retornar,
  con días transcurridos), conteo de papeletas por destino y por área que autoriza.
- **Configuración** (`/configuracion`): pantalla para agregar o quitar los correos que
  reciben la notificación automática, sin tener que tocar variables de entorno ni
  reiniciar el servidor. Si ya tenías `NOTIFICACION_DESTINATARIOS` configurado, esos
  correos se importan automáticamente la primera vez que arranca con esta versión.

## Notas de diseño

- El "tiempo real" se logra con *polling* (la página pide la lista nueva cada 5s), no con
  WebSockets — es más que suficiente para el volumen de una papeleta cada tanto, y evita la
  complejidad de mantener conexiones persistentes.
- El envío de correo ocurre en el servidor al guardar (`PapeletaService.registrar`), así que
  solo se necesita configurar las credenciales SMTP una vez en la PC servidor, no en cada
  cliente.
- Si el correo falla (credenciales mal puestas, sin internet, etc.) el registro de la
  papeleta **no se revierte** — solo queda un log de error — para que un problema de correo
  nunca bloquee el registro del bien.

## Licencia

Distribuido bajo la licencia MIT. Ver [LICENSE](LICENSE) para el texto completo.
