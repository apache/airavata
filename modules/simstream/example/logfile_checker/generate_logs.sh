#!/usr/bin/env bash

outfile="test.txt"

echo "[STATUS] Starting logfile generator" >> $outfile

sleep 2

echo "[STATUS] Doing stuff" >> $outfile
echo "Stuff that doesn't need to be reported" >> $outfile
echo "Stuff that also doesn't need to be reported" >> $outfile
echo "[DATA] 7.267" >> $outfile

sleep 2

echo "[STATUS] Doing more stuff" >> $outfile
echo "Yet more stuff that doesn't need to be reported" >> $outfile
echo "[ERROR] Some non-fatal error that the user should know about" >> $outfile

sleep 2

echo "[STATUS] Finished generating logs" >> $outfile