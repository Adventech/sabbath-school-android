for dir in metadata/android/*/changelogs; do touch -- "$dir/27.txt"; done;



///

find . -name "*.png" -exec convert google_pixel_frame.png '{}' -gravity center -geometry -6-22 -composite '{}' \;