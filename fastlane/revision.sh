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