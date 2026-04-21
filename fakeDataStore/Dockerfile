# Use a lightweight Node image
FROM node:20-alpine

# Set the working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy your mock database
COPY db.json ./

# Expose the port
EXPOSE 3000

# Start the server (bind to 0.0.0.0 so it accepts outside k8s traffic)
CMD ["npx", "json-server", "--watch", "db.json", "--host", "0.0.0.0", "--port", "3000"]