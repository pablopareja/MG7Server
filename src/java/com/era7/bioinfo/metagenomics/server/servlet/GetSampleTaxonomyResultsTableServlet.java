/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.metagenomics.server.servlet;

import com.era7.bioinfo.bio4jmodel.nodes.ncbi.NCBITaxonNode;
import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.bio4jmodel.util.NodeRetriever;
import com.era7.bioinfo.metagenomics.MetagenomicsManager;
import com.era7.bioinfo.metagenomics.nodes.SampleNode;
import com.era7.bioinfo.metagenomics.relationships.TaxonFrequencyResultsRel;
import com.era7.bioinfo.metagenomics.server.CommonData;
import com.era7.bioinfo.metagenomics.server.RequestList;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.graphml.NodeXML;
import com.era7.lib.bioinfoxml.metagenomics.SampleXML;
import com.era7.lib.bioinfoxml.ncbi.NCBITaxonomyNodeXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetSampleTaxonomyResultsTableServlet extends BasicServletNeo4j {

    public static long EDGE_COUNTER = 0;

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (rqst.getMethod().equals(RequestList.GET_SAMPLE_TAXONOMY_RESULTS_TABLE_REQUEST)) {

            System.out.println(rqst.getMethod());

            SampleXML sampleXML = new SampleXML(rqst.getParameters().getChild(SampleXML.TAG_NAME));

            MetagenomicsManager manager = new MetagenomicsManager(CommonData.DB_FOLDER);

            SampleNode sampleNode = new SampleNode(manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleXML.getSampleName()).getSingle());
            
            Iterator<Relationship> relIterator = sampleNode.getNode().getRelationships(new TaxonFrequencyResultsRel(null), Direction.INCOMING).iterator();
            
            while (relIterator.hasNext()) {

                TaxonFrequencyResultsRel taxonFreqRel = new TaxonFrequencyResultsRel(relIterator.next());
                NCBITaxonNode currentNode = new NCBITaxonNode(taxonFreqRel.getRelationship().getStartNode());
                
                System.out.println("Table: " + currentNode.getScientificName());

                //-----------creating ncbi taxon xml node------------------
                NCBITaxonomyNodeXML nodeXML = new NCBITaxonomyNodeXML();
                nodeXML.setTaxId(Integer.parseInt(currentNode.getTaxId()));
                nodeXML.setScientificName(currentNode.getScientificName());
                nodeXML.setAbsoluteFrequency(taxonFreqRel.getAbsoluteValue());
                nodeXML.setAccumulatedAbsoluteFrequency(taxonFreqRel.getAccumulatedAbsoluteValue());

                response.addChild(nodeXML);
            }

            response.setStatus(Response.SUCCESSFUL_RESPONSE);

        } else {
            response.setError("There is no such method");
        }

        return response;

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