FROM node:20-alpine
WORKDIR /app
COPY package.json server.mjs prompt-engine.mjs prompt-engine.json ./
COPY public ./public
RUN npm install --omit=dev
ENV NODE_ENV=production
EXPOSE 8787
CMD ["node","server.mjs"]
