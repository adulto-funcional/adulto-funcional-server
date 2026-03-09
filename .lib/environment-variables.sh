# -----------------------------------------------------------------------------
# Variables de configuración del entorno para la aplicación.
#
# Este archivo define las variables utilizadas durante el proceso de
# configuración del proyecto y la generación del archivo `.env`.
# NO está diseñado para ejecutarse directamente.
#
# En su lugar, debe cargarse desde otro script usando `source`:
#
#   source ./.lib/variables.sh
#
# Las variables declaradas aquí se utilizan para almacenar temporalmente
# los valores de configuración antes de exportarlos al entorno y guardarlos
# en el archivo `.env`.
# -----------------------------------------------------------------------------

# ------------------------------------------------------------------
# Directorio de proyecto y archivo .env
# ENV_FILE contendrá los valores configurados para poder cargarlos
# en cualquier sesión sin volver a ejecutar la configuración.
# ------------------------------------------------------------------
declare PROJECT_DIR=$(pwd)
declare ENV_FILE="$PROJECT_DIR/.env"
declare LOAD_ENV

# ------------------------------------------------------------------
# Variables de configuración de la aplicación
# Se usan temporalmente para capturar los valores del usuario
# antes de ser exportadas al entorno y guardadas en .env.
# ------------------------------------------------------------------
declare SPRING_APPLICATION_NAME
declare SPRING_DATASOURCE_URL
declare SPRING_DATASOURCE_USERNAME
declare SPRING_DATASOURCE_PASSWORD
declare SPRING_JPA_HIBERNATE_DDL_AUTO
declare SPRING_JPA_SHOW_SQL
declare SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
declare SERVER_PORT
declare SERVER_ADDRESS
