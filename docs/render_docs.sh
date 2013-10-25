#!/bin/bash

rm html/*
asciidoctor *.adoc
mv *.html html/
