
package com.hazelcast.internal.alto.apps;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.table.Pipeline;
import com.hazelcast.table.Table;

public class PipelineMain {

    public static long rounds = 1000 * 1000;
    public static long pipelineSize = 1;
    public static long hashtableSize = 10_000_000;

    public static void main(String[] args) throws Exception {
        System.setProperty("hazelcast.alto.enabled", "true");
        System.setProperty("hazelcast.partition.count","1");// for maximum pressure on the partition
        System.setProperty("hazelcast.tpc.reactor.count", "1");

        Config config = new Config();
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        HazelcastInstance node1 = Hazelcast.newHazelcastInstance(config);
        // HazelcastInstance node2 = Hazelcast.newHazelcastInstance();

        Table table = node1.getTable("sometable");

        System.out.println("Filling hashtable");
        for(int k=0;k<hashtableSize;k++){
            table.set((""+k).getBytes(), "fooo".getBytes());
        }
        System.out.println("Filling hashtable:Done");

        byte[] key = "foo".getBytes();
        byte[] value = "5353453245345345325345252535353535345".getBytes();

        long startMs = System.currentTimeMillis();
        Pipeline pipeline = table.newPipeline();
        pipeline.set(key, value);
        for (int round = 0; round < rounds; round++) {
            for (int l = 0; l < pipelineSize; l++) {
                pipeline.get(key);
            }
            pipeline.execute();
            pipeline.reset();

            if (round % 10000 == 0) {
                System.out.println("at round:" + round);
            }
        }

        System.out.println("Done");

        long duration = System.currentTimeMillis() - startMs;
        System.out.println("Throughput: " + (rounds * pipelineSize * 1000.0f / duration) + " op/s");
        node1.shutdown();
    }
}
