cat BankChurners.csv | awk '{if(NR>1 && NR<20)print}' |  kafka-console-producer --topic client_in --bootstrap-server localhost:9092
