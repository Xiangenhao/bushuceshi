package org.example.afd.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义的LocalDateTime序列化器
 * 将LocalDateTime序列化为带时区的ISO-8601格式，解决Android客户端解析问题
 */
public class JacksonLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    
    /**
     * ISO-8601格式的日期时间，带时区Z (UTC)
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter FORMATTER = 
            DateTimeFormatter.ofPattern(DATETIME_FORMAT)
                    .withZone(ZoneId.of("UTC"));
    
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        // 将LocalDateTime转换为UTC时区
        ZonedDateTime utcDateTime = value.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"));
        
        // 格式化为带Z时区标记的字符串
        String formattedDate = FORMATTER.format(utcDateTime);
        gen.writeString(formattedDate);
    }
} 