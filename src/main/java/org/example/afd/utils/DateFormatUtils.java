package org.example.afd.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期格式化工具类
 * 专门用于处理与客户端交互的日期格式，确保包含时区信息
 */
public class DateFormatUtils {

    /**
     * ISO-8601格式，包含Z时区标记(UTC)
     */
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    private static final DateTimeFormatter ISO_FORMATTER = 
            DateTimeFormatter.ofPattern(ISO_8601_FORMAT)
                    .withZone(ZoneId.of("UTC"));
    
    /**
     * 格式化LocalDateTime为带时区的ISO字符串
     */
    public static String formatToIsoWithZone(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        // 转换为UTC时区的ZonedDateTime
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC);
        
        return ISO_FORMATTER.format(zonedDateTime);
    }
    
    /**
     * 格式化Date为带时区的ISO字符串
     */
    public static String formatToIsoWithZone(Date date) {
        if (date == null) {
            return null;
        }
        
        return ISO_FORMATTER.format(date.toInstant());
    }
    
    /**
     * 解析带时区的ISO字符串为LocalDateTime
     */
    public static LocalDateTime parseFromIsoWithZone(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        
        // 解析为ZonedDateTime，然后转换为系统默认时区的LocalDateTime
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr, ISO_FORMATTER);
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }
} 