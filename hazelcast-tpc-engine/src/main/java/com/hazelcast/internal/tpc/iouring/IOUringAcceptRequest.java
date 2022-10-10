package com.hazelcast.internal.tpc.iouring;

import com.hazelcast.internal.tpc.AcceptRequest;

public class IOUringAcceptRequest implements AcceptRequest {
    final NativeSocket socket;

    public IOUringAcceptRequest(NativeSocket socket) {
        this.socket = socket;
    }
}
