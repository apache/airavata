/*****************************************************************
*
*  Licensed to the Apache Software Foundation (ASF) under one  
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information       
*  regarding copyright ownership.  The ASF licenses this file  
*  to you under the Apache License, Version 2.0 (the           
*  "License"); you may not use this file except in compliance  
*  with the License.  You may obtain a copy of the License at  
*                                                              
*    http://www.apache.org/licenses/LICENSE-2.0                
*                                                              
*  Unless required by applicable law or agreed to in writing,  
*  software distributed under the License is distributed on an 
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY      
*  KIND, either express or implied.  See the License for the   
*  specific language governing permissions and limitations     
*  under the License.                                          
*                                                              
*
*****************************************************************/
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