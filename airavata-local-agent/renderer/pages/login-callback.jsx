import { Link, Text } from "@chakra-ui/react";
import { useRouter } from "next/router";
import { useEffect } from "react";

const LoginCallback = () => {
  const router = useRouter();

  useEffect(() => {
    async function getToken() {
      const code = router.query.code;

      if (code) {
        // exchange code for token
        const resp = await fetch(`https://testdrive.cybershuttle.org/auth/get-token-from-code/?code=${code}`);
        const data = await resp.json();

        localStorage.setItem("accessToken", data.access_token);
        localStorage.setItem("refreshToken", data.refresh_token);

        window.ipc.send('write-file', '~/csagent/token/keys.json', JSON.stringify(data));

        router.push('/docker-home');
      }
    }

    getToken();
  });

  return (
    <>
      <Text>
        Logging in...you should be redirected shortly.
      </Text>

      <Text>
        If you are not redirected within a minute, please try <Link href="/login">logging in again</Link>.
      </Text>
    </>
  );
};

export default LoginCallback;