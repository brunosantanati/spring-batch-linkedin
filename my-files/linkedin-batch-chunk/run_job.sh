CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
UUID=1
mvn clean package -Dmaven.test.skip=true;
java -jar ./target/linkedin-batch-chunk-0.0.1-SNAPSHOT.jar "run.date(date)=$CURRENT_DATE" "lesson=$LESSON" "uuid=$UUID";
read;
