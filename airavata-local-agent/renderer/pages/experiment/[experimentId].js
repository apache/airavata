import { useRouter } from 'next/router';
import { useEffect, useState } from "react";

function ExperimentPage() {
    const router = useRouter();
    const { experimentId } = router.query;
    const [experimentData, setExperimentData] = useState(null);

    console.log(router.query);

    // Rest of your code here

    useEffect(() => {
        // Fetch data here
        let accessToken = localStorage.getItem("accessToken");

        async function fetchExperimentData() {
            const resp = await fetch(`https://md.cybershuttle.org/api/experiments/AlphaFold2_on_Jun_16,_2024_4:28_PM_66f4610a-811d-49b7-9b74-1096ebccf096/?format=json`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });

            if (!resp.ok) {
                console.log("Error fetching experiment data");
                return;
            }

            const data = await resp.json();

            console.log(data);
            setExperimentData(data);
        }

        fetchExperimentData();

    }, []);

    if (!experimentId) {
        return <h1>Loading...</h1>;
    }

    return (
        <div>
            <h1>Experiment ID: {experimentId}</h1>
            {/* Rest of your JSX here */}
        </div>
    );
}

export default ExperimentPage;