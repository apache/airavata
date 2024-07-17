import { useRouter } from "next/router";
import { useEffect } from "react";

const LoginCallback = () => {
  const router = useRouter();

  useEffect(() => {
    async function getToken() {
      const code = router.query.code;

      console.log("Code", code);

      if (code) {
        // exchange code for token
        const resp = await fetch(`https://md.cybershuttle.org/auth/get-token-from-code/?code=${code}`);
        const data = await resp.json();

        console.log("Token data", data);

        localStorage.setItem("accessToken", data.access_token);
        localStorage.setItem("refreshToken", data.refresh_token);

        window.ipc.send('write-file', '~/.csagent/token/keys.json', JSON.stringify(data));

        // redirect to docker-home
        // router.push('/docker-home');
      }
    }


    getToken();
  });

  return (
    <>
      <h1>Login Callback</h1>
    </>
  );
};

export default LoginCallback;