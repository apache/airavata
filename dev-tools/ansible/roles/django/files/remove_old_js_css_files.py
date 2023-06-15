#!/usr/bin/env python3

import argparse
import glob
import json
import os
import re
import time

BASE_DIR = "/var/www/portals"
IGNORE_DIRS = ['node_modules', 'venv']
# Example filenames:
# - pattern: [name].[checksum].[ext]
#   - example: "chunk-common.c7db5bee.js"
# - pattern: [name].[checksum].chunk.[ext]
#   - example: "main.bbfe2914.chunk.css"
REGEX = re.compile(r"^([\w-]+)\.([a-f0-9]{8})\.((?:chunk\.)?(?:js|js\.map|css|css\.map))$")
MAX_ATIME_AGE_HOURS = 24

parser = argparse.ArgumentParser()
parser.add_argument("-b", "--basedir", default=BASE_DIR, help="base directory from which to look for older built JS/CSS files")
args = parser.parse_args()

for root, dirs, files in os.walk(args.basedir):
    for i in IGNORE_DIRS:
        if i in dirs:
            dirs.remove(i)
    if "webpack-stats.json" in files:
        # print(f"Found webpack-stats.json in {root}")
        with open(os.path.join(root, "webpack-stats.json")) as sf:
            stats = json.load(sf)
        for chunk, files in stats["chunks"].items():
            for f in files:
                dirname, filename = os.path.split(f["name"])
                # capture base name and hash and extension
                m = REGEX.match(filename)
                if m is not None:
                    basename, content_hash, file_ext = m.groups()
                    other_files = glob.glob(os.path.join(root, dirname, basename + ".*." + file_ext))
                    for other_file in other_files:
                        m = REGEX.match(os.path.basename(other_file))
                        other_hash = m.group(2)
                        if other_hash != content_hash:
                            # Check last accessed time and remove file if more than MAX_ATIME_AGE_HOURS old
                            atime = os.stat(other_file).st_atime
                            atime_hours = (time.time() - atime) / 3600
                            if atime_hours > MAX_ATIME_AGE_HOURS:
                                print(f"Deleting alternate of {filename} {file_ext}, {atime_hours} hours old: {other_file}")
                                os.remove(other_file)
                else:
                    raise Exception(f"Regex failed on filename {filename} in {root}")
