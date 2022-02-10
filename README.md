# Introduction

This projects indexes PDFs, splits them into images, makes the OCR text searchable, and the documents accessible with a IIIF viewer.
The stack consisting of MongoDB, RESTHeart, Cantaloupe, some Java tools, and a simple JavaScript viewer which uses Mirador.

# Architecture

![Architecture Diagram](arch.png)

The command line tools process a set of PDFs and makes them available on a website. MongoDB is used to store information about the documents and search the OCR'd text. RESTHeart is a REST API on top of MongoDB. It manages access to MongoDB and also static data such as PDF's and IIIF representations of those PDF's. An index on the OCR'd text is created to allow keyword searching. Cantaloupe is an image server which implements the IIIF Image API. The command line tools split the PDF's into images to make them available to Cantaloupe. (For this demo, JPEG format is used. In production, JP2 using the Kakadu image library would be more performant.) Every image of a PDF can be accessed by its own URL. The demo website lets the documents be searched and then viewed with Mirador 3, a IIIF viewer.

# Build

The Java tool scan be built using maven with:

```
mvn package
```

# Process and import data

This should be able to handle a heirarchy of PDFs with a single pdf in a leaf directory. 
Requires Imagemagick, xmllint, and poppler-utils.

Grab the code:
```
git clone https://github.com/markpatton/oida-experiment
```

Make the PDFs available as `./data'. Then run a processing step to plit the pdfs into images and turn the OCR into an XML file.

```
./process_insys data/
```

Write out IIIF manifest and annotation files for the PDFs
```
java -jar oida-cli/target/oida-cli-0.1-jar-with-dependencies.jar write-iiif data/
```

Produce an ingest file for the database:
```
java -jar oida-cli/target/oida-cli-0.1-jar-with-dependencies.jar serialize-restheart data >data.json
```

Note that for the Java tool you can set the base urls used for static data and IIIF images by using the system properties 
`oida.base_data_url` and `oida.base_image_url` respectively.

# Start the stack

```
docker-compose up -d
```

Configure the database:
```
./setup_db.sh
```

Load the data

```
curl --user admin:secret -H "Content-Type:application/json" localhost:8080/oida/doc -d @data.json
```

# Web interface

The web application in oida-ui can be built with:
```
npm install
npm run build
```

You may need to force the npm install to get past some dependency conflicts between mirador and mirador-annotation.

The simple web site in oida-ui is bind mounted into restheart and made available at http://localhost:8080/oida-ui/.
Having the UI on the same domain as the static data also served out by restheart gets around some CORS issues. Make sure
you visit oida-ui using the same domain as the static data.

You can also visit http://localhost:8080/oida-ui/uv.html to see a simple demo of Universal Viewer.
Use a manifest URL found in the main demo to try it.