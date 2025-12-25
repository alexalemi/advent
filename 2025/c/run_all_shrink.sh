#!/bin/bash
# Run shrink on all regions, log results
echo "Starting shrink for all regions at $(date)"
echo "Region,Original,Shrunk,Percent" > shrink_results.csv

for i in $(seq 0 999); do
    result=$(timeout 120 ./p12_shrink $i 2>&1)
    if echo "$result" | grep -q "Found tightest"; then
        orig=$(echo "$result" | grep "Original:" | grep -oP '\d+ cells' | grep -oP '\d+')
        shrunk=$(echo "$result" | grep "Found tightest" | grep -oP '= \d+ cells' | grep -oP '\d+')
        pct=$(echo "scale=1; 100*$shrunk/$orig" | bc)
        echo "$i,$orig,$shrunk,$pct" >> shrink_results.csv
        echo "Region $i: $orig -> $shrunk ($pct%)"
    elif echo "$result" | grep -q "No tighter fit"; then
        echo "Region $i: no improvement possible"
    fi
done

echo "Done at $(date)"
