#!/bin/bash
#
# Stress test for /auth/login endpoint using Apache Benchmark (ab)
# Outputs P90, P95, P99 response times
#
# Usage: ./stress-test-login.sh [options]
# Options:
#   -u  Base URL (default: http://localhost:8181)
#   -n  Total number of requests (default: 500)
#   -c  Concurrent requests (default: 10)
#   -p  Post data file (auto-generated)
#   -H  Header: Content-Type
#
# Requirements: ab, awk, jq

set -euo pipefail

# ── Configuration ──────────────────────────────────────────────────────────
BASE_URL="${BASE_URL:-http://localhost:8181}"
TOTAL_REQUESTS="${TOTAL_REQUESTS:-500}"
CONCURRENCY="${CONCURRENCY:-10}"
LOGIN_ENDPOINT="${BASE_URL}/auth/login"
RESULTS_DIR="etc/stress-test/apache-benchmark/results"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
RESULTS_FILE="${RESULTS_DIR}/login-${TIMESTAMP}.txt"
PARSED_FILE="${RESULTS_DIR}/login-${TIMESTAMP}-parsed.txt"

# ── Create test user in Keycloak (or use existing one) ─────────────────────
# Adjust these credentials to match a user that exists in your Keycloak
TEST_USER="${TEST_USER:-johndoe}"
TEST_PASSWORD="${TEST_PASSWORD:-SecurePass123!}"

mkdir -p "${RESULTS_DIR}"

echo "============================================================"
echo "  Stress Test: POST ${LOGIN_ENDPOINT}"
echo "  Total Requests : ${TOTAL_REQUESTS}"
echo "  Concurrency    : ${CONCURRENCY}"
echo "  Timestamp      : ${TIMESTAMP}"
echo "============================================================"

# ── Prepare POST body ─────────────────────────────────────────────────────
POST_DATA_FILE="$(mktemp /tmp/login_body_XXXXXX.json)"

cat > "${POST_DATA_FILE}" <<EOF
{"username":"${TEST_USER}","password":"${TEST_PASSWORD}"}
EOF

echo "POST body: $(cat "${POST_DATA_FILE}")"

# ── Run Apache Benchmark ──────────────────────────────────────────────────
echo ""
echo "Running ab ... (${TOTAL_REQUESTS} requests, ${CONCURRENCY} concurrent)"
echo ""

ab -n "${TOTAL_REQUESTS}" \
   -c "${CONCURRENCY}" \
   -v 2 \
   -T "application/json" \
   -p "${POST_DATA_FILE}" \
   -e "${RESULTS_FILE}" \
   "${LOGIN_ENDPOINT}" 2>&1 | tee "${RESULTS_DIR}/login-${TIMESTAMP}-raw.txt"

echo ""
echo "Raw CSV results saved to: ${RESULTS_FILE}"

# ── Parse P90, P95, P99 from the CSV ──────────────────────────────────────
# ab -e outputs: elapsed_time_in_ms,bytes_sent,bytes_received,status_code,...
# We use column 1 (elapsed time in ms) and compute percentiles

if [[ -f "${RESULTS_FILE}" ]]; then
  # Skip header line, sort by elapsed time, extract percentiles
  awk -F',' 'NR > 1 { print $1 }' "${RESULTS_FILE}" | sort -n | awk -v total="${TOTAL_REQUESTS}" '
  BEGIN {
    count = 0
  }
  {
    values[NR] = $1
    count = NR
  }
  END {
    p90_idx = int(count * 0.90 + 0.5)
    p95_idx = int(count * 0.95 + 0.5)
    p99_idx = int(count * 0.99 + 0.5)

    if (p90_idx < 1) p90_idx = 1
    if (p95_idx < 1) p95_idx = 1
    if (p99_idx < 1) p99_idx = 1
    if (p90_idx > count) p90_idx = count
    if (p95_idx > count) p95_idx = count
    if (p99_idx > count) p99_idx = count

    printf "=============================\n"
    printf "  Response Time Percentiles\n"
    printf "=============================\n"
    printf "  P90: %8.2f ms\n", values[p90_idx]
    printf "  P95: %8.2f ms\n", values[p95_idx]
    printf "  P99: %8.2f ms\n", values[p99_idx]
    printf "  Min: %8.2f ms\n", values[1]
    printf "  Max: %8.2f ms\n", values[count]
    printf "=============================\n"
    printf "  Total successful: %d\n", count
    printf "=============================\n"
  }' | tee "${PARSED_FILE}"

  echo ""
  echo "Parsed results saved to: ${PARSED_FILE}"
else
  echo "ERROR: Results file not found. ab may have failed."
  exit 1
fi

# ── Cleanup ───────────────────────────────────────────────────────────────
rm -f "${POST_DATA_FILE}"

echo ""
echo "Done."
