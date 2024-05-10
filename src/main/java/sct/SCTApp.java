// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 <LICENSE-APACHE or
// https://www.apache.org/licenses/LICENSE-2.0> or the MIT license
// <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. This file may not be copied, modified, or distributed
// except according to those terms.

package sct;

import java.util.Arrays;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypeDescriptors;
import sct.transforms.ElasticSearchDocumentIndexingFn;


public class SCTApp {
	public interface Options extends StreamingOptions, PipelineOptions, DataflowPipelineOptions {
		@Description("Input text to print.")
		@Default.String("My input text")
		String getInputText();

		void setInputText(String value);


		@Description("Input topic.")
		String getTopic();

		void setTopic(String value);

		@Description("Output ES topic.")
		String getESTopic();

		void setESTopic(String value);

		@Description("Kafka Bootstrap Servers.")
		String getKafkaBootstrapServers();

		void setKafkaBootstrapServers(String value);

		@Description("Kafka Bootstrap Servers.")
		boolean getReplayEnabled();

		void setReplayEnabled(boolean value);

	}

	public static void buildPipeline(Pipeline pipeline, Options options) {

		// 3. Read Indexing Events from Kafka
		PCollection<String> indexingEvents = null;
		// pipeline
		// 		.apply("Read from Kafka", KafkaIO.<String, String>read()
		// 				.withBootstrapServers(options.getKafkaBootstrapServers())
		// 				.withTopic(options.getTopic())
		// 				.withKeyDeserializer(StringDeserializer.class)
		// 				.withValueDeserializer(StringDeserializer.class)
		// 				.withoutMetadata())
		// 		.apply("Values", Values.create() );
		String sub = "projects/radoslaws-playground-pso/subscriptions/frauds-sub";
		indexingEvents = pipeline
				.apply("Read from PS", PubsubIO.readStrings().fromSubscription(sub));
;
		 PCollection<String> replays = null;
		PCollection<String> merged = indexingEvents;
		if (options.getReplayEnabled()) {
			//projects/radoslaws-playground-pso/subscriptions/dql-sub
			replays = pipeline
					.apply("Read Replay", PubsubIO.readStrings()
							.fromSubscription("projects/radoslaws-playground-pso/subscriptions/dql-sub"))
					.apply("map identity", MapElements.into(TypeDescriptors.strings()).via(s -> s));
			PCollectionList<String> list = PCollectionList.of(indexingEvents).and(replays);
			//flatten PCollectionList
			merged = list.apply("Flatten", Flatten.pCollections());
		}

		PCollectionTuple indexed = merged
				.apply("Index Documents",
						ParDo.of(new ElasticSearchDocumentIndexingFn())
								.withOutputTags(ElasticSearchDocumentIndexingFn.BIGQUERY_TAG,
										TupleTagList.of(
						Arrays.asList(ElasticSearchDocumentIndexingFn.SOLR_TAG,ElasticSearchDocumentIndexingFn.ELASTICSEARCH_TAG))));


		// 6. Branching Paths (Conditional)
		if (true) {
			indexed.get(ElasticSearchDocumentIndexingFn.ELASTICSEARCH_TAG)
					.apply("Index to Elasticsearch (Buffered)", PubsubIO.writeStrings()
							.to(options.getESTopic())
					);
		}
	}

	public static void main(String[] args) {
		PipelineOptionsFactory.register(Options.class);
		Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
		Pipeline pipeline = Pipeline.create(options);
		SCTApp.buildPipeline(pipeline, options);
		pipeline.run().waitUntilFinish();
	}
}