from datetime import datetime
import piexif
import glob
import re
import sys
import os

dates = {
  "IMG-20191222-WA0054.jpg": (2019, 12, 22),
  "bild_00.jpg": (2020, 6, 1),
  "IMG-20200719-WA0014.jpg": (2020, 7, 18),
  "IMG-20200810-WA0000.jpg": (2020, 8, 1),
  "IMG-20200103-WA0000.jpg": (2019, 12, 30),
  "IMG-20200204-WA0016.jpg": (2020, 2, 1),
}

for arg in sys.argv:
  for root, dirs, files in os.walk(arg):
    for basename in files:
      filename = os.path.join(root, basename)
      
      try:
        exif_dict = piexif.load(filename)
      except:
        print ('EXIF: %s' % filename)
        continue  
      if not 'Exif' in exif_dict:
        print ('Weird: %s' % (filename))
        continue  
      if piexif.ExifIFD.DateTimeOriginal not in exif_dict['Exif']:
        date = dates[basename]
        print(basename, date)
        year, month, day = date
        try:
          exif_dict['Exif'] = { piexif.ExifIFD.DateTimeOriginal: datetime(
            year, month, day, 0, 0, 0).strftime("%Y:%m:%d %H:%M:%S") }
        except ValueError:
          print ('Error: %s' % filename)
        exif_bytes = piexif.dump(exif_dict)
        piexif.insert(exif_bytes, filename)
        print ('Updated: %s' % filename)



