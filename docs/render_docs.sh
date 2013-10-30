#!/bin/bash

rm -rf html/
mkdir html
asciidoctor *.adoc
mv *.html html/
