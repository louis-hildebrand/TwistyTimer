#!/bin/bash

echo "Cleaning TwistyTimer..."
git clean -xdi -e local.properties -e .idea/

cd lib/vintage-chroma
echo "Cleaning VintageChroma..."
git clean -xdi
