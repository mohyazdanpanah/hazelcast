/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.dynamicconfig;

import com.hazelcast.config.UserCodeNamespaceAwareConfig;
import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spi.impl.operationservice.Operation;

/**
 * Supplier that creates {@link RemoveDynamicConfigOperation}s for a given config.
 */
public class RemoveDynamicConfigOperationSupplier extends DynamicConfigOperationSupplier {
    public RemoveDynamicConfigOperationSupplier(ClusterService clusterService, IdentifiedDataSerializable config) {
        super(clusterService, config);
    }

    @Override
    public Operation get() {
        return new RemoveDynamicConfigOperation(config, clusterService.getMemberListVersion(),
                config instanceof UserCodeNamespaceAwareConfig
                        ? ((UserCodeNamespaceAwareConfig) config).getUserCodeNamespace() : null);
    }
}
