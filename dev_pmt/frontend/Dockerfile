# Angular CLI 22 exige Node.js 22.22.3 ou une version ultérieure compatible.
FROM node:22-alpine AS build

# Les dépendances sont installées depuis le lockfile pour un build reproductible.
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# Le code frontend est compilé en mode production.
COPY . .
RUN npm run build

# Nginx sert les fichiers statiques et relaie les appels API.
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Le conteneur expose le port HTTP standard de nginx.
EXPOSE 80
