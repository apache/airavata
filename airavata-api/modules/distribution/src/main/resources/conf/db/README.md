# Database Setup

## Automated Deployments

For docker-compose, Ansible, and Kubernetes, the canonical init script is
`conf/init-db/01-create-databases.sql` at the repo root. It creates the
`airavata` database with dev credentials.

## Manual Setup

1. **Create databases and users** (run as MySQL/MariaDB admin):
   ```bash
   mysql -u root -p < create-database.sql
   ```
   Edit `create-database.sql` to set passwords before running.

2. **Configure connection** in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mariadb://localhost:3306/airavata
   spring.datasource.username=airavata
   spring.datasource.password=YOUR_PASSWORD
   ```

3. **Run migrations**: Flyway runs automatically on first startup, or run:
   ```bash
   airavata init
   ```

## Files

| File | Purpose |
|------|---------|
| `create-database.sql` | Manual/reference setup (creates airavata DB) |
| `migration/airavata/V1__Baseline_schema.sql` | Flyway baseline migration (all tables) |
| `conf/init-db/01-create-databases.sql` (repo root) | Canonical init for automated deployments |

## Requirements

- MySQL 5.7+ or MariaDB 10.2+
- UTF-8 (utf8mb4) supported
