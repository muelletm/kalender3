#!/bin/bash 
set -ue

dir="$(dirname $0)"

big=$dir/big
small=$dir/small

rm -rf $small
mkdir -p $small

cp $big/*.png $small
cp $big/*.jpg $small

ALL_FILES=$(find $small -name "*.png")

for file in $ALL_FILES; do
  outfile=$(echo $file | sed 's/png/jpg/g')
  CMD="convert $file $outfile"
  echo $CMD
  $CMD
done

rm $small/*.png

SIZE=1024000

BIG_FILES=$(find $small -name "*.jpg" -size +1024k)

for file in $BIG_FILES; do

  file_size="$(ls -l $file | awk '{print $5}')"

  target="$(expr 100 \* ${SIZE} \/ $file_size)"

  CMD="convert -resize $target% $file $file"
  echo $CMD
  $CMD

done

ANDROID_DIR="/home/thomas/projects/kalender3/kalender3"
DRAWABLE="${ANDROID_DIR}/app/src/main/res/drawable"
cp $small/*.jpg "${DRAWABLE}"

