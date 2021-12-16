#!/bin/bash

echo "Patching template..."
sed "s|<TOKEN>|$TOKEN|" /app/config.json.template > /app/config.json