package com.hdu.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeKryoSerializer extends Serializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(Kryo kryo, Output output, LocalDateTime localDateTime) {
        output.writeString(localDateTime.format(formatter));
    }

    @Override
    public LocalDateTime read(Kryo kryo, Input input, Class<LocalDateTime> type) {
        String str = input.readString();
        return LocalDateTime.parse(str, formatter);
    }
}
