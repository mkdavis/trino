/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.sql.planner.iterative.rule;

import io.prestosql.sql.planner.Symbol;
import io.prestosql.sql.planner.plan.AssignUniqueId;
import io.prestosql.sql.planner.plan.PlanNode;

import java.util.Optional;
import java.util.Set;

import static io.prestosql.sql.planner.iterative.rule.Util.restrictChildOutputs;
import static io.prestosql.sql.planner.plan.Patterns.assignUniqueId;

public class PruneAssignUniqueIdColumns
        extends ProjectOffPushDownRule<AssignUniqueId>
{
    public PruneAssignUniqueIdColumns()
    {
        super(assignUniqueId());
    }

    @Override
    protected Optional<PlanNode> pushDownProjectOff(
            Context context,
            AssignUniqueId assignUniqueId,
            Set<Symbol> referencedOutputs)
    {
        // remove unused AssignUniqueId node
        if (!referencedOutputs.contains(assignUniqueId.getIdColumn())) {
            return Optional.of(assignUniqueId.getSource());
        }

        return restrictChildOutputs(context.getIdAllocator(), assignUniqueId, referencedOutputs);
    }
}