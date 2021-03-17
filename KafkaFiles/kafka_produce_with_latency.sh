awk -F ',' 'NR>1 {print}' < BankChurners.csv | xargs -I % sh  -c '{ echo %; sleep 1; }' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092
