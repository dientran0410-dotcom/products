# ================================
# Build stage
# Sử dụng Maven + JDK 21 để build ứng dụng
# ================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file pom.xml để tải dependency trước
COPY pom.xml .

# Copy toàn bộ source code
COPY src ./src

# Build ứng dụng Spring Boot và sinh file JAR
RUN mvn clean package -DskipTests

# ================================
# Run stage
# Sử dụng JRE nhẹ (Java 21) để chạy ứng dụng
# ================================
FROM eclipse-temurin:21-jre-alpine

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy file JAR từ stage build sang stage run
COPY --from=build /app/target/*.jar app.jar

# Render sẽ truyền PORT qua biến môi trường
ENV PORT=8080

# Khai báo cổng container
EXPOSE 8080

# Chạy ứng dụng và bind theo PORT Render cung cấp
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]