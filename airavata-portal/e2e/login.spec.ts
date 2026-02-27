import { test, expect } from "@playwright/test";

test.describe("Login flow", () => {
  test("unauthenticated user is redirected to login", async ({ page }) => {
    await page.goto("/default/dashboard");
    // Should redirect to login page (NextAuth or Keycloak)
    await expect(page).toHaveURL(/\/(login|realms\/default)/);
  });

  test("login page renders", async ({ page }) => {
    await page.goto("/login");
    await expect(page).toHaveTitle(/Airavata/i);
  });
});
