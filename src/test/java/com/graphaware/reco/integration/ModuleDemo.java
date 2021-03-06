/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.integration;

import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.module.algo.generator.GraphGenerator;
import com.graphaware.module.algo.generator.Neo4jGraphGenerator;
import com.graphaware.module.algo.generator.config.BarabasiAlbertConfig;
import com.graphaware.module.algo.generator.config.BasicGeneratorConfig;
import com.graphaware.module.algo.generator.node.SocialNetworkNodeCreator;
import com.graphaware.module.algo.generator.relationship.BarabasiAlbertRelationshipGenerator;
import com.graphaware.module.algo.generator.relationship.SocialNetworkRelationshipCreator;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Arrays;

import static com.graphaware.reco.neo4j.engine.Neo4jPrecomputedEngine.RECOMMEND;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.graphdb.DynamicLabel.label;
import static org.neo4j.helpers.collection.Iterators.*;

public class ModuleDemo extends GraphAwareIntegrationTest {

    @Test
    @Ignore
    public void demoRecommendations() throws InterruptedException {
        Thread.sleep(20000);

        try (Transaction tx = getDatabase().beginTx()) {
            for (Node person : asIterable(getDatabase().findNodes(label("Person")))) {
                printRecommendations(person);
            }
            tx.success();
        }
    }

    @Override
    protected String configFile() {
        return "demo-neo4j.conf";
    }

    @Override
    protected void populateDatabase(GraphDatabaseService database) {
        GraphGenerator generator = new Neo4jGraphGenerator(database);
        generator.generateGraph(new BasicGeneratorConfig(
                new BarabasiAlbertRelationshipGenerator(new BarabasiAlbertConfig(1000, 3)),
                SocialNetworkNodeCreator.getInstance(),
                SocialNetworkRelationshipCreator.getInstance()
        ));
    }

    private void printRecommendations(Node node) {
        StringBuilder s = new StringBuilder(node.getProperty("name").toString()).append("(").append(Arrays.toString(Iterables.asArray(Label.class, node.getLabels()))).append("):");
        for (Relationship reco : node.getRelationships(RECOMMEND, OUTGOING)) {
            s.append(" ").append(reco.getEndNode().getProperty("name").toString()).append("(");
            s.append(PropertyContainerUtils.propertiesToString(reco));
            s.append("),");
        }
        System.out.println(s.toString()+" Degree: "+ node.getDegree());
    }
}
