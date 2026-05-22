FROM node:16-bullseye

WORKDIR /app/yxtyg-web

EXPOSE 10011

CMD ["sh", "-c", "if [ -f package-lock.json ]; then npm ci; else npm install; fi && npm run serve -- --host 0.0.0.0 --port 10011"]
