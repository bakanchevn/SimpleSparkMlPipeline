END=30
for ((i=2;i<=END;i++)); do cat BankChurners.csv | awk -v VAR="$i" '{if (NR==VAR) print}' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092; sleep 1; done;
