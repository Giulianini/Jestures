#!/bin/bash
set -e
gradle publish || gradle printVersion
cp -R */build/reports/* report