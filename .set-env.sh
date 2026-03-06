#!/usr/bin/env bash
# -------------------------------------------------------------
# .set-env.sh
# Configures environment variables for Spring Boot
# -------------------------------------------------------------

# Ensure the script runs from its own directory
cd "$(dirname "$0")"

# Imports
source ./.lib/environment-variables.sh
source ./.lib/functions.sh

prevent_sudo_or_root # this script can't be executad as sudo or root
set -e               # stops the script if some command fails

# ---------------------------------------------
# Main script
# ---------------------------------------------
request_variables
export_variables
show_summary

echo "Environment variables are ready! You can now run your Spring Boot application:"
echo "  source .set-env.sh"
echo "  ./mvnw spring-boot:run"
