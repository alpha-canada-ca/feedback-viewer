#!/bin/bash
# Script to decrypt the IP whitelist file using GPG for local development
# Usage: ./scripts/decrypt_whitelist.sh [passphrase]

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

ENCRYPTED_FILE="secrets/gc_ip_whitelist.txt.gpg"
OUTPUT_FILE="secrets/gc_ip_whitelist.txt"

# Check if encrypted file exists
if [ ! -f "$ENCRYPTED_FILE" ]; then
    echo -e "${RED}Error: Encrypted file $ENCRYPTED_FILE not found${NC}"
    exit 1
fi

# Get passphrase from argument or prompt
if [ -z "$1" ]; then
    echo -e "${YELLOW}Enter GPG passphrase:${NC}"
    read -s PASSPHRASE
    echo
else
    PASSPHRASE="$1"
fi

# Decrypt the file
echo -e "${YELLOW}Decrypting $ENCRYPTED_FILE...${NC}"
gpg --batch --yes --passphrase "$PASSPHRASE" --decrypt --output "$OUTPUT_FILE" "$ENCRYPTED_FILE"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Successfully decrypted to $OUTPUT_FILE${NC}"
    echo -e "${YELLOW}Remember: This file is excluded from git. Never commit the unencrypted version.${NC}"
else
    echo -e "${RED}✗ Decryption failed${NC}"
    exit 1
fi
