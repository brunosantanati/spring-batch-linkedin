$CURRENT_DATE = GET-DATE -Format "yyyy/dd/MM"
$LESSON = pwd | Select-Object | %{$_.ProviderPath.Split("\")[-1]}
mvn clean package "-Dmaven.test.skip=true";

$JAR_PATH = Resolve-Path ./target/linkedin-batch-*-*-0.0.1-SNAPSHOT.jar
java -jar $JAR_PATH "run.date(date)=$CURRENT_DATE" "lesson=$LESSON";
pause;