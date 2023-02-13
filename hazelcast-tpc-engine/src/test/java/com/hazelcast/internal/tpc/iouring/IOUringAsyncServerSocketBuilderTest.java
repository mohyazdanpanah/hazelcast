package com.hazelcast.internal.tpc.iouring;

import com.hazelcast.internal.tpc.AsyncServerSocketBuilderTest;
import com.hazelcast.internal.tpc.ReactorBuilder;

public class IOUringAsyncServerSocketBuilderTest extends AsyncServerSocketBuilderTest {
    @Override
    public ReactorBuilder newReactorBuilder() {
        return new IOUringReactorBuilder();
    }
}
