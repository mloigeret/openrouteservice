/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.routing.phast;

import java.util.PriorityQueue;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.PathBidirRef;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.CHGraph;
import com.graphhopper.storage.CHGraphImpl.CHEdgeIteratorImpl;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.CHEdgeExplorer;
import com.graphhopper.util.Parameters;

/**
 * Calculates distances one-to-many.
 * <p>
 * 'Ref' stands for reference implementation and is using the normal
 * Java-'reference'-way.
 * <p>
 *
 * @author Peter Karich, Hendrik Leuschner
 */
public class DijkstraBidirectionRefRPHAST extends AbstractBidirAlgoRPHAST {
	public IntObjectMap<SPTEntry> bestWeightMapFrom;
	public IntObjectMap<SPTEntry> bestWeightMapOther;

	protected SPTEntry currFrom;
	protected SPTEntry currTo;
	protected SPTEntry currTarget;
	protected PathBidirRef bestPath;
	PriorityQueue<SPTEntry> openSetFrom;
	PriorityQueue<SPTEntry> openSetTo;
	PriorityQueue<SPTEntry> openSetTargets;
	IntObjectMap<SPTEntry> targetTree;

	public DijkstraBidirectionRefRPHAST(CHGraph chGraph, Weighting weighting, TraversalMode tMode) {
		super(chGraph, weighting, tMode);
		int size = Math.min(Math.max(200, graph.getNodes() / 10), 150_000);
		initCollections(size);
	}

	protected void initCollections(int size) {
		openSetFrom = new PriorityQueue<SPTEntry>(size);
		bestWeightMapFrom = new GHIntObjectHashMap<SPTEntry>(size);
		targetTree = new GHIntObjectHashMap<SPTEntry>(size);
		openSetTargets = new PriorityQueue<SPTEntry>(size);
		openSetTo = new PriorityQueue<SPTEntry>(size);
	}

