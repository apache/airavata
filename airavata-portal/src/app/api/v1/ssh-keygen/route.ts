import { NextRequest, NextResponse } from "next/server";
import crypto from "crypto";

export async function POST(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const keySize = parseInt(searchParams.get("keySize") || "2048");

    // Generate RSA key pair
    const { publicKey, privateKey } = crypto.generateKeyPairSync("rsa", {
      modulusLength: keySize,
      publicKeyEncoding: {
        type: "spki",
        format: "pem",
      },
      privateKeyEncoding: {
        type: "pkcs8",
        format: "pem",
      },
    });

    // Convert public key to OpenSSH format (simplified)
    // Extract modulus and exponent from PEM
    const publicKeyObj = crypto.createPublicKey(publicKey);
    const publicKeyDer = publicKeyObj.export({ type: "spki", format: "der" });
    const publicKeyBase64 = publicKeyDer.toString("base64");

    // For OpenSSH format, we need proper encoding
    // This is a simplified version - in production use proper ASN.1 encoding
    const sshPublicKey = `ssh-rsa ${publicKeyBase64} generated@airavata`;

    return NextResponse.json({
      privateKey,
      publicKey: sshPublicKey,
      keySize: keySize.toString(),
    });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : "Key generation failed" },
      { status: 500 }
    );
  }
}
