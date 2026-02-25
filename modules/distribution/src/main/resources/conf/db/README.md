# Database Setup

## Quick Start

1. **Create database and user** (run as MySQL/MariaDB admin):
   ```bash
   mysql -u root -p < create-database.sql
   ```
   Edit `create-database.sql` to set the desired password before running.

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
| `create-database.sql` | Manual setup: creates database and user (run once as admin) |
| `migration/airavata/V1__Baseline_schema.sql` | Flyway migration: creates all tables (run by Flyway) |

## Requirements

- MySQL 5.7+ or MariaDB 10.2+
- UTF-8 (utf8mb4) supported
