import requests

r = requests.post("http://localhost:8089/job_status", data={
    'job_id': 12524, 'job_name': 'issue', 'status': '0', 'emails[]': ['supun.nakandala@gmail.com']
})
print(r.status_code, r.reason)
