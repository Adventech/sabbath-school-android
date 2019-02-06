#!/bin/bash

for i in "$@"
do
case $i in
    -c=*|--create=*)
    REVISION="${i#*=}"
    shift
    ;;
    -t=*|--translate=*)
    TRANSLATE="${i#*=}"
    shift
    ;;
    -f=*|--frameit=*)
    FRAMEIT="${i#*=}"
    shift
    ;;
    -l=*|--locale=*)
    LOCALE="${i#*=}"
    shift
    ;;
    --default)
    DEFAULT=YES
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

if [[ $REVISION ]]; then
    for dir in metadata/android/*/changelogs; do touch -- "$dir/$REVISION.txt" && echo "Creating revision $dir/$REVISION.txt"; done;
    # for dir in metadata/android/*/changelogs; do echo "Creating revision $dir/$REVISION.txt"; done;
fi

if [[ $TRANSLATE ]]; then
    for dir in metadata/android/*; do
        if [ -d $dir ]; then
            LANGUAGE="${dir//metadata\/android\//}"

            if [ "$LANGUAGE" != "en-US" ]; then
                echo "Translating $LANGUAGE"

                if [ "$LANGUAGE" == "sr" ]; then
                    LANGUAGE=sr-Latn
                fi

                ./trans -b -i metadata/android/en-US/changelogs/$TRANSLATE.txt -o $dir/changelogs/$TRANSLATE.txt -no-bidi :$LANGUAGE
            fi;
        fi
    done;
fi

if [[ $FRAMEIT ]]; then
    LANGUAGE_TMP=""
    INCR=0
    find metadata/android/$LOCALE/images/phoneScreenshots/ -name "*.png-framed.png" -exec rm '{}' \;
    for file in metadata/android/$LOCALE/images/phoneScreenshots/*.png; do

        LANGUAGE="${file//metadata\/android\//}"
        LANGUAGE="${LANGUAGE:0:2}"

        echo "Processing $file"

        if [ "$LANGUAGE_TMP" != "$LANGUAGE" ]; then
            INCR=0
        fi

        TITLE_GLOBAL=$(eval "cat Framefile.json | jq -r '.global[$INCR].title'")
        FONT_GLOBAL=$(eval "cat Framefile.json | jq -r '.global[$INCR].font'")

        TITLE_LOCAL=$(eval "cat metadata/android/$LOCALE/Framefile.json | jq -r '.[$INCR].title'")
        FONT_LOCAL=$(eval "cat metadata/android/$LOCALE/Framefile.json | jq -r '.[$INCR].font'")

        if [ ! -z "$TITLE_LOCAL" -a "$TITLE_LOCAL" != "null" ]; then
            TITLE=$TITLE_LOCAL
        else
            TITLE=$TITLE_GLOBAL
        fi

        if [ ! -z "$FONT_LOCAL" -a "$FONT_LOCAL" != "null" ]; then
            FONT=$FONT_LOCAL
        else
            FONT=$FONT_GLOBAL
        fi

        if [ "$LANGUAGE" == "ar" ] || [ "$LANGUAGE" == "fa" ] || [ "$LANGUAGE" == "he" ]; then
            TITLE=$(echo $TITLE | rev)
        fi

        LANGUAGE_TMP=$LANGUAGE
        INCR=$((INCR+1))
        OUTPUT="$file-framed.png"

        convert $file \( +clone -alpha extract -draw 'fill black polygon 0,0 0,85 85,0 fill white circle 85,85 85,0' \( +clone -flip \) -compose Multiply -composite \( +clone -flop \) -compose Multiply -composite \) -alpha off -compose CopyOpacity -composite -resize 1064x2125 rounded_temp.png \
        && \
        convert google_pixel_2_xl_frame.png rounded_temp.png -gravity center -geometry +13+700 -composite -font $FONT -pointsize 135 -fill '#303E55' -annotate +0-1000 ''"$TITLE"'' $OUTPUT
    done;
fi