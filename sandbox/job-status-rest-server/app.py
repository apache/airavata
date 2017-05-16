#
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

from flask import Flask, request
from flask_restful import Resource, Api
from flask import jsonify

from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import smtplib
import sys

app = Flask(__name__)

email_username = ''
email_password = ''
status_string = "COMPLETED"
from_address = 'emailtrigger@scigap.org'


@app.route('/job_status', methods=['POST'])
def job_status():
    name = request.form['job_name']
    job_id = request.form['job_id']
    status = request.form['status']
    emails = request.form.getlist('emails[]')
    if status == "0":
        for to_address in emails:
            msg = MIMEMultipart()
            msg['From'] = from_address
            msg['To'] = to_address
            msg['Subject'] = "Job_id=" + job_id + " Name=" + name + " Status=" + status_string

            server = smtplib.SMTP('smtp.gmail.com:587')
            server.ehlo()
            server.starttls()
            server.login(email_username, email_password)
            server.sendmail(from_address, to_address, msg.as_string())

            server.quit()

    return jsonify({'result': 200})


if __name__ == '__main__':
    app.config.update(
        DEBUG=False
    )
    app.run(
        host="localhost",
        port=int("8089"),
    )
