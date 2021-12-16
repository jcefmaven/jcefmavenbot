#!/bin/bash

echo "Patching template..."
sed "s|{TOKEN}|$TOKEN|;s|{BUILD}|$BUILD|;s|{ISSUE}|$ISSUE|" /app/config.json.template > /app/config.json

#Continue execution
exec "$@"