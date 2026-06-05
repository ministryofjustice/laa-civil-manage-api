# Specify java runtime base image
FROM amazoncorretto:26-alpine@sha256:5f73844deb7a511f1f3f257525968a5175cd669b9d1abb039db21682c97cff8d

# Set up working directory in the container
RUN mkdir -p /opt/laa-civil-manage-api/
WORKDIR /opt/laa-civil-manage-api/

# Copy the JAR file into the container
COPY build/libs/laa-civil-manage-api-0.0.1-SNAPSHOT.jar app.jar

# Create a group and non-root user
RUN addgroup -S appgroup && adduser -u 1001 -S appuser -G appgroup

# Set the default user
USER 1001

# Expose the port that the application will run on
EXPOSE 8080

# Run the JAR file
CMD ["java", "-jar", "app.jar"]