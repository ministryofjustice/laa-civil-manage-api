# Use a lightweight Node image
FROM node:20-alpine

# Set the working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json db.json ./
RUN npm install

# Copy the db and give the 'node' user (UID 1000) ownership
COPY --chown=node:node db.json ./

# Switch to the non-root user before running the app
USER node

EXPOSE 3000
CMD ["npx", "json-server", "--watch", "db.json", "--host", "0.0.0.0", "--port", "3000"]