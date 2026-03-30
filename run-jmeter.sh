#!/bin/bash
# ============================================================
# run-jmeter.sh — Lance les tests JMeter contre Spring Boot
# ============================================================

set -e

PLAN=${1:-"olympics_load_test"}
HOST=${2:-"localhost"}
PORT=${3:-"8080"}
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_DIR="./jmeter/results/${TIMESTAMP}"

echo ""
echo "===================================="
echo "  JMeter - Olympic Medal Tracker"
echo "===================================="
echo "  Plan    : $PLAN"
echo "  Cible   : http://$HOST:$PORT"
echo "  Rapport : $RESULTS_DIR/html-report"
echo "===================================="
echo ""

mkdir -p "$RESULTS_DIR"

# --- Mode Docker ---
if command -v docker &> /dev/null && docker image inspect justb4/jmeter:latest &> /dev/null; then
  echo "[Docker] Lancement du test..."
  docker run --rm \
    --network host \
    -v "$(pwd)/jmeter/plans:/jmeter/plans" \
    -v "$(pwd)/${RESULTS_DIR}:/jmeter/results" \
    justb4/jmeter:latest \
      -n \
      -t "/jmeter/plans/${PLAN}.jmx" \
      -l "/jmeter/results/results.jtl" \
      -e \
      -o "/jmeter/results/html-report" \
      -Jhost="$HOST" \
      -Jport="$PORT"

# --- Mode local (JMeter installé) ---
elif command -v jmeter &> /dev/null; then
  echo "[Local] Lancement du test..."
  jmeter -n \
    -t "./jmeter/plans/${PLAN}.jmx" \
    -l "${RESULTS_DIR}/results.jtl" \
    -e \
    -o "${RESULTS_DIR}/html-report" \
    -Jhost="$HOST" \
    -Jport="$PORT"

else
  echo "[ERREUR] JMeter non trouvé."
  echo "  Option 1 : docker pull justb4/jmeter:latest"
  echo "  Option 2 : installer JMeter localement (https://jmeter.apache.org/download_jmeter.cgi)"
  exit 1
fi

echo ""
echo "[OK] Test terminé !"
echo "     Rapport HTML : ${RESULTS_DIR}/html-report/index.html"
echo ""

# Ouvrir le rapport automatiquement si possible
if command -v xdg-open &> /dev/null; then
  xdg-open "${RESULTS_DIR}/html-report/index.html" &
elif command -v open &> /dev/null; then
  open "${RESULTS_DIR}/html-report/index.html" &
fi
