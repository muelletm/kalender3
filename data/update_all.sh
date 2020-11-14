#!/bin/bash 
set -ue

dir="$(dirname $0)"
$dir/doors/smallify.sh
$dir/drawings/smallify.sh
$dir/photos/smallify.sh
