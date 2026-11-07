ALTER TABLE "batch_application_deployments" ADD COLUMN "container_image" varchar(1024);
ALTER TABLE "batch_application_deployments" ADD COLUMN "container_bind" text;
