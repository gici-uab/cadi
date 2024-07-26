#!/bin/bash
docFinal=./CADIUserManual

pdflatex $docFinal".tex"

rm -rf $docFinal".log" $docFinal".aux"

