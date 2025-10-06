#!/bin/sh

# Запускаем Ollama сервер в фоне
ollama serve &

# Ждём, пока сервер поднимется
echo "⌛ Ожидание запуска Ollama..."
until wget -q --spider http://localhost:11434/api/tags >/dev/null; do
  sleep 1
done

echo "✅ Ollama запущен, загружаем модель 'mistral'..."
ollama pull mistral

# Оставляем сервер запущенным на переднем плане
echo "🚀 Ollama и модель 'mistral' готовы"
wait

