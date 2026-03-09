#!/usr/bin/env bash

# -------------------------------------------------------------
# .set-env.sh
# Configura las variables de entorno necesarias para la
# aplicación Spring Boot. Permite ingresarlas desde consola,
# guardarlas en .env y cargarlas automáticamente en el entorno.
# -------------------------------------------------------------

# Cambia al directorio del script para que las rutas relativas funcionen
SCRIPT_DIR="$(command cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
command cd "$SCRIPT_DIR"

# Importa funciones auxiliares y variables de entorno comunes
source ./.lib/environment-variables.sh
source ./.lib/functions.sh

prevent_sudo_or_root
execution_warning

# ------------------------------------------------------------------
# Evita que el script se ejecute directamente.
# Debe ser sourceado para que las variables se carguen en el entorno actual.
# Si se ejecuta con ./, muestra un mensaje y termina el script.
# ------------------------------------------------------------------
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  echo "Error, ejecute con: source .set-env.sh"
  exit 1
fi

set -e # detiene el script si algun comando falla

# ------------------------------------------------------------------
# Flujo principal del script
# Solicita, guarda y carga las variables, luego muestra un resumen
# ------------------------------------------------------------------
request_variables
export_variables
show_summary

echo "Las variables de entorno están listas. Puede ejecutar su aplicación Spring Boot:"
echo "Con este comando:  ./mvnw spring-boot:run"
echo
echo "NOTA"
echo
echo "SI USTED ENCUENTRA PROBLEMAS AL EJECUTAR LA APLICACIÓN SPRING BOOT"
echo "POR FAVOR REVISE QUE HAYA ESCRITO CORRECTAMENTE LAS VARIABLES"

loading
