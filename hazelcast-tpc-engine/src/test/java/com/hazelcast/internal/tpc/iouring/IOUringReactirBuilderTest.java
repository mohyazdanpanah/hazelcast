package com.hazelcast.internal.tpc.iouring;

import com.hazelcast.internal.tpc.ReactorBuilder;
import com.hazelcast.internal.tpc.ReactorBuilderTest;

public class IOUringReactirBuilderTest extends ReactorBuilderTest {

    @Override
    public ReactorBuilder newBuilder() {
        return new IOUringReactorBuilder();
    }
}