	@Override
	public IntObjectMap<SPTEntry> init(int from, double weight) {
		currFrom = createSPTEntry(from, weight);
		currFrom.visited = true;
		openSetFrom.add(currFrom);

		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(from, currFrom);
		} else if (currTo != null && currTo.adjNode == from) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
		return bestWeightMapFrom;
	}

	@Override
	public void initDownwardPHAST(int to, double weight) {
		// Set the highest node from the upwards pass as the start node for the
		// downwards pass. Keep parent refs for future use in distance
		// extraction.
		currTo = bestWeightMapFrom.get(to);
		// currTo = createSPTEntry(to, weight);
		currTo.visited = true;
		openSetTo.add(currTo);
		if (!traversalMode.isEdgeBased()) {
			bestWeightMapFrom.put(to, currTo);
		} else if (currFrom != null && currFrom.adjNode == to) {
			// special case of identical start and end
			// bestPath.sptEntry = currFrom;
			// bestPath.edgeTo = currTo;
			finishedFrom = true;
			finishedTo = true;
		}
	}

	@Override
	protected double getCurrentFromWeight() {
		return currFrom.weight;
	}

	// Creates the target node set in G that induces the restricted search space
	// for RPHAST in the second PHAST phase.

	@Override
	public IntObjectMap<SPTEntry> createTargetTree(IntObjectMap<SPTEntry> targets) {
		// At the start, the targetTree and the prioQueue both equal the given
		// targets
		targetTree.putAll(targets);
		for (IntObjectCursor<SPTEntry> c : targets) {
			openSetTargets.add(c.value);
		}
		while (!openSetTargets.isEmpty()) {
			currTarget = openSetTargets.poll();
			CHEdgeIteratorImpl iter = (CHEdgeIteratorImpl) targetEdgeExplorer.setBaseNode(currTarget.adjNode);
			while (iter.next()) {
				if (!additionalEdgeFilter.accept(iter))
					continue;

				int traversalId = traversalMode.createTraversalId(iter, false);
				double tmpWeight = weighting.calcWeight(iter, false, currTarget.edge) + currTarget.weight;

				SPTEntry ee = targetTree.get(traversalId);
				if (ee == null) {
					ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
					// ee.parent = null;
					targetTree.put(traversalId, ee);
					openSetTargets.add(ee);
				} else
					continue;
			}
		}

		return targetTree;
	}

	// Create target tree from node ids
	@Override
	public IntObjectMap<SPTEntry> createTargetTree(int[] targetNodes) {
		IntObjectMap<SPTEntry> targets = new GHIntObjectHashMap<SPTEntry>(targetNodes.length);
		for (int i = 0; i < targetNodes.length; i++) {
			SPTEntry target = this.createSPTEntry(targetNodes[i], 0);
			targets.put(target.adjNode, target);
		}
		return createTargetTree(targets);
	}

	@Override
	public boolean fillEdgesFrom() {
		if (openSetFrom.isEmpty()) {
			return false;
		}
		currFrom = openSetFrom.poll();
		fillEdges(currFrom, openSetFrom, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountFrom++;

		return true;
	}

	@Override
	public boolean downwardPHAST() {
		if (openSetTo.isEmpty()) {
			return false;
		}
		currTo = openSetTo.poll();
		fillEdgesDownwards(currTo, openSetTo, bestWeightMapFrom, outEdgeExplorer, false);
		visitedCountTo++;

		return true;
	}

	// http://www.cs.princeton.edu/courses/archive/spr06/cos423/Handouts/EPP%20shortest%20path%20algorithms.pdf
	// a node from overlap may not be on the best path!
	// => when scanning an arc (v, w) in the forward search and w is scanned in
	// the reverseOrder
	// search, update extractPath = μ if df (v) + (v, w) + dr (w) < μ
	@Override
	public boolean finished() {
		if (finishedFrom) {
			return true;
		}

		return currFrom.weight + currTo.weight >= bestPath.getWeight();
	}

	void fillEdges(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue, IntObjectMap<SPTEntry> shortestWeightMap,
			CHEdgeExplorer explorer, boolean reverse) {
		CHEdgeIteratorImpl iter = (CHEdgeIteratorImpl) explorer.setBaseNode(currEdge.adjNode);

		while (iter.next()) {
			if (!additionalEdgeFilter.accept(iter)) {
				continue;
			}

			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;

			if (Double.isInfinite(tmpWeight)) {
				continue;
			}
			// Works only in node-based behaviour for now (for performance's
			// sake)
			// int traversalId = traversalMode.createTraversalId(iter, reverse);
			// SPTEntry ee = shortestWeightMap.get(traversalId);
			int traversalId = iter.getAdjNode();
			SPTEntry ee = shortestWeightMap.get(traversalId);

			// Keep track of the currently found highest CH level node
			if (graph.getLevel(additionalEdgeFilter.getHighestNode()) < graph.getLevel(iter.getAdjNode()))
				additionalEdgeFilter.setHighestNode(iter.getAdjNode());
			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(traversalId, ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} else
				continue;

		}
	}

	void fillEdgesDownwards(SPTEntry currEdge, PriorityQueue<SPTEntry> prioQueue,
			IntObjectMap<SPTEntry> shortestWeightMap, CHEdgeExplorer explorer, boolean reverse) {
		CHEdgeIteratorImpl iter = (CHEdgeIteratorImpl) explorer.setBaseNode(currEdge.adjNode);

		while (iter.next()) {
			if (!additionalEdgeFilter.accept(iter)) {
				continue;
			}

			double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.edge) + currEdge.weight;
			if (Double.isInfinite(tmpWeight))
				continue;

			// Works only in node-based behaviour for now (for performance's
			// sake)
			// int traversalId = traversalMode.createTraversalId(iter, reverse);
			// SPTEntry ee = shortestWeightMap.get(traversalId);
			int traversalId = iter.getAdjNode();
			SPTEntry ee = shortestWeightMap.get(traversalId);

			if (ee == null) {
				ee = new SPTEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight);
				ee.parent = currEdge;
				shortestWeightMap.put(traversalId, ee);
				prioQueue.add(ee);
			} else if (ee.weight > tmpWeight) {
				prioQueue.remove(ee);
				ee.edge = iter.getEdge();
				ee.weight = tmpWeight;
				ee.parent = currEdge;
				prioQueue.add(ee);
			} else if (ee.visited == false) {
				// // This is the case if the node has been assigned a weight in
				// // the upwards pass (fillEdges). We need to use it in the
				// // downwards pass to access lower level nodes, though the
				// weight
				// // does not have to be reset necessarily
				// prioQueue.remove(ee);
				//
				prioQueue.add(ee);
				ee.visited = true;
			} else
				continue;
		}
	}

	IntObjectMap<SPTEntry> getBestFromMap() {
		return bestWeightMapFrom;
	}

	void setBestOtherMap(IntObjectMap<SPTEntry> other) {
		bestWeightMapOther = other;
	}

	@Override
	public String getName() {
		return Parameters.Algorithms.DIJKSTRA_BI;
	}

}