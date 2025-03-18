# Java formatter Makefile
# Keep this file locally, don't commit it to the repository

# Variables
FORMATTER_VERSION = 1.17.0
FORMATTER_JAR = google-java-format-$(FORMATTER_VERSION)-all-deps.jar
FORMATTER_URL = https://github.com/google/google-java-format/releases/download/v$(FORMATTER_VERSION)/$(FORMATTER_JAR)
JAVA_FILES = $(shell find src -name "*.java")

# Default target
.PHONY: help
help:
	@echo "Available targets:"
	@echo "  format     - Format all Java files using Google Java Format"
	@echo "  check      - Check if files need formatting without changing them"
	@echo "  clean      - Remove the formatter jar"
	@echo ""
	@echo "Note: Keep this Makefile locally, don't commit it to your repository"

# Download the formatter if it doesn't exist
$(FORMATTER_JAR):
	@echo "Downloading Google Java Format..."
	@curl -L $(FORMATTER_URL) -o $(FORMATTER_JAR)

# Format all Java files
.PHONY: format
format: $(FORMATTER_JAR)
	@echo "Formatting Java files..."
	@java -jar $(FORMATTER_JAR) --replace $(JAVA_FILES)
	@echo "Formatting complete!"

# Check if files need formatting without changing them
.PHONY: check
check: $(FORMATTER_JAR)
	@echo "Checking Java files for formatting issues..."
	@java -jar $(FORMATTER_JAR) --dry-run --set-exit-if-changed $(JAVA_FILES)

# Clean up
.PHONY: clean
clean:
	@echo "Removing formatter jar..."
	@rm -f $(FORMATTER_JAR)