#! /bin/sh

# Extract info from PDF given as argument.
#
# Write OCR with layout info to PDF_BASENAME-ocrlayout.xml
# Split PDF into images of form PDF_BASENAME-??.jpg

echo "Processing $1"

pdftotext -bbox-layout "$1" tmp.xml || echo "Failure extracting ocr layout"

# Fix invalid characters pdftotext likes to spit out
if [[ -f "tmp.xml" ]]; then
  xmllint --recover tmp.xml 2>/dev/null > $(basename "$1" .pdf)-ocrlayout.xml && rm tmp.xml
fi

# Use pdftocairo rather than pdfimages because it seems to handle rotation.
pdftocairo -jpeg "$1" $(basename "$1" .pdf) || echo "Failure extracting images"
