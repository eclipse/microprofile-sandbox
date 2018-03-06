#!/bin/sh

topic=$1
name=$2
email=$3

echo '{"type": "UPDATED", "id": "6b46e75c-2e1e-4a91-b7a0-ff785c305a01", "name": "'$2'", "email": "'$3'"}' | kafkacat -P -b localhost:9092 -t user-details.events$1
