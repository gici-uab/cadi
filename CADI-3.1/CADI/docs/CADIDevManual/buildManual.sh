#!/bin/bash
docFinal=./CADIDevManual

pdflatex $docFinal".tex"

rm -rf $docFinal".log" $docFinal".aux"

