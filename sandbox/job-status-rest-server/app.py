from flask import Flask, request
from flask_restful import Resource, Api
from flask import jsonify

from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import smtplib
import sys

app = Flask(__name__)

email_username = 'gw56jobs@scigap.org'
email_password = 'XseDe2015'
status_string = "COMPLETED"
from_address = 'gw56jobs@scigap.org'


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
        DEBUG=True
    )
    app.run(
        host="localhost",
        port=int("8089"),
    )
