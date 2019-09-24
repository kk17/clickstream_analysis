package net.kk17.clickstream.spark.processor;

import com.datastax.spark.connector.japi.CassandraJavaUtil;
import net.kk17.clickstream.spark.dto.ClickstreamData;
import net.kk17.clickstream.spark.entity.UserStatusData;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.datastax.spark.connector.japi.CassandraStreamingJavaUtil.javaFunctions;

/**
 * @author kk
 */
public class RealtimeClickEventDataProcessor {
    private static final Logger logger = Logger.getLogger(RealtimeClickEventDataProcessor.class);
    private Function<Tuple2<String, ClickstreamData>, UserStatusData> userStatusFunc = (tuple -> {
        logger.info("User status : " + tuple._2.getUserID());
        UserStatusData userStatus = new UserStatusData();
        userStatus.setUserId(tuple._2.getUserID());
        userStatus.setCartAmount(tuple._2.getCartAmount());
        userStatus.setLastSessionId(tuple._2.getSessionID());
        userStatus.setLastVisitTime(new Date(tuple._2.getDate()));

        return userStatus;
    });


    public void processUserStatusData(JavaDStream<ClickstreamData> directKafkaStream) {
        JavaPairDStream<String, ClickstreamData> userLastEventDStreamPair = directKafkaStream
            .filter(click -> click.getUserID() != null)
            .mapToPair(click -> new Tuple2<>(click.getUserID(), click))
            .reduceByKey((a, b) -> {
                if (a.getDate() > b.getDate()) {
                    return a;
                } else {
                    return b;
                }
            });

        JavaDStream<UserStatusData> userStatusDStream = userLastEventDStreamPair.map(userStatusFunc);

        // Map Cassandra table column
        Map<String, String> columnNameMappings = new HashMap<String, String>();
        columnNameMappings.put("userId", "userid");
        columnNameMappings.put("lastSessionId", "lastsessionid");
        columnNameMappings.put("cartAmount", "cartamount");
        columnNameMappings.put("lastVisitTime", "lastvisittime");

        // call CassandraStreamingJavaUtil function to save in DB
        javaFunctions(userStatusDStream).writerBuilder(
            "clickstreamkeyspace",
            "userstatus",
            CassandraJavaUtil.mapToRow(UserStatusData.class, columnNameMappings)
        ).saveToCassandra();
    }
}
