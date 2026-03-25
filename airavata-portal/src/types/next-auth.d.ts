import "next-auth";
import "next-auth/jwt";

declare module "next-auth" {
  interface Session {
    accessToken?: string;
    user: {
      email?: string;
      name?: string;
      gatewayId?: string;
      userName?: string;
    };
  }

  interface User {
    email?: string;
    name?: string;
    gatewayId?: string;
    userName?: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    accessToken?: string;
    refreshToken?: string;
    idToken?: string;
    expiresAt?: number;
    email?: string;
    name?: string;
    gatewayId?: string;
    userName?: string;
  }
}
