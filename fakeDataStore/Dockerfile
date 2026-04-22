# Use a lightweight Node image
FROM node:20-alpine

# Set the working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

COPY db.json ./

EXPOSE 3000
CMD ["npx", "json-server", "--watch", "db.json", "--host", "0.0.0.0", "--port", "3000"]