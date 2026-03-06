# This is NOT a script for execution, but for loading functions, so NOT need execution permission or shebang.
# NOTE that you NOT need to `cd ..' because the `$0' is NOT this file, but the script file which will source this file.

function prevent_sudo_or_root() {
  if [[ $(whoami) == "root" ]]; then
    echo "This script cannot run as root. Aborting..."
    return 1
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

function export_variables() {
  export SPRING_APPLICATION_NAME
  export SPRING_DATASOURCE_URL
  export SPRING_DATASOURCE_USERNAME
  export SPRING_DATASOURCE_PASSWORD
  export SPRING_JPA_HIBERNATE_DDL_AUTO
  export SPRING_JPA_SHOW_SQL
  export SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
  export SERVER_PORT
  export SERVER_ADDRESS
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
