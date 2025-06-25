Python Flask framework based REST API for sending job statuses from clusters
============================================================================

Requirement
* pip install flask
* pip install flask-restful

Sample CURL Command
curl -X POST -d "job_id=ABC&job_name=Test&status=0&emails[]=supun.nakandala@gmail.com&emails[]=eabeysin@iu.edu" http://localhost:8089/job_status