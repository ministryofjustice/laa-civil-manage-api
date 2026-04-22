# Use the latest lightweight Node image
FROM node:alpine

# Set the working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

COPY db.json ./

RUN chown -R node:node /app

USER node

EXPOSE 3000
CMD ["npx", "json-server", "--watch", "db.json", "--host", "0.0.0.0", "--port", "3000"]