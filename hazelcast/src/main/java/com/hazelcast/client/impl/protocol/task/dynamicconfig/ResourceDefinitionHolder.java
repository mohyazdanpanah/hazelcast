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

package com.hazelcast.client.impl.protocol.task.dynamicconfig;

import com.hazelcast.internal.namespace.ResourceDefinition;

import java.util.Arrays;
import java.util.Objects;

public class ResourceDefinitionHolder {
    private final String id;
    private final int resourceType;
    private final byte[] payload;

    public ResourceDefinitionHolder(ResourceDefinition resourceDefinition) {
        id = resourceDefinition.id();
        resourceType = resourceDefinition.type().ordinal();
        payload = resourceDefinition.payload();
    }

    public ResourceDefinitionHolder(String id, int resourceType, byte[] payload) {
        this.id = id;
        this.resourceType = resourceType;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public int getResourceType() {
        return resourceType;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + Arrays.hashCode(payload);
        result = (prime * result) + Objects.hash(id, resourceType);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ResourceDefinitionHolder other = (ResourceDefinitionHolder) obj;
        return Objects.equals(id, other.id) && Arrays.equals(payload, other.payload) && (resourceType == other.resourceType);
    }
}
