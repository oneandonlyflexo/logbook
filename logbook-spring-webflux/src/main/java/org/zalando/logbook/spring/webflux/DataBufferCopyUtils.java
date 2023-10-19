package org.zalando.logbook.spring.webflux;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;

import java.util.function.Consumer;

public class DataBufferCopyUtils {

    private DataBufferCopyUtils() {}

    public static DefaultDataBufferFactory sharedInstance = new DefaultDataBufferFactory();

    public static Publisher<? extends DataBuffer> wrapAndBuffer(Publisher<? extends DataBuffer> body, Consumer<byte[]> copyConsumer) {
        return DataBufferUtils
                .join(body)
                .defaultIfEmpty(sharedInstance.wrap(new byte[0]))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    DefaultDataBuffer wrappedDataBuffer = sharedInstance.wrap(bytes);
                    copyConsumer.accept(bytes);
                    return wrappedDataBuffer;
                });
    }
}
