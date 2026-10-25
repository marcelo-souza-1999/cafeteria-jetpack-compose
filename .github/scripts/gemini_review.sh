#!/bin/bash
set -e

if [ -z "$GEMINI_API_KEY" ]; then
  echo "Erro: GEMINI_API_KEY nao configurado nos secrets do repositorio."
  exit 1
fi

if [ -z "$GITHUB_TOKEN" ]; then
  echo "Erro: GITHUB_TOKEN nao configurado no ambiente."
  exit 1
fi

DIFF_FILE=$1
PR_NUMBER_ARG=$2
if [ -z "$DIFF_FILE" ] || [ ! -f "$DIFF_FILE" ]; then
  echo "Erro: Arquivo diff nao especificado ou nao encontrado."
  exit 1
fi

if [ ! -s "$DIFF_FILE" ]; then
  echo "Nenhuma alteracao de diff detectada para revisar."
  exit 0
fi

RULES_FILE=".agents/code_review.md"
TEMP_RULES=$(mktemp)
if [ -f "$RULES_FILE" ]; then
  cp "$RULES_FILE" "$TEMP_RULES"
else
  cat <<'EOF' > "$TEMP_RULES"
Voce e um code reviewer especializado em Android (Kotlin, Jetpack Compose, Clean Arch, Koin). Revise o diff enviado e forneca sugestoes e criticas construtivas de alta qualidade tecnica.
EOF
fi

jq -n \
  --rawfile diff "$DIFF_FILE" \
  --rawfile rules "$TEMP_RULES" \
  '{
    contents: [{
      parts: [{
        text: ($rules + "\n\nAnalise o seguinte git diff de Pull Request e forneca a sua revisao tecnica contendo pontos criticos a serem melhorados de forma direta e incisiva:\n\n" + $diff)
      }]
    }]
  }' > gemini_request.json

rm -f "$TEMP_RULES"

echo "Disparando analise automatizada com a API do Gemini..."
MODELS=("gemini-2.5-flash" "gemini-1.5-flash" "gemini-2.0-flash" "gemini-3.5-flash")
REVIEW_TEXT=""

for MODEL in "${MODELS[@]}"; do
  echo "Tentando modelo $MODEL..."
  RESPONSE=$(curl -s -X POST \
    "https://generativelanguage.googleapis.com/v1/models/${MODEL}:generateContent?key=${GEMINI_API_KEY}" \
    -H "Content-Type: application/json" \
    -d @gemini_request.json)

  if echo "$RESPONSE" | jq -e '.error' >/dev/null 2>&1; then
    ERROR_CODE=$(echo "$RESPONSE" | jq -r '.error.code // empty' 2>/dev/null || echo "")
    ERROR_MSG=$(echo "$RESPONSE" | jq -r '.error.message // empty' 2>/dev/null || echo "")
    echo "Aviso: Modelo $MODEL indisponivel ($ERROR_CODE: $ERROR_MSG). Tentando fallback..."
    sleep 2
    continue
  fi

  CANDIDATE_TEXT=$(echo "$RESPONSE" | jq -r '.candidates[0].content.parts[0].text // empty' 2>/dev/null || echo "")
  if [ -n "$CANDIDATE_TEXT" ] && [ "$CANDIDATE_TEXT" != "null" ]; then
    REVIEW_TEXT="$CANDIDATE_TEXT"
    echo "Revisao tecnica obtida com sucesso via $MODEL."
    break
  fi
done

if [ -z "$REVIEW_TEXT" ] || [ "$REVIEW_TEXT" = "null" ]; then
  echo "⚠️ [Sentinel AI] Servico do Gemini indisponivel no momento (alta demanda temporaria/quota)."
  echo "A revisao automatica foi ignorada graciosamente para nao bloquear a esteira de entrega continua."
  exit 0
fi

PR_NUMBER=$PR_NUMBER_ARG
if [ -z "$PR_NUMBER" ] || [ "$PR_NUMBER" = "null" ]; then
  PR_NUMBER=$(gh pr view --json number -q '.number' || echo "")
fi

if [ -n "$PR_NUMBER" ] && [ "$PR_NUMBER" != "null" ]; then
  echo "Postando revisao tecnica no Pull Request #${PR_NUMBER}..."
  TEMP_COMMENT=$(mktemp)
  echo "$REVIEW_TEXT" > "$TEMP_COMMENT"
  gh pr comment "$PR_NUMBER" --body-file "$TEMP_COMMENT"
  rm -f "$TEMP_COMMENT"
  echo "Revisao postada com sucesso."
else
  echo "Nao foi possivel identificar o numero do Pull Request. Exibindo revisao no log:"
  echo "======================================================================"
  echo "$REVIEW_TEXT"
  echo "======================================================================"
fi
