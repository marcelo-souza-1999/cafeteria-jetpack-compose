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
if [ -f "$RULES_FILE" ]; then
  RULES_CONTENT=$(cat "$RULES_FILE")
else
  RULES_CONTENT="Voce e um code reviewer especializado em Android (Kotlin, Jetpack Compose, Clean Arch, Koin). Revise o diff enviado e forneca sugestoes e criticas construtivas de alta qualidade tecnica."
fi

jq -n \
  --arg diff "$(cat "$DIFF_FILE")" \
  --arg rules "$RULES_CONTENT" \
  '{
    contents: [{
      parts: [{
        text: ($rules + "\n\nAnalise o seguinte git diff de Pull Request e forneca a sua revisao tecnica contendo pontos criticos a serem melhorados de forma direta e incisiva:\n\n" + $diff)
      }]
    }]
  }' > gemini_request.json

echo "Disparando chamada para a API do Gemini (gemini-3.5-flash)..."
RESPONSE=$(curl -s -X POST \
  "https://generativelanguage.googleapis.com/v1/models/gemini-3.5-flash:generateContent?key=${GEMINI_API_KEY}" \
  -H "Content-Type: application/json" \
  -d @gemini_request.json)

if echo "$RESPONSE" | jq -e '.error' >/dev/null; then
  echo "Erro retornado pela API do Gemini:"
  echo "$RESPONSE"
  exit 1
fi

REVIEW_TEXT=$(echo "$RESPONSE" | jq -r '.candidates[0].content.parts[0].text')

if [ -z "$REVIEW_TEXT" ] || [ "$REVIEW_TEXT" = "null" ]; then
  echo "Erro: Nao foi possivel obter resposta de revisao valida da API do Gemini."
  echo "Resposta bruta da API:"
  echo "$RESPONSE"
  exit 1
fi

PR_NUMBER=$PR_NUMBER_ARG
if [ -z "$PR_NUMBER" ] || [ "$PR_NUMBER" = "null" ]; then
  PR_NUMBER=$(gh pr view --json number -q '.number' || echo "")
fi

if [ -n "$PR_NUMBER" ] && [ "$PR_NUMBER" != "null" ]; then
  echo "Postando revisao tecnica no Pull Request #${PR_NUMBER}..."
  gh pr comment "$PR_NUMBER" --body "$REVIEW_TEXT"
  echo "Revisao postada com sucesso."
else
  echo "Nao foi possivel identificar o numero do Pull Request. Exibindo revisao no log:"
  echo "======================================================================"
  echo "$REVIEW_TEXT"
  echo "======================================================================"
fi
