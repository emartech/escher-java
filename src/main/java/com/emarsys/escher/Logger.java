package com.emarsys.escher;

import java.util.function.Consumer;

class Logger {

    private static Consumer<String> consumer = msg -> {};


    public static void setConsumer(Consumer<String> consumer) {
        Logger.consumer = consumer;
    }


    public static void log(String message) {
        consumer.accept(message);
    }

}
