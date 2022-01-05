/*
 * Copyright 2021 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.sql.impl.aggregate.function;

import com.hazelcast.jet.sql.impl.schema.HazelcastTableFunction;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.sql.SqlCallBinding;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlOperandMetadata;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.type.SqlTypeName;

import static com.hazelcast.jet.sql.impl.aggregate.WindowUtils.getOrderingColumnType;

public abstract class HazelcastWindowTableFunction extends HazelcastTableFunction {

    protected HazelcastWindowTableFunction(SqlKind kind, SqlOperandMetadata operandMetadata, int orderingColumnIndex) {
        super(kind.name(), operandMetadata, returnTypeInference(orderingColumnIndex));
    }

    /**
     * @param orderingColumnParameterIndex The index of the DESCRIPTOR
     *                                     parameter pointing to the ordering column.
     */
    private static SqlReturnTypeInference returnTypeInference(int orderingColumnParameterIndex) {
        return binding -> {
            SqlCallBinding callBinding = ((SqlCallBinding) binding);
            // We'll use the original row type and append two columns: window start and end. These
            // columns have the same type as the time column referenced by the descriptor.
            SqlTypeName orderingColumnType = getOrderingColumnType(callBinding, orderingColumnParameterIndex).getSqlTypeName();

            RelDataType inputRowType = callBinding.getValidator().getValidatedNodeType(callBinding.operand(0));
            return binding.getTypeFactory().builder()
                    .kind(inputRowType.getStructKind())
                    .addAll(inputRowType.getFieldList())
                    // todo [viliam] test null timestamp
                    .add("window_start", orderingColumnType)
                    .add("window_end", orderingColumnType)
                    .build();
        };
    }

    @Override
    public boolean argumentMustBeScalar(int ordinal) {
        return ordinal != 0;
    }
}
