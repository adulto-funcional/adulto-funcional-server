# This is NOT a script for execution, but for loading functions, so NOT need execution permission or shebang.
# NOTE that you NOT need to `cd ..' because the `$0' is NOT this file, but the script file which will source this file.

function prevent_sudo_or_root() {
  if [[ $(whoami) == "root" ]]; then
    echo "This script cannot run as root. Aborting..."
    exit 1
  fi
}

# Prompt the user for environment variables
function request_variables() {
  echo "Please enter the configuration variables:"

  read -rp "Application name (spring.application.name): " SPRING_APPLICATION_NAME
  read -rp "Datasource URL (jdbc...): " SPRING_DATASOURCE_URL
  read -rp "Datasource Username: " SPRING_DATASOURCE_USERNAME
  read -rsp "Datasource Password: " SPRING_DATASOURCE_PASSWORD
  echo
  read -rp "JPA Hibernate ddl-auto (update/validate/create): " SPRING_JPA_HIBERNATE_DDL_AUTO
  read -rp "Show SQL (true/false): " SPRING_JPA_SHOW_SQL
  read -rp "Hibernate dialect: " SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
  read -rp "Server port: " SERVER_PORT
  read -rp "Server address (0.0.0.0): " SERVER_ADDRESS
}

function save_variables() {
  # Sobrescribe el archivo para no duplicar
  >"$ENV_FILE"

  # Recorre todas las variables que quieras exportar
  for var in SPRING_APPLICATION_NAME SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD \
    SPRING_JPA_HIBERNATE_DDL_AUTO SPRING_JPA_SHOW_SQL SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT \
    SERVER_PORT SERVER_ADDRESS; do
    # Escribe en formato clave=valor
    echo "$var=\"${!var}\"" >>"$ENV_FILE"
  done
}

function export_variables() {
  save_variables

  source "$ENV_FILE"
}

# Display a summary of the loaded variables (password hidden)
function show_summary() {
  echo
  echo "====================================="
  echo "Loaded environment variables:"
  echo "spring.application.name: $SPRING_APPLICATION_NAME"
  echo "spring.datasource.url: $SPRING_DATASOURCE_URL"
  echo "spring.datasource.username: $SPRING_DATASOURCE_USERNAME"
  echo "spring.datasource.password: ********"
  echo "spring.jpa.hibernate.ddl-auto: $SPRING_JPA_HIBERNATE_DDL_AUTO"
  echo "spring.jpa.show-sql: $SPRING_JPA_SHOW_SQL"
  echo "spring.jpa.properties.hibernate.dialect: $SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"
  echo "server.port: $SERVER_PORT"
  echo "server.address: $SERVER_ADDRESS"
  echo "====================================="
  echo
}
