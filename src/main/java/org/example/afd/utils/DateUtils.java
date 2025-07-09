package org.example.afd.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_SIMPLE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    /**
     * 格式化LocalDateTime为字符串(yyyy-MM-dd HH:mm:ss)
     */
    public static String formatLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * 格式化LocalDateTime为简单字符串(MM-dd HH:mm)
     */
    public static String formatLocalDateTimeSimple(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_SIMPLE_FORMATTER);
    }

    /**
     * 格式化LocalDateTime为日期字符串(yyyy-MM-dd)
     */
    public static String formatLocalDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 格式化LocalDateTime为时间字符串(HH:mm:ss)
     */
    public static String formatLocalTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(TIME_FORMATTER);
    }

    /**
     * 获取相对时间描述（如：刚刚、5分钟前、1小时前、昨天等）
     */
    public static String getTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 24 * 60) {
            long hours = minutes / 60;
            return hours + "小时前";
        } else if (minutes < 48 * 60) {
            return "昨天";
        } else if (minutes < 7 * 24 * 60) {
            long days = minutes / (24 * 60);
            return days + "天前";
        } else {
            return formatLocalDateTimeSimple(dateTime);
        }
    }

    /**
     * 获取相对时间描述（日期对象版本）
     */
    public static String getTimeAgo(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return getTimeAgo(dateTime);
    }

    /**
     * Date转LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
} 