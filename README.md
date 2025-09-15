# MTLS Password Service

A Spring Boot microservice that retrieves user passwords by calling an external REST API with mutual TLS (mTLS) authentication.

## Features

- **mTLS Authentication**: Secure communication with external APIs using client certificates
- **HTTP Client**: Built with Spring RestTemplate and Apache HttpClient for reliable HTTP communication
- **OpenShift Ready**: Containerized and configured for OpenShift deployment
- **Health Checks**: Built-in health and status endpoints
- **Comprehensive Logging**: Detailed logging for debugging and monitoring
- **Configuration Management**: Environment-specific configurations

## Architecture

```
┌─────────────────┐    mTLS     ┌──────────────────┐
│   Client App    │ ──────────► │  External API    │
└─────────────────┘             └──────────────────┘
         │
         │ HTTP
         ▼
┌─────────────────┐
│ MTLS Password   │
│ Service         │
└─────────────────┘
```

## Prerequisites

- Java 17+
- Maven 3.6+
- Docker (for containerization)
- OpenShift CLI (for deployment)

## Quick Start

### 1. Build the Application

```bash
mvn clean package
```

### 2. Run Locally

```bash
# Development mode
java -jar target/mtls-password-service-1.0.0.jar --spring.profiles.active=dev

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. Test the Service

```bash
# Health check
curl http://localhost:8080/api/v1/status

# Get password
curl -X POST http://localhost:8080/api/v1/password \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "domain": "example.com"}'
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `EXTERNAL_API_BASE_URL` | External API base URL | `https://external-api.example.com` |
| `EXTERNAL_API_ENDPOINT` | Password endpoint path | `/api/v1/password` |
| `EXTERNAL_API_TIMEOUT` | Request timeout in seconds | `30` |
| `MTLS_CLIENT_KEYSTORE_PATH` | Client keystore file path | `/etc/ssl/certs/client-keystore.p12` |
| `MTLS_CLIENT_KEYSTORE_PASSWORD` | Client keystore password | - |
| `MTLS_CLIENT_KEYSTORE_TYPE` | Keystore type | `PKCS12` |
| `MTLS_TRUSTSTORE_PATH` | Truststore file path | `/etc/ssl/certs/truststore.jks` |
| `MTLS_TRUSTSTORE_PASSWORD` | Truststore password | - |
| `MTLS_TRUSTSTORE_TYPE` | Truststore type | `JKS` |
| `MTLS_VERIFY_HOSTNAME` | Verify hostname in certificate | `true` |

### Certificate Setup

1. **Client Keystore**: Contains the client certificate and private key for mTLS authentication
2. **Truststore**: Contains the CA certificates to verify the external API's certificate

#### Creating Certificates (Development)

```bash
# Create client keystore
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 \
  -keystore client-keystore.p12 -storetype PKCS12 \
  -dname "CN=client,OU=IT,O=Company,L=City,S=State,C=US" \
  -storepass changeit

# Create truststore (if needed)
keytool -import -alias server -file server-cert.pem \
  -keystore truststore.jks -storepass changeit
```

## API Endpoints

### POST /api/v1/password

Retrieve user password from external API.

**Request:**
```json
{
  "username": "john.doe",
  "domain": "example.com",
  "requestId": "optional-request-id"
}
```

**Response:**
```json
{
  "username": "john.doe",
  "password": "encrypted-password",
  "domain": "example.com",
  "requestId": "optional-request-id",
  "timestamp": "2024-01-15T10:30:00",
  "status": "SUCCESS",
  "message": "Password retrieved successfully"
}
```

### GET /api/v1/health

Check external API connectivity.

**Response:**
```json
"External API is healthy"
```

### GET /api/v1/status

Service status check.

**Response:**
```json
{
  "status": "UP",
  "service": "MTLS Password Service"
}
```

## OpenShift Deployment

### 1. Prepare Certificates

```bash
# Encode certificates to base64
base64 -i client-keystore.p12 > client-keystore.b64
base64 -i truststore.jks > truststore.b64
```

### 2. Update Secrets

Edit `openshift/deployment.yaml` and update the SSL certificates secret:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mtls-password-service-ssl-certs
type: Opaque
data:
  client-keystore.p12: "LS0tLS1CRUdJTi..."  # Your base64 encoded keystore
  truststore.jks: "LS0tLS1CRUdJTi..."       # Your base64 encoded truststore
```

### 3. Deploy to OpenShift

```bash
# Create project
oc new-project mtls-password-service

# Apply configurations
oc apply -f openshift/deployment.yaml
oc apply -f openshift/route.yaml
oc apply -f openshift/build-config.yaml

# Start build
oc start-build mtls-password-service --follow
```

### 4. Verify Deployment

```bash
# Check pods
oc get pods

# Check service
oc get svc

# Check route
oc get route

# Test the service
curl -X POST https://mtls-password-service.apps.example.com/api/v1/password \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser"}'
```

## Monitoring and Logging

### Health Checks

- **Liveness Probe**: `/api/v1/status`
- **Readiness Probe**: `/api/v1/status`
- **Health Check**: `/api/v1/health`

### Logging

Logs are written to `/var/log/mtls-password-service/application.log` in production.

Key log events:
- mTLS connection establishment
- External API calls
- Error conditions
- Performance metrics

### Metrics

The service exposes Prometheus metrics at `/actuator/prometheus`:
- HTTP request metrics
- JVM metrics
- Custom business metrics

## Security Considerations

1. **Certificate Management**: Store certificates securely using OpenShift secrets
2. **Network Policies**: Implement network policies to restrict traffic
3. **RBAC**: Use proper service accounts and role-based access control
4. **Secrets Rotation**: Implement certificate rotation procedures
5. **Audit Logging**: Enable audit logging for compliance

## Troubleshooting

### Common Issues

1. **Certificate Errors**
   - Verify certificate paths and permissions
   - Check certificate validity and expiration
   - Ensure correct keystore/truststore types

2. **Connection Timeouts**
   - Check network connectivity to external API
   - Verify firewall rules
   - Review timeout configurations

3. **Authentication Failures**
   - Verify client certificate is valid
   - Check certificate chain
   - Ensure proper mTLS configuration

### Debug Mode

Enable debug logging:

```yaml
logging:
  level:
    com.example.mtlspasswordservice: DEBUG
    org.apache.hc.client5.http.ssl: DEBUG
```

## Development

### Running Tests

```bash
mvn test
```

### Code Style

The project follows standard Java conventions and Spring Boot best practices.

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.