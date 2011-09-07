/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.metagenomics.server.servlet;

import com.era7.bioinfo.bio4jmodel.nodes.ncbi.NCBITaxonNode;
import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.bio4jmodel.util.NodeRetriever;
import com.era7.bioinfo.metagenomics.server.CommonData;
import com.era7.bioinfo.metagenomics.server.RequestList;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.graphml.DataXML;
import com.era7.lib.bioinfoxml.graphml.EdgeXML;
import com.era7.lib.bioinfoxml.graphml.GraphXML;
import com.era7.lib.bioinfoxml.graphml.GraphmlXML;
import com.era7.lib.bioinfoxml.graphml.KeyXML;
import com.era7.lib.bioinfoxml.graphml.NodeXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetTaxonomyTreeServlet extends BasicServletNeo4j {

    public static long EDGE_COUNTER = 0;

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (rqst.getMethod().equals(RequestList.GET_TAXONOMY_TREE_REQUEST)) {

            int maxDepth = Integer.parseInt(rqst.getParameters().getChildText("max_depth"));
            String parentTaxId = rqst.getParameters().getChildText("tax_id");
            
            NodeRetriever nodeRetriever = new NodeRetriever(mn);

            NCBITaxonNode rootTaxon = nodeRetriever.getNCBITaxonByTaxId(parentTaxId);

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

            GraphXML graph = new GraphXML();
            graph.setDefaultEdgeType(GraphXML.DIRECTED_EDGE_TYPE);
            graph.setId("Taxonomy graph");

            graphmlXML.addGraph(graph);

            NodeXML rootNodeXML = new NodeXML();

            getTaxonomySubTree(rootTaxon, rootNodeXML, graph, maxDepth);

            response.addChild(graphmlXML);

            response.setStatus(Response.SUCCESSFUL_RESPONSE);

        } else {
            response.setError("There is no such method");
        }

        return response;

    }

    private String getTaxonomySubTree(NCBITaxonNode currentNode,
            NodeXML currentNodeXML,
            GraphXML graph,
            int maxDepth) {

        String currentTaxId = currentNode.getTaxId();

        DataXML scientificNameData = new DataXML();
        scientificNameData.setKey(NCBITaxonNode.SCIENTIFIC_NAME_PROPERTY);
        scientificNameData.setText(currentNode.getScientificName());

        //System.out.println("currentNode.getScientificName() = " + currentNode.getScientificName());

        DataXML taxIdData = new DataXML();
        taxIdData.setKey(NCBITaxonNode.TAX_ID_PROPERTY);
        taxIdData.setText(currentTaxId);

        currentNodeXML.addData(scientificNameData);
        currentNodeXML.addData(taxIdData);
        currentNodeXML.setId(currentTaxId);

        graph.addNode(currentNodeXML);

        if (maxDepth > 0) {
            List<NCBITaxonNode> children = currentNode.getChildren();

            for (NCBITaxonNode childNode : children) {
                NodeXML childXML = new NodeXML();
                String tempTaxId = getTaxonomySubTree(childNode, childXML, graph, maxDepth - 1);

                EdgeXML edgeXML = new EdgeXML();
                edgeXML.setId("" + EDGE_COUNTER++);
                edgeXML.setSource(currentTaxId);
                edgeXML.setTarget(tempTaxId);

                graph.addEdge(edgeXML);
            }
        }


        return currentTaxId;

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
        return CommonData.DB_FOLDER;
    }

    @Override
    protected void initServlet() {
    }
}
