# building

1. profile - dataflow or direct-runner
2. mvn -P dataflow-runner package 


# running locally (direct-runner)
0. mvn -P direct-runner package
1. this example reads from pubsub so authenticate: gcloud auth application-default login 
2. java -cp target/starter-streaming-project-bundled-0.1.jar sct.SCTApp  --topic=projects/radoslaws-playground-pso/subscriptions/frauds-sub --kafkaBootstrapServers=1.2.3.4 --replayEnabled=false --ESTopic=projects/radoslaws-playground-pso/topics/elastic

# running on dataflow
1. mvn -P dataflow-runner package 
2. review flags - https://cloud.google.com/dataflow/docs/guides/setting-pipeline-options
3. java -cp target/starter-streaming-project-bundled-0.1.jar sct.SCTApp  --topic=projects/radoslaws-playground-pso/subscriptions/frauds-sub --kafkaBootstrapServers=1.2.3.4 --replayEnabled=false --ESTopic=projects/radoslaws-playground-pso/topics/elastic --runner=DataflowRunner --region=us-central1 --gcpTempLocation=gs://radoslaws-playground-pso/temp/  --project=radoslaws-playground-pso --enableStreamingEngine=true  --maxNumWorkers=10 --usePublicIps=false --subnetwork=projects/radoslaws-playground-pso/regions/us-central1/subnetworks/default --serviceAccount=96401984427-compute@developer.gserviceaccount.comjava -cp target/starter-streaming-project-bundled-0.1.jar sct.SCTApp  --topic=projects/radoslaws-playground-pso/subscriptions/frauds-sub --kafkaBootstrapServers=1.2.3.4 --replayEnabled=false --ESTopic=projects/radoslaws-playground-pso/topics/elastic --runner=DataflowRunner --region=us-central1 --gcpTempLocation=gs://radoslaws-playground-pso/temp/  --project=radoslaws-playground-pso --enableStreamingEngine=true  --maxNumWorkers=10 --usePublicIps=false --subnetwork=projects/radoslaws-playground-pso/regions/us-central1/subnetworks/default --serviceAccount=96401984427-compute@developer.gserviceaccount.com
