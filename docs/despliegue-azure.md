# Despliegue en Azure

Los cuatro sistemas viven en **una sola suscripción de Azure**, con los mismos recursos compartidos:

| Recurso | Para qué | Costo aproximado |
| --- | --- | --- |
| Grupo de recursos `rg-sgj` | Agrupa todo; borrarlo borra todo | Gratis |
| Azure Database for MySQL 8.4, Burstable B1ms, 32 GiB | Un servidor con una base y un usuario por sistema | ~12 USD/mes más almacenamiento |
| App Service Plan Linux B1 | Un plan donde corren las cuatro apps | ~13 USD/mes en total |
| Una Web App por sistema (Tomcat 10.1, Java 21) | `sgj-oficialia-…`, `sgj-peritos-…`, etc. | Incluida en el plan |

Los costos son aproximados; revisa el real en **Cost Management** del portal.

## 0. Antes de empezar

- Instala la Azure CLI (`brew install azure-cli` o `winget install Microsoft.AzureCLI`) y entra: `az login`.
- Elige un sufijo único para los nombres, por ejemplo tus iniciales: `SUFIJO=cac`.
- Región: `southcentralus`. Si tu suscripción de estudiante no la permite, el error dice cuáles sí.

```bash
SUFIJO=cac
RG=rg-sgj
REGION=southcentralus
MYSQL=sgj-mysql-$SUFIJO
PLAN=plan-sgj
az group create --name $RG --location $REGION
```

## 1. Servidor MySQL (una vez para todos)

```bash
az mysql flexible-server create --resource-group $RG --name $MYSQL --location $REGION \
  --tier Burstable --sku-name Standard_B1ms --storage-size 32 --version 8.4 \
  --admin-user sgjadmin --admin-password 'UNA-CONTRASEÑA-LARGA' --public-access 0.0.0.0 --yes

# Permite tu IP para crear las bases desde tu máquina
az mysql flexible-server firewall-rule create --resource-group $RG --name $MYSQL \
  --rule-name mi-ip --start-ip-address $(curl -s https://ifconfig.me) --end-ip-address $(curl -s https://ifconfig.me)
```

`--public-access 0.0.0.0` deja entrar solo a servicios de Azure (las Web Apps), no a todo internet.
Tarda unos 10 minutos.

## 2. Bases y usuarios (los mismos que en local)

Conéctate con el cliente de MySQL del contenedor de Docker:

```bash
docker run -it --rm mysql:8.4 mysql -h $MYSQL.mysql.database.azure.com -u sgjadmin -p --ssl-mode=REQUIRED
```

Y pega esto, cambiando las contraseñas:

```sql
CREATE DATABASE sgj_oficialia CHARACTER SET utf8mb4;
CREATE DATABASE sgj_peritos CHARACTER SET utf8mb4;
CREATE DATABASE sgj_audiencias CHARACTER SET utf8mb4;
CREATE DATABASE sgj_tocas CHARACTER SET utf8mb4;
CREATE USER 'oficialia'@'%' IDENTIFIED BY 'CAMBIA-oficialia';
CREATE USER 'peritos'@'%' IDENTIFIED BY 'CAMBIA-peritos';
CREATE USER 'audiencias'@'%' IDENTIFIED BY 'CAMBIA-audiencias';
CREATE USER 'tocas'@'%' IDENTIFIED BY 'CAMBIA-tocas';
GRANT ALL PRIVILEGES ON sgj_oficialia.* TO 'oficialia'@'%';
GRANT ALL PRIVILEGES ON sgj_peritos.* TO 'peritos'@'%';
GRANT ALL PRIVILEGES ON sgj_audiencias.* TO 'audiencias'@'%';
GRANT ALL PRIVILEGES ON sgj_tocas.* TO 'tocas'@'%';
```

Las tablas no se crean a mano: cada sistema aplica sus scripts de `db/migration` al arrancar.

## 3. Plan de App Service (una vez para todos)

```bash
az appservice plan create --resource-group $RG --name $PLAN --location $REGION --is-linux --sku B1
az webapp list-runtimes --os linux --output table | grep -i tomcat   # debe aparecer TOMCAT:10.1-java21
```

## 4. Web App de un sistema (ejemplo: oficialia)

```bash
APP=sgj-oficialia-$SUFIJO
az webapp create --resource-group $RG --plan $PLAN --name $APP --runtime "TOMCAT:10.1-java21"
az webapp config set --resource-group $RG --name $APP --always-on true

# Una clave por sistema que va a consumir la API
KEY_AUDIENCIAS=$(openssl rand -hex 24); KEY_TOCAS=$(openssl rand -hex 24); KEY_PERITOS=$(openssl rand -hex 24)

az webapp config appsettings set --resource-group $RG --name $APP --settings \
  OFICIALIA_DB_URL="jdbc:mysql://$MYSQL.mysql.database.azure.com:3306/sgj_oficialia?sslMode=REQUIRED" \
  OFICIALIA_DB_USER=oficialia \
  OFICIALIA_DB_PASSWORD='CAMBIA-oficialia' \
  OFICIALIA_API_KEYS="audiencias=$KEY_AUDIENCIAS;tocas=$KEY_TOCAS;peritos=$KEY_PERITOS" \
  TZ=America/Mexico_City

echo "audiencias: $KEY_AUDIENCIAS"; echo "tocas: $KEY_TOCAS"; echo "peritos: $KEY_PERITOS"
```

Guarda las claves: cada equipo recibe solo la suya, por mensaje privado, nunca en el repo.

## 5. Compilar y desplegar

```bash
./mvnw -pl oficialia -am verify
az webapp deploy --resource-group $RG --name $APP --src-path oficialia/target/oficialia.war --type war
```

La app queda en la raíz: `https://$APP.azurewebsites.net/`. El primer arranque tarda un par de minutos
porque crea las tablas y carga los datos de ejemplo.

## 6. Comprobar

```bash
curl https://$APP.azurewebsites.net/api/v1/health
# {"system":"oficialia","status":"UP","database":"UP"}

curl -H "X-Api-Key: $KEY_TOCAS" https://$APP.azurewebsites.net/api/v1/case-files
```

Si algo falla: `az webapp log tail --resource-group $RG --name $APP`.

## Los otros sistemas

Cada equipo repite el paso 4 con su prefijo (`PERITOS_`, `AUDIENCIAS_`, `TOCAS_`), su base y su
usuario, en el mismo plan. Para que despliegue solo, dale permiso únicamente sobre su Web App:

```bash
az role assignment create --assignee correo-del-lider@ejemplo.com --role Contributor \
  --scope $(az webapp show --resource-group $RG --name sgj-tocas-$SUFIJO --query id --output tsv)
```

## Al terminar el cuatrimestre

```bash
az group delete --name $RG --yes
```
