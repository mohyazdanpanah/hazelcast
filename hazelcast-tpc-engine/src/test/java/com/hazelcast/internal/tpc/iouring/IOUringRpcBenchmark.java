/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.tpc.iouring;


import com.hazelcast.internal.tpc.AsyncServerSocket;
import com.hazelcast.internal.tpc.AsyncSocket;
import com.hazelcast.internal.tpc.ReadHandler;
import com.hazelcast.internal.tpc.iobuffer.IOBuffer;
import com.hazelcast.internal.tpc.iobuffer.IOBufferAllocator;
import com.hazelcast.internal.tpc.iobuffer.NonConcurrentIOBufferAllocator;
import com.hazelcast.internal.util.ThreadAffinity;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import static com.hazelcast.internal.tpc.TpcTestSupport.constructComplete;
import static com.hazelcast.internal.tpc.util.BitUtil.SIZEOF_INT;
import static com.hazelcast.internal.tpc.util.BitUtil.SIZEOF_LONG;

/**
 * A very trivial benchmark to measure the throughput we can get with a RPC system.
 * <p>
 * JMH would be better; but for now this will give some insights.
 */
public class IOUringRpcBenchmark {

    public static int serverCpu = -1;
    public static int clientCpu = -1;
    public static boolean spin = true;
    public static int requestTotal = 1000 * 1000;
    public static int concurrency = 10;

    public static void main(String[] args) throws InterruptedException {
        SocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5000);

        AsyncServerSocket socket = newServer(serverAddress);

        CountDownLatch latch = new CountDownLatch(concurrency);

        AsyncSocket clientSocket = newClient(serverAddress, latch);

        System.out.println("Starting");

        long startMs = System.currentTimeMillis();

        for (int k = 0; k < concurrency; k++) {
            IOBuffer buf = new IOBuffer(128, true);
            buf.writeInt(-1);
            buf.writeLong(requestTotal / concurrency);
            constructComplete(buf);
            clientSocket.write(buf);
        }
        clientSocket.flush();

        latch.await();

        long duration = System.currentTimeMillis() - startMs;
        float throughput = requestTotal * 1000f / duration;
        System.out.println("Throughput:" + throughput);
        System.exit(0);
    }

    private static AsyncSocket newClient(SocketAddress serverAddress, CountDownLatch latch) {
        IOUringReactorBuilder config = new IOUringReactorBuilder();
        if (clientCpu >= 0) {
            config.setThreadAffinity(new ThreadAffinity("" + clientCpu));
        }
        config.setSpin(spin);
        IOUringReactor clientReactor = new IOUringReactor(config);
        clientReactor.start();

        AsyncSocket clientSocket = clientReactor.openTcpAsyncSocket();
        clientSocket.setTcpNoDelay(true);
        clientSocket.setReadHandler(new ReadHandler() {
            private final IOBufferAllocator responseAllocator = new NonConcurrentIOBufferAllocator(8, true);

            @Override
            public void onRead(ByteBuffer receiveBuff) {
                for (; ; ) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    if (receiveBuff.remaining() < SIZEOF_INT + SIZEOF_LONG) {
                        return;
                    }

                    int size = receiveBuff.getInt();

                    if (size == 0) {
                        throw new RuntimeException();
                    }

                    long l = receiveBuff.getLong();

                    if (l % 100000 == 0) {
                        System.out.println("at:" + l);
                    }
                    if (l == 0) {
                        latch.countDown();
                    } else {
                        IOBuffer buf = responseAllocator.allocate(8);
                        buf.writeInt(-1);
                        buf.writeLong(l);
                        constructComplete(buf);
                        socket.unsafeWriteAndFlush(buf);
                    }
                }
            }
        });
        clientSocket.start();
        clientSocket.connect(serverAddress).join();
        return clientSocket;
    }

    private static AsyncServerSocket newServer(SocketAddress serverAddress) {
        IOUringReactorBuilder config = new IOUringReactorBuilder();
        config.setSpin(spin);
        if (serverCpu >= 0) {
            config.setThreadAffinity(new ThreadAffinity("" + serverCpu));
        }
        IOUringReactor serverReactor = new IOUringReactor(config);
        serverReactor.start();

        AsyncServerSocket serverSocket = serverReactor.openTcpAsyncServerSocket();
        serverSocket.setReusePort(true);
        serverSocket.bind(serverAddress);
        serverSocket.accept(acceptRequest -> {
            AsyncSocket socket = serverReactor.openAsyncSocket(acceptRequest);
            socket.setTcpNoDelay(true);
            socket.setReadHandler(new ReadHandler() {
                private final IOBufferAllocator responseAllocator = new NonConcurrentIOBufferAllocator(8, true);

                @Override
                public void onRead(ByteBuffer receiveBuff) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }

                    for (; ; ) {
                        if (receiveBuff.remaining() < SIZEOF_INT + SIZEOF_LONG) {
                            return;
                        }
                        int size = receiveBuff.getInt();
                        long l = receiveBuff.getLong();

                        if (size == 0) {
                            throw new RuntimeException();
                        }

                        IOBuffer buf = responseAllocator.allocate(8);
                        buf.writeInt(-1);
                        buf.writeLong(l - 1);
                        constructComplete(buf);
                        socket.unsafeWriteAndFlush(buf);
                    }
                }
            });
            socket.start();
        });

        return serverSocket;
    }
}
