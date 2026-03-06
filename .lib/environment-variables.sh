# Variable definitions
# This is NOT a script for execution, but for loading functions, so NOT need execution permission or shebang.

# Routes
declare PROJECT_DIR=$(pwd)
declare ENV_FILE="$PROJECT_DIR/.env"

# Variables to be exported
declare SPRING_APPLICATION_NAME
declare SPRING_DATASOURCE_URL
declare SPRING_DATASOURCE_USERNAME
declare SPRING_DATASOURCE_PASSWORD
declare SPRING_JPA_HIBERNATE_DDL_AUTO
declare SPRING_JPA_SHOW_SQL
declare SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT
declare SERVER_PORT
declare SERVER_ADDRESS
