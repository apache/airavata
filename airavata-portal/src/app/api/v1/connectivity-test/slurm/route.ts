import { NextRequest, NextResponse } from "next/server";
import net from "net";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { host, sshPort = 22, slurmPort = 6817 } = body;

    if (!host) {
      return NextResponse.json(
        { success: false, message: "Host is required" },
        { status: 400 }
      );
    }

    // Test both SSH and SLURM ports
    const [sshAccessible, slurmAccessible] = await Promise.all([
      testPort(host, sshPort, 5000),
      testPort(host, slurmPort, 5000),
    ]);

    const success = sshAccessible && slurmAccessible;

    return NextResponse.json({
      success,
      sshPort,
      sshAccessible,
      slurmPort,
      slurmAccessible,
      message: success
        ? `SLURM cluster is accessible (SSH: ${sshPort}, SLURM: ${slurmPort})`
        : `Some ports are not accessible (SSH: ${sshAccessible ? "✓" : "✗"}, SLURM: ${slurmAccessible ? "✓" : "✗"})`,
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        message: error instanceof Error ? error.message : "Connection test failed",
      },
      { status: 500 }
    );
  }
}

function testPort(host: string, port: number, timeout: number): Promise<boolean> {
  return new Promise((resolve) => {
    const socket = new net.Socket();
    const timer = setTimeout(() => {
      socket.destroy();
      resolve(false);
    }, timeout);

    socket.once("connect", () => {
      clearTimeout(timer);
      socket.destroy();
      resolve(true);
    });

    socket.once("error", () => {
      clearTimeout(timer);
      resolve(false);
    });

    socket.connect(port, host);
  });
}
