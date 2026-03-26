/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Apache Airavata - Database Setup Script (manual / reference)
 *
 * For automated deployments (docker-compose, Ansible, K8s), use the canonical
 * init script at: conf/init-db/01-create-databases.sql
 *
 * This file is for manual one-off setup. Run as a MySQL/MariaDB admin before
 * first deployment. Replace credentials with values for your environment.
 *
 * Prerequisites: MySQL 5.7+ or MariaDB 10.2+
 *
 * Usage: mysql -u root -p < create-database.sql
 */

-- Airavata database
CREATE DATABASE IF NOT EXISTS airavata
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'airavata'@'%' IDENTIFIED BY 'CHANGE_ME_IN_PRODUCTION';
GRANT ALL PRIVILEGES ON airavata.* TO 'airavata'@'%';

FLUSH PRIVILEGES;
