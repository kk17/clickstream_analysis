package net.kk17.clickstream.spark.processor;

import net.kk17.clickstream.spark.dto.ClickstreamData;
import net.kk17.clickstream.spark.util.ClickstreamDataDeserializer;
import net.kk17.clickstream.spark.util.PropertyFileReader;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import static org.apache.spark.sql.functions.*;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.CanCommitOffsets;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.ConsumerStrategy;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;
import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author kk
 */
public class StreamingProcessor implements Serializable {
    private static final Logger logger = Logger.getLogger(StreamingProcessor.class);
    private String file;

    public StreamingProcessor(String file) {
        this.file = file;
    }

    public static void main(String[] args) throws Exception {
        String file = "spark.properties";
        StreamingProcessor streamingProcessor = new StreamingProcessor(file);
        streamingProcessor.start();
    }

    private void start() throws Exception {
        Properties prop = PropertyFileReader.readPropertyFile(file);
        Map<String, Object> kafkaProperties = getKafkaParams(prop);
        String[] jars = {
        };
        SparkConf conf = getSparkConf(prop, jars);

        //batch interval of 5 seconds for incoming stream
        JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(5));
        final SparkSession sparkSession = SparkSession.builder().config(conf).getOrCreate();
        jssc.checkpoint(prop.getProperty("net.kk17.clickstream.spark.checkpoint.dir"));
        Map<TopicPartition, Long> lastOffSet = getLatestOffSet(sparkSession, prop);
        JavaInputDStream<ConsumerRecord<String, ClickstreamData>> directKafkaStream = getStream(prop, jssc, kafkaProperties, lastOffSet);

        logger.info("Starting Stream Processing");

        JavaDStream<ClickstreamData> dataStream = directKafkaStream.map(r-> r.value());

        processStream(prop, jssc, sparkSession, dataStream);
        commitOffset(directKafkaStream);

        jssc.start();
        jssc.awaitTermination();
    }

    private void commitOffset(JavaInputDStream<ConsumerRecord<String, ClickstreamData>> directKafkaStream) {
        directKafkaStream.foreachRDD((JavaRDD<ConsumerRecord<String, ClickstreamData>> clickstreamRdd) -> {
            if (!clickstreamRdd.isEmpty()) {
                OffsetRange[] offsetRanges = ((HasOffsetRanges) clickstreamRdd.rdd()).offsetRanges();

                CanCommitOffsets canCommitOffsets = (CanCommitOffsets) directKafkaStream.inputDStream();
                canCommitOffsets.commitAsync(offsetRanges, new ClickstreamDataOffsetCommitCallback());
            }
        });
    }

    private void processStream(Properties prop, JavaStreamingContext jssc, SparkSession spark, JavaDStream<ClickstreamData> dataStream) {
        appendDataToHDFS(prop, spark, dataStream);


        //process data
        RealtimeClickEventDataProcessor clickstreamProcessor = new RealtimeClickEventDataProcessor();
        clickstreamProcessor.processUserStatusData(dataStream);

    }

    private void appendDataToHDFS(Properties prop, SparkSession spark, JavaDStream<ClickstreamData> dataStream) {
        // FIXME: should accumulate data here?
//        dataStream.foreachRDD(rdd -> {
//            if (!rdd.isEmpty()) {
//                Dataset<Row> dataFrame = spark.createDataFrame(rdd, ClickstreamData.class);
//                dataFrame = dataFrame.withColumn("dt", to_date(from_unixtime(col("date").divide(1000))));
//
//                dataFrame.write()
//                    .partitionBy("dt")
//                    .mode(SaveMode.Append)
//                    .parquet(prop.getProperty("net.kk17.clickstream.hdfs") + "clickstream-data-parquet");
//            }
//        });
    }

    private JavaInputDStream<ConsumerRecord<String, ClickstreamData>> getStream(Properties prop, JavaStreamingContext jssc, Map<String, Object> kafkaProperties, Map<TopicPartition, Long> fromOffsets) {
        List<String> topicSet = Arrays.asList(new String[]{prop.getProperty("net.kk17.clickstream.kafka.topic")});
        ConsumerStrategy<String, ClickstreamData> subscribe;
        if (fromOffsets.isEmpty()) {
            subscribe = ConsumerStrategies.<String, ClickstreamData>Subscribe(topicSet, kafkaProperties);
        } else {
            subscribe = ConsumerStrategies.<String, ClickstreamData>Subscribe(topicSet, kafkaProperties, fromOffsets);
        }

        return KafkaUtils.createDirectStream(
            jssc,
            LocationStrategies.PreferConsistent(),
            subscribe
        );
    }

    private Map<TopicPartition, Long> getLatestOffSet(SparkSession sparkSession, Properties prop) {
        Map<TopicPartition, Long> collect = Collections.emptyMap();
        try {
            Dataset<Row> parquet = sparkSession.read()
                .parquet(prop.getProperty("net.kk17.clickstream.hdfs") + "clickstream-data-parque");

            parquet.createTempView("clickstream");
            Dataset<Row> sql = parquet.sqlContext()
                .sql("select max(untilOffset) as untilOffset, topic, kafkaPartition from clickstream group by topic, kafkaPartition");

            collect = sql.javaRDD()
                .collect()
                .stream()
                .map(row -> {
                    TopicPartition topicPartition = new TopicPartition(row.getString(row.fieldIndex("topic")), row.getInt(row.fieldIndex("kafkaPartition")));
                    Tuple2<TopicPartition, Long> key = new Tuple2<>(
                        topicPartition,
                        Long.valueOf(row.getString(row.fieldIndex("untilOffset")))
                    );
                    return key;
                })
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
        } catch (Exception e) {
            return collect;
        }
        return collect;
    }

    private SparkConf getSparkConf(Properties prop, String[] jars) {
        SparkConf conf =  new SparkConf()
            .setAppName(prop.getProperty("net.kk17.clickstream.spark.app.name"))
            .set("spark.cassandra.connection.host", prop.getProperty("net.kk17.clickstream.cassandra.host"))
            .set("spark.cassandra.connection.port", prop.getProperty("net.kk17.clickstream.cassandra.port"))
            .set("spark.cassandra.auth.username", prop.getProperty("net.kk17.clickstream.cassandra.username"))
            .set("spark.cassandra.auth.password", prop.getProperty("net.kk17.clickstream.cassandra.password"))
            .set("spark.cassandra.connection.keep_alive_ms", prop.getProperty("net.kk17.clickstream.cassandra.keep_alive"))
            ;
        if (prop.contains("net.kk17.clickstream.spark.master")) {
            conf.setMaster(prop.getProperty("net.kk17.clickstream.spark.master"));
        }

        return conf;
    }

    private Map<String, Object> getKafkaParams(Properties prop) {
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, prop.getProperty("net.kk17.clickstream.kafka.brokerlist"));
        kafkaProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ClickstreamDataDeserializer.class);
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, prop.getProperty("net.kk17.clickstream.kafka.topic"));
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, prop.getProperty("net.kk17.clickstream.kafka.resetType"));
        kafkaProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return kafkaProperties;
    }

}

final class ClickstreamDataOffsetCommitCallback implements OffsetCommitCallback, Serializable {

    private static final Logger log = Logger.getLogger(ClickstreamDataOffsetCommitCallback.class);

    @Override
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
        log.info("---------------------------------------------------");
        log.info(String.format("{0} | {1}", new Object[]{offsets, exception}));
        log.info("---------------------------------------------------");
    }
}
