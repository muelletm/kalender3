#!/bin/bash 
set -ue

dir="$(dirname $0)"

big=$dir/big
small=$dir/small

rm -rf $small
mkdir -p $small

cp $big/*.jpg $small

SIZE=1024000

BIG_FILES=$(find $small -name "*.jpg" -size +1024k)

for file in $BIG_FILES; do

  file_size="$(ls -l $file | awk '{print $5}')"

  target="$(expr 100 \* ${SIZE} \/ $file_size)"

  CMD="convert -resize $target% $file $file"
  echo $CMD
  $CMD

done

index=0

for file in $(find $small -name "*.jpg"); do

  index=$(expr $index + 1)
   
  mv $file $(printf "$small/door_%02d.jpg" $index)
  
  printf "%02d %s\n" $index $file >> $small/original_files.txt

done

ANDROID_DIR="/home/thomas/projects/kalender3/kalender3"
DRAWABLE="${ANDROID_DIR}/app/src/main/res/drawable"
cp $small/*.jpg "${DRAWABLE}"

