package net.kk17.clickstream.spark.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.kk17.clickstream.spark.dto.ClickstreamData;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @author kk
 */
public class ClickstreamDataDeserializer implements Deserializer<ClickstreamData> {

    private static ObjectMapper objectMapper = new ObjectMapper();

    public ClickstreamData fromBytes(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, ClickstreamData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @Override
    public ClickstreamData deserialize(String s, byte[] bytes) {
        return fromBytes((byte[]) bytes);
    }

    @Override
    public void close() {

    }
}
