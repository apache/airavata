import { getSession } from "@/lib/session";
import ApiConsole from "@/components/ApiConsole";

export default async function Home({
  searchParams,
}: {
  searchParams: { error?: string };
}) {
  const session = await getSession();

  if (!session) {
    return (
      <div className="page">
        <div className="centered">
          <h1>Airavata API Console</h1>
          <p>Sign in with CILogon to authenticate and start invoking Airavata REST APIs.</p>
          {searchParams.error && (
            <p className="error-text">Sign-in failed: {searchParams.error}</p>
          )}
          <a className="btn btn-primary" href="/login">
            Sign in with CILogon
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="header">
        <h1>Airavata API Console</h1>
        <div className="user-box">
          <span>{session.email ?? session.name ?? "Signed in"}</span>
          <form action="/logout" method="POST">
            <button className="btn" type="submit">
              Sign out
            </button>
          </form>
        </div>
      </div>
      <ApiConsole accessTokenExpires={session.accessTokenExpires} />
    </div>
  );
}
