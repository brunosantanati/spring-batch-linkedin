CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
UUID=2
mvn clean package -Dmaven.test.skip=true;
java -jar -Dspring.batch.job.names=prepareFlowersJob ./target/linkedin-batch-0.0.1-SNAPSHOT.jar "run.date(date)=$CURRENT_DATE" "lesson=$LESSON" type=$1 "uuid=$UUID";
read;
