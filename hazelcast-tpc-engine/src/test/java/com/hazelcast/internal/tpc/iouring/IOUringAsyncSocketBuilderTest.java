package com.hazelcast.internal.tpc.iouring;

import com.hazelcast.internal.tpc.AsyncSocketBuilderTest;
import com.hazelcast.internal.tpc.ReactorBuilder;

public class IOUringAsyncSocketBuilderTest extends AsyncSocketBuilderTest {
    @Override
    public ReactorBuilder newReactorBuilder() {
        return new IOUringReactorBuilder();
    }
}
