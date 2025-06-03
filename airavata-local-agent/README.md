# Cybershuttle Local Agent (v2)

*Ganning Xu*, GSoC 2024 Project, Developed with mentors from the [Apache Software Foundation](https://www.apache.org/)

After presenting the first version of Cybershuttle Local Agent at Auburn, we sought feedback from attendees. Thus, we learned that ***scientists wanted a way to run Jupyter Labs both on their local machine and in remote environments, all in the same Jupyter Notebook.***

Thus, we began building Cybershuttle Local Agent version 2 to present at [PEARC24](https://pearc.acm.org/pearc24/). 

After speaking with Dimuthu and Lahiru, we decided it would be best to separately create a Jupyter Notebook that supports both local and remote execution. This notebook would then be packaged into a Docker container to be rendered in the local agent.

Dimuthu and Lahiru developed the Docker container that housed the custom Jupyter Lab.

I developed the local agent. The result can be seen in this demo:

<iframe width="100%" height="400" src="https://www.youtube.com/embed/nSJ9H-gQ8rk?si=u9tbtus_J_C8fIKY" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

## Features
- Allows for launching Docker containers with a custom image that allows for both remote and local execution through Jupyter Notebook.
- Allows for listing, viewing, deleting containers.
- Easily login with CILogon (supports most universities, Google, GitHub, etc.)

Thus, version 2 of the Cybershuttle Local Agent was developed and presented at PEARC 24. The local agent is also available for download at [https://md.cybershuttle.org/](https://md.cybershuttle.org).

## License
```
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.    
```
