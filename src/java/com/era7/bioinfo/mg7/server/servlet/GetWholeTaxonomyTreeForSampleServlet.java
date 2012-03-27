/*
 * Copyright (C) 2011-2012  "MG7"
 *
 * This file is part of MG7
 *
 * MG7 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.era7.bioinfo.mg7.server.servlet;

import com.era7.bioinfo.bio4jmodel.nodes.ncbi.NCBITaxonNode;
import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.bio4jmodel.util.NodeRetriever;
import com.era7.bioinfo.mg7.MG7Manager;
import com.era7.bioinfo.mg7.nodes.SampleNode;
import com.era7.bioinfo.mg7.relationships.TaxonFrequencyResultsRel;
import com.era7.bioinfo.mg7.server.CommonData;
import com.era7.bioinfo.mg7.server.RequestList;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.graphml.DataXML;
import com.era7.lib.bioinfoxml.graphml.EdgeXML;
import com.era7.lib.bioinfoxml.graphml.GraphXML;
import com.era7.lib.bioinfoxml.graphml.GraphmlXML;
import com.era7.lib.bioinfoxml.graphml.KeyXML;
import com.era7.lib.bioinfoxml.graphml.NodeXML;
import com.era7.lib.bioinfoxml.mg7.SampleXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetWholeTaxonomyTreeForSampleServlet extends BasicServletNeo4j {

    public static long EDGE_COUNTER = 0;

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (rqst.getMethod().equals(RequestList.GET_WHOLE_TAXONOMY_TREE_FOR_SAMPLE_REQUEST)) {

            System.out.println(rqst.getMethod());

            SampleXML sampleXML = new SampleXML(rqst.getParameters().getChild(SampleXML.TAG_NAME));
            String mode = rqst.getParameters().getChildText("mode");

            MG7Manager manager = new MG7Manager(CommonData.getMG7DataXML().getResultsDBFolder(),false,true);

            NodeRetriever nodeRetriever = new NodeRetriever(mn);

            SampleNode sampleNode = new SampleNode(manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleXML.getSampleName()).getSingle());

            HashMap<String, NodeXML> taxonMap = new HashMap<String, NodeXML>();

            System.out.println("getting taxon frequencies relationships...");
            Iterator<Relationship> relIterator = sampleNode.getNode().getRelationships(new TaxonFrequencyResultsRel(null), Direction.INCOMING).iterator();


            while (relIterator.hasNext()) {

                TaxonFrequencyResultsRel taxonFreqRel = new TaxonFrequencyResultsRel(relIterator.next());
                NCBITaxonNode currentNode = new NCBITaxonNode(taxonFreqRel.getRelationship().getStartNode());

                if (mode.equals("direct")) {
                    if (taxonFreqRel.getAbsoluteValue() > 0) {
                        
                        //-----------creating graphml node------------------

                        NodeXML currentNodeXML = createNodeXML(currentNode.getScientificName(),
                                currentNode.getTaxId(),
                                "" + taxonFreqRel.getAbsoluteValue(),
                                "" + taxonFreqRel.getAccumulatedAbsoluteValue());

                        taxonMap.put(currentNode.getTaxId(), currentNodeXML);
                        
                    }
                } else if (mode.equals("lca")) {
                    if (taxonFreqRel.getLCAAbsoluteValue() > 0) {
                        
                        //-----------creating graphml node------------------

                        NodeXML currentNodeXML = createNodeXML(currentNode.getScientificName(),
                                currentNode.getTaxId(),
                                "" + taxonFreqRel.getLCAAbsoluteValue(),
                                "" + taxonFreqRel.getLCAAccumulatedAbsoluteValue());

                        taxonMap.put(currentNode.getTaxId(), currentNodeXML);
                    }
                }



            }

            GraphmlXML graphmlXML = getGraphmlXML();

            GraphXML graph = new GraphXML();
            graph.setDefaultEdgeType(GraphXML.DIRECTED_EDGE_TYPE);
            graph.setId("Taxonomy graph");

            //-----first we create and add the root node (so that it gets in first place)---
            NCBITaxonNode rootNode = nodeRetriever.getNCBITaxonByTaxId("1");
            NodeXML rootNodeXML = createNodeXML(rootNode.getScientificName(),
                    rootNode.getTaxId(),
                    "-1",
                    "-1");
            graph.addNode(rootNodeXML);
            //--------------------------------------------

            graphmlXML.addGraph(graph);

            HashSet<String> nodesIncludedInGraph = new HashSet<String>();

            Set<String> taxonKeys = taxonMap.keySet();
            for (String taxonKey : taxonKeys) {
                getAncestors(nodeRetriever.getNCBITaxonByTaxId(taxonKey),
                        graph,
                        taxonMap,
                        nodesIncludedInGraph);
            }

            response.addChild(graphmlXML);

            response.setStatus(Response.SUCCESSFUL_RESPONSE);

        } else {
            response.setError("There is no such method");
        }

        return response;

    }

    private void getAncestors(NCBITaxonNode currentNode,
            GraphXML graph,
            HashMap<String, NodeXML> taxonMap,
            HashSet<String> nodesIncludedInGraph) {

        String currentNodeTaxId = currentNode.getTaxId();

        if (!nodesIncludedInGraph.contains(currentNodeTaxId)) {

            NodeXML nodeXML = taxonMap.get(currentNodeTaxId);
            if (nodeXML == null) {
                nodeXML = createNodeXML(currentNode.getScientificName(),
                        currentNodeTaxId,
                        "-1",
                        "-1");
            }

            nodesIncludedInGraph.add(currentNodeTaxId);

            NCBITaxonNode parentNode = currentNode.getParent();

            if (parentNode != null) {

                EdgeXML edgeXML = new EdgeXML();
                edgeXML.setId("" + EDGE_COUNTER++);
                edgeXML.setSource(parentNode.getTaxId());
                edgeXML.setTarget(currentNodeTaxId);
                graph.addEdge(edgeXML);

                graph.addNode(nodeXML);

                getAncestors(parentNode, graph, taxonMap, nodesIncludedInGraph);

            }

        }

    }

    private GraphmlXML getGraphmlXML() {

        GraphmlXML graphmlXML = new GraphmlXML();

        KeyXML scientificNameKey = new KeyXML();
        scientificNameKey.setAttrName(NCBITaxonNode.SCIENTIFIC_NAME_PROPERTY);
        scientificNameKey.setAttrType("String");
        scientificNameKey.setFor(KeyXML.NODE);
        scientificNameKey.setId(NCBITaxonNode.SCIENTIFIC_NAME_PROPERTY);
        graphmlXML.addKey(scientificNameKey);

        KeyXML taxIdKey = new KeyXML();
        taxIdKey.setAttrName(NCBITaxonNode.TAX_ID_PROPERTY);
        taxIdKey.setAttrType("String");
        taxIdKey.setFor(KeyXML.NODE);
        taxIdKey.setId(NCBITaxonNode.TAX_ID_PROPERTY);
        graphmlXML.addKey(taxIdKey);

        KeyXML absoluteValueKey = new KeyXML();
        absoluteValueKey.setAttrName(TaxonFrequencyResultsRel.ABSOLUTE_VALUE_PROPERTY);
        absoluteValueKey.setAttrType("String");
        absoluteValueKey.setFor(KeyXML.NODE);
        absoluteValueKey.setId(TaxonFrequencyResultsRel.ABSOLUTE_VALUE_PROPERTY);
        graphmlXML.addKey(absoluteValueKey);

        KeyXML accumulatedAbsoluteValueKey = new KeyXML();
        accumulatedAbsoluteValueKey.setAttrName(TaxonFrequencyResultsRel.ACCUMULATED_ABSOLUTE_VALUE_PROPERTY);
        accumulatedAbsoluteValueKey.setAttrType("String");
        accumulatedAbsoluteValueKey.setFor(KeyXML.NODE);
        accumulatedAbsoluteValueKey.setId(TaxonFrequencyResultsRel.ACCUMULATED_ABSOLUTE_VALUE_PROPERTY);
        graphmlXML.addKey(accumulatedAbsoluteValueKey);

        return graphmlXML;
    }

    private NodeXML createNodeXML(String scientificName,
            String taxId,
            String freqAbsolute,
            String freqAccumulated) {

        NodeXML nodeXML = new NodeXML();

        DataXML scientificNameData = new DataXML();
        scientificNameData.setKey(NCBITaxonNode.SCIENTIFIC_NAME_PROPERTY);
        scientificNameData.setText(scientificName);

        DataXML taxIdData = new DataXML();
        taxIdData.setKey(NCBITaxonNode.TAX_ID_PROPERTY);
        taxIdData.setText(taxId);

        DataXML absoluteValueData = new DataXML();
        absoluteValueData.setKey(TaxonFrequencyResultsRel.ABSOLUTE_VALUE_PROPERTY);
        absoluteValueData.setText(freqAbsolute);

        DataXML accumulatedAbsoluteValueData = new DataXML();
        accumulatedAbsoluteValueData.setKey(TaxonFrequencyResultsRel.ACCUMULATED_ABSOLUTE_VALUE_PROPERTY);
        accumulatedAbsoluteValueData.setText(freqAccumulated);

        nodeXML.addData(scientificNameData);
        nodeXML.addData(taxIdData);
        nodeXML.addData(absoluteValueData);
        nodeXML.addData(accumulatedAbsoluteValueData);

        nodeXML.setId(taxId);

        return nodeXML;
    }

    @Override
    protected void logSuccessfulOperation(Request rqst, Response rspns, Bio4jManager bm, BasicSession bs) {
    }

    @Override
    protected void logErrorResponseOperation(Request rqst, Response rspns, Bio4jManager bm, BasicSession bs) {
    }

    @Override
    protected void logErrorExceptionOperation(Request rqst, Response rspns, Throwable thrwbl, Bio4jManager bm) {
    }

    @Override
    protected void noSession(Request rqst) {
    }

    @Override
    protected boolean checkPermissions(ArrayList<?> al, Request rqst) {
        return true;
    }

    @Override
    protected boolean defineCheckSessionFlag() {
        return false;
    }

    @Override
    protected boolean defineCheckPermissionsFlag() {
        return false;
    }

    @Override
    protected boolean defineLoggableFlag() {
        return false;
    }

    @Override
    protected boolean defineLoggableErrorsFlag() {
        return false;
    }

    @Override
    protected boolean defineUtf8CharacterEncodingRequest() {
        return false;
    }

    @Override
    protected String defineNeo4jDatabaseFolder() {
        String dbFolder = "";
        try {
            dbFolder = CommonData.getMG7DataXML().getResultsDBFolder();
        } catch (Exception ex) {
            Logger.getLogger(GetReadResultServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbFolder;
    }

    @Override
    protected void initServlet() {
    }
}
