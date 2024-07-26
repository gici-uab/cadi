#!/bin/bash
docFinal=./CADIInstallManual

pdflatex $docFinal".tex"

rm -rf $docFinal".log" $docFinal".aux"

