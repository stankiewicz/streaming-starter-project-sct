package sct.transforms;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.TupleTag;


public class ElasticSearchDocumentIndexingFn extends DoFn<String, String> {

  // TupleTag for BigQuery, Solr and ElasticSearch
  public static final TupleTag<String> BIGQUERY_TAG = new TupleTag<String>() {};
  public static final TupleTag<String> SOLR_TAG = new TupleTag<String>() {};
  public static final TupleTag<String> ELASTICSEARCH_TAG = new TupleTag<String>() {};

  @Setup
  public void setup() {

  }

  @ProcessElement
  public void processElement(ProcessContext c, @Element String inputString, OutputReceiver<String> output) {
    String modifiedString = inputString.toUpperCase();

    c.output(BIGQUERY_TAG, modifiedString);
    c.output(SOLR_TAG, modifiedString);
    c.output(ELASTICSEARCH_TAG, modifiedString);
  }


  //finishBundle

  @FinishBundle
  public void finishBundle(FinishBundleContext c) {
    // skip
  }

  @Teardown
  public void teardown() {
    // skip
  }
}
