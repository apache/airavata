#!/usr/bin/env python

import argparse
import os
import subprocess

BASEDIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

parser = argparse.ArgumentParser(description="Run npm in Docker container")
parser.add_argument('commands',
                    metavar="COMMANDS",
                    nargs="+",
                    help="npm sub command and arguments")
args = parser.parse_args()

if not os.path.exists(os.path.join(os.getcwd(), "package.json")):
    raise Exception("No package.json file in the local directory! Are you "
                    "sure you're in a Node package directory?")

subprocess.check_call(["docker",
                       "build",
                       "-t",
                       "airavata-django-build",
                       os.path.join("scripts", "docker-build")],
                      cwd=BASEDIR)

app_dir = os.path.relpath(os.getcwd(), start=BASEDIR)

docker_run_npm_cmd = ["docker",
                      "run",
                      "-p",
                      "9000:9000",
                      "-v",
                      f"{BASEDIR}:/code",
                      "-w",
                      os.path.join("/code", app_dir),
                      "--rm",
                      "airavata-django-build",
                      "npm"]

docker_run_npm_cmd.extend(args.commands)
subprocess.check_call(docker_run_npm_cmd)
