import { NextRequest, NextResponse } from "next/server";
import net from "net";

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { host, port = 22 } = body;

    if (!host) {
      return NextResponse.json(
        { success: false, message: "Host is required" },
        { status: 400 }
      );
    }

    // SFTP uses SSH, so same test
    const isAccessible = await testPort(host, port, 5000);

    if (isAccessible) {
      return NextResponse.json({
        success: true,
        message: `SFTP port ${port} is accessible on ${host}`,
        details: "Port is open and accepting connections",
      });
    } else {
      return NextResponse.json({
        success: false,
        message: `Cannot connect to ${host}:${port}`,
        details: "Port is not accessible or connection timed out",
      });
    }
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
