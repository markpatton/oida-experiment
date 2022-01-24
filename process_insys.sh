#! /bin/sh

# Process PDFs from Insys data dump directory given as argument.

process_pdf=$(realpath ./process_pdf.sh)
find "$1" -name *.pdf -execdir "$process_pdf" '{}' \;
