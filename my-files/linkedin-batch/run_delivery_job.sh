CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
#UUID=`uuidgen`
UUID=14
mvn clean package -Dmaven.test.skip=true;
java -jar -Dspring.batch.job.names=deliverPackageJob ./target/linkedin-batch-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=$CURRENT_DATE" "lesson=$LESSON" "uuid=$UUID";
read;
