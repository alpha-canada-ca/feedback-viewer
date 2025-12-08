#!/bin/bash
# Script to encrypt the IP whitelist file using GPG
# Usage: ./scripts/encrypt_whitelist.sh [passphrase]

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

SOURCE_FILE="secrets/gc_ip_whitelist.txt"
ENCRYPTED_FILE="secrets/gc_ip_whitelist.txt.gpg"

# Check if source file exists
if [ ! -f "$SOURCE_FILE" ]; then
    echo -e "${RED}Error: Source file $SOURCE_FILE not found${NC}"
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

# Encrypt the file
echo -e "${YELLOW}Encrypting $SOURCE_FILE...${NC}"
gpg --batch --yes --passphrase "$PASSPHRASE" --symmetric --cipher-algo AES256 --output "$ENCRYPTED_FILE" "$SOURCE_FILE"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Successfully encrypted to $ENCRYPTED_FILE${NC}"
    echo -e "${YELLOW}Remember to:${NC}"
    echo -e "  1. Add $ENCRYPTED_FILE to git"
    echo -e "  2. Never commit the unencrypted $SOURCE_FILE"
    echo -e "  3. Store the passphrase in GitHub Secrets as GPG_PASSPHRASE"
else
    echo -e "${RED}✗ Encryption failed${NC}"
    exit 1
fi
