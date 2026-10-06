FROM node:20-alpine
WORKDIR /app
COPY package.json server.mjs ./
RUN npm install --omit=dev
ENV NODE_ENV=production
EXPOSE 8787
CMD ["node","server.mjs"]
