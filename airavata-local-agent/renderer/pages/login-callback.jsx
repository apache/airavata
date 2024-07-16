import { useRouter } from "next/router";
import { useEffect } from "react";

const LoginCallback = () => {
    const router = useRouter();

    useEffect(() => {
        const code = router.query.code;

        console.log(code);
    });

    console.log(router.query);


    return (
        <>
            <h1>Login Callback</h1>
        </>
    );
};

export default LoginCallback;