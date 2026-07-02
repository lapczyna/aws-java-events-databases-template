# Ad Platform — Cloud-Native Spring Boot Starter Kit

A production-grade **Java 21 / Spring Boot 3.x** enterprise starter template demonstrating modern cloud-native development practices on AWS. Built around a **Media & Advertisement** business domain, it serves as a ready-to-extend foundation for real-world applications.

---

## Architecture Overview

```
                        ┌──────────────────────────────────────┐
                        │              Ad Platform              │
                        │                                        │
  HTTP Client ──────▶  │  CampaignController  │  AdImpressionController │
                        │        │                      │         │
                        │   CampaignService        AdImpressionService   │
                        │        │                      │         │
                        │  CampaignRepository    DynamoDbAdImpressionRepo│
                        │        │                      │         │
                        │    MySQL (RDS)           DynamoDB       │
                        │                                        │
                        │  SqsProducerService ──▶ SQS Queue     │
                        │  SqsConsumerService ◀── SQS Queue     │
                        │  KafkaProducerService ──▶ Kafka Topic  │
                        │  KafkaConsumerService ◀── Kafka Topic  │
                        └──────────────────────────────────────┘
```

### Package Structure

```
src/main/java/com/enterprise/adplatform/
├── AdPlatformApplication.java
├── config/          # Spring & AWS client configuration
├── controller/      # REST controllers (HTTP layer)
├── service/         # Business logic interfaces + implementations
├── repository/      # Spring Data JPA repositories (MySQL)
├── persistence/     # DynamoDB repository wrappers
├── infrastructure/  # DynamoDB item models
├── entity/          # JPA entities
├── dto/             # Request/Response data transfer objects
├── mapper/          # MapStruct mapper interfaces
├── exception/       # Custom exceptions + global handler
├── messaging/
│   ├── sqs/         # SQS producer, consumer, event types
│   └── kafka/       # Kafka producer, consumer, event types
└── security/        # Security placeholder (JWT/OAuth2 ready)
```

---

## Technology Choices

| Technology | Version | Why |
|-----------|---------|-----|
| Java | 21 | LTS, virtual threads, pattern matching |
| Spring Boot | 3.3.x | Industry standard, excellent AWS integration |
| Spring Data JPA + Hibernate | Latest | Relational persistence for Campaign entities |
| Flyway | 10.x | Plain SQL migrations, simpler than Liquibase, first-class Spring Boot support |
| AWS SDK v2 | 2.27.x | Modern async-capable SDK, DynamoDB Enhanced Client |
| Apache Kafka | 3.7.x (via Confluent) | High-throughput event streaming for campaign events |
| AWS SQS | — | Reliable message queuing for ad events |
| MapStruct | 1.6.x | Compile-time type-safe DTO mapping, zero reflection overhead |
| Testcontainers | 1.20.x | Real infrastructure in tests, no mocking of external systems |
| Cucumber | 7.20.x | Business-readable BDD tests |
| LocalStack | 3.8 | Local AWS emulation (SQS + DynamoDB) |

### Why Flyway over Liquibase?

- **Plain SQL** migrations — no proprietary XML/YAML DSL to learn
- **Simpler configuration** — Spring Boot autoconfigures Flyway with zero extra beans
- **Excellent MySQL support** — native driver awareness for checksums
- **Smaller footprint** — fewer transitive dependencies
- **Industry adoption** — more widely used in Java/Spring ecosystems

---

## Prerequisites

- Java 21+ (`JAVA_HOME` set)
- Maven 3.9+
- Docker 24+ and Docker Compose v2
- AWS CLI v2 (for LocalStack interaction)
- `kubectl` + `helm` (for Kubernetes deployment)
- Terraform 1.9+ (for AWS provisioning)

---

## Local Setup (without Docker)

```bash
# 1. Clone the repo
git clone https://github.com/YOUR_ORG/aws-java-events-databases-template.git
cd aws-java-events-databases-template

# 2. Start dependencies only
docker compose up mysql kafka zookeeper localstack -d

# 3. Wait for LocalStack to be healthy, then initialize resources
./scripts/localstack-init.sh

# 4. Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The app starts on `http://localhost:8080`.

---

## Docker Setup (full stack)

```bash
# Build and start all services
docker compose up --build -d

# Tail app logs
docker compose logs -f app

# Stop everything
docker compose down -v
```

Services available:
| Service | URL |
|---------|-----|
| Application | http://localhost:8080 |
| Kafka UI | http://localhost:8090 |
| MySQL | localhost:3306 |
| LocalStack | http://localhost:4566 |

---

## LocalStack Setup

LocalStack runs SQS and DynamoDB locally. The init script (`scripts/localstack-init.sh`) creates:
- SQS queue: `ad-events-queue`
- DynamoDB table: `ad-impression-events`

```bash
# Verify SQS queue exists
aws --endpoint-url=http://localhost:4566 sqs list-queues

# Verify DynamoDB table exists
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

---

## Running Tests

```bash
# Unit tests only (fast, no Docker required)
mvn test

# Integration tests (requires Docker for Testcontainers)
mvn verify

# All tests including integration
mvn failsafe:integration-test failsafe:verify
```

## Running BDD Tests (Cucumber)

```bash
# Run Cucumber scenarios
mvn failsafe:integration-test failsafe:verify -pl . -Dgroups=cucumber

# Reports generated at:
# target/cucumber-reports/report.html
```

Feature file: `src/test/resources/features/campaign_management.feature`

---

## REST API — Campaign (MySQL)

### Create Campaign

```bash
curl -X POST http://localhost:8080/api/v1/campaigns \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": 1,
    "campaignName": "Summer Sale 2025",
    "campaignType": "DISPLAY",
    "budget": 50000.00,
    "startDate": "2025-06-01",
    "endDate": "2025-08-31",
    "status": "DRAFT"
  }'
```

### Get All Campaigns

```bash
curl http://localhost:8080/api/v1/campaigns
```

### Get Campaign by ID

```bash
curl http://localhost:8080/api/v1/campaigns/1
```

### Update Campaign

```bash
curl -X PUT http://localhost:8080/api/v1/campaigns/1 \
  -H "Content-Type: application/json" \
  -d '{
    "advertiserId": 1,
    "campaignName": "Summer Sale 2025 Updated",
    "campaignType": "DISPLAY",
    "budget": 75000.00,
    "startDate": "2025-06-01",
    "endDate": "2025-09-30",
    "status": "ACTIVE"
  }'
```

### Delete Campaign

```bash
curl -X DELETE http://localhost:8080/api/v1/campaigns/1
```

---

## REST API — Ad Impression Events (DynamoDB)

### Record Impression

```bash
curl -X POST http://localhost:8080/api/v1/impressions \
  -H "Content-Type: application/json" \
  -d '{
    "campaignId": "campaign-123",
    "placementId": "placement-456",
    "timestamp": "2025-06-01T12:00:00Z",
    "deviceType": "MOBILE",
    "country": "US",
    "eventType": "VIEW",
    "cost": 0.0025
  }'
```

### Get All Impressions

```bash
curl http://localhost:8080/api/v1/impressions
```

### Get Impression by ID

```bash
curl http://localhost:8080/api/v1/impressions/{impressionId}
```

---

## Health & Observability

```bash
# Overall health
curl http://localhost:8080/actuator/health

# Liveness (is app alive?)
curl http://localhost:8080/actuator/health/liveness

# Readiness (ready to serve traffic?)
curl http://localhost:8080/actuator/health/readiness

# Metrics
curl http://localhost:8080/actuator/metrics
```

---

## Terraform Deployment

### Prerequisites

- AWS CLI configured with appropriate credentials
- S3 bucket for Terraform state (update `versions.tf` backend config)

```bash
cd terraform

# Initialize
terraform init

# Preview changes
terraform plan \
  -var="rds_username=adplatform" \
  -var="rds_password=CHANGE_ME"

# Apply (after careful review)
terraform apply \
  -var="rds_username=adplatform" \
  -var="rds_password=CHANGE_ME"
```

### Destroy All Resources

```bash
# Remove application resources first
terraform destroy \
  -var="rds_username=adplatform" \
  -var="rds_password=CHANGE_ME"
```

> **Warning:** `deletion_protection = true` on RDS. Disable it first if you want Terraform to delete the instance.

---

## EKS Deployment

```bash
# Configure kubectl
aws eks update-kubeconfig --name ad-platform-prod-cluster --region us-east-1

# Deploy namespace first
kubectl apply -f k8s/namespace.yml

# Deploy service account (update IRSA role ARN from Terraform outputs)
kubectl apply -f k8s/serviceaccount.yml

# Deploy ConfigMap and Secret template
kubectl apply -f k8s/configmap.yml
# Edit k8s/secret.yml with real values, then:
kubectl apply -f k8s/secret.yml

# Deploy application
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/hpa.yml

# Deploy ingress (requires AWS Load Balancer Controller)
kubectl apply -f k8s/ingress.yml

# Check status
kubectl get pods -n ad-platform
kubectl get svc -n ad-platform
```

---

## Cost Considerations

### Estimated Monthly Costs (us-east-1)

| Resource | Size | Estimated Cost |
|----------|------|---------------|
| EKS Cluster | 1 cluster | ~$73/mo |
| EC2 t4g.medium nodes (2x) | 2 vCPU, 4 GB | ~$48/mo |
| RDS MySQL db.t4g.micro | 1 vCPU, 1 GB | ~$12/mo |
| DynamoDB (PAY_PER_REQUEST) | Traffic-based | ~$0–5/mo light use |
| SQS | Per-message | ~$0–1/mo light use |
| NAT Gateway | Single | ~$35/mo + data |
| Data transfer | Variable | ~$5–20/mo |
| **Total estimate** | | **~$175–195/mo** |

### Cost Optimisation Applied

- **ARM instances (t4g)** — ~20% cheaper than x86 equivalents
- **PAY_PER_REQUEST DynamoDB** — no idle provisioned capacity cost
- **Single NAT Gateway** — avoids 2× cost of per-AZ NAT setup
- **db.t4g.micro RDS** — minimum viable for development/light production
- **EKS managed nodes** — no EC2 management overhead, right-sized at start

### Tear Down

```bash
# Delete K8s resources first (detaches load balancers)
kubectl delete namespace ad-platform

# Then destroy Terraform infrastructure
cd terraform
terraform destroy -var="rds_username=x" -var="rds_password=x"
```

---

## Security

The `SecurityConfig` class is intentionally permissive for starter kit use. It is structured for easy integration with:

- **AWS Cognito** as OAuth2 identity provider
- **Spring Security OAuth2 Resource Server** with JWT validation
- **RBAC** per endpoint

To add Cognito authentication, replace the `anyRequest().permitAll()` block with:

```java
.oauth2ResourceServer(oauth2 -> oauth2
    .jwt(jwt -> jwt.jwkSetUri(cognitoJwksUri)))
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/**").authenticated())
```

---

## Troubleshooting

### Application fails to start — DB connection refused
- Ensure MySQL is running: `docker compose ps mysql`
- Check credentials in `application-local.yml`

### LocalStack resources not found
- Run the init script: `./scripts/localstack-init.sh`
- Check LocalStack health: `curl http://localhost:4566/_localstack/health`

### Kafka consumer not receiving messages
- Verify Kafka is healthy: `docker compose ps kafka`
- Check topic exists in Kafka UI at http://localhost:8090

### Testcontainers failing in CI
- Ensure Docker socket is accessible in the CI runner
- GitHub Actions runners have Docker pre-installed

### EKS pods in CrashLoopBackOff
- Check logs: `kubectl logs -n ad-platform deploy/ad-platform`
- Verify secrets are populated: `kubectl get secret ad-platform-secrets -n ad-platform`
- Verify IRSA role ARN matches service account annotation

---

## CI/CD Workflows

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `build.yml` | push/PR | Build, unit tests, integration tests, Cucumber |
| `container.yml` | push to main / tags | Docker build, Trivy scan, push to GHCR |
| `terraform.yml` | changes to `terraform/` | fmt, validate, plan (no auto-apply) |
| `k8s-validate.yml` | changes to `k8s/` | kubeval + kubeconform manifest validation |

### Branch Protection

`main` is a protected branch: changes must go through a pull request, and `Build & Unit Tests`, `Integration & Cucumber BDD Tests`, and `Build Docker Image` must pass before merging. Force pushes and branch deletion are disabled. Workflow runs on pull requests from external contributors require maintainer approval before they execute.
