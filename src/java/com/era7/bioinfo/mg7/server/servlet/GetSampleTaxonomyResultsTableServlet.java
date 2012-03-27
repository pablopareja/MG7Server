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
import com.era7.bioinfo.mg7.MG7Manager;
import com.era7.bioinfo.mg7.nodes.SampleNode;
import com.era7.bioinfo.mg7.relationships.TaxonFrequencyResultsRel;
import com.era7.bioinfo.mg7.server.CommonData;
import com.era7.bioinfo.mg7.server.RequestList;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.mg7.SampleXML;
import com.era7.lib.bioinfoxml.ncbi.NCBITaxonomyNodeXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
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

            String mode = rqst.getParameters().getChildText("mode");
            SampleXML sampleXML = new SampleXML(rqst.getParameters().getChild(SampleXML.TAG_NAME));

            MG7Manager manager = new MG7Manager(CommonData.getMG7DataXML().getResultsDBFolder(),false,true);

            SampleNode sampleNode = new SampleNode(manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleXML.getSampleName()).getSingle());

            Iterator<Relationship> relIterator = sampleNode.getNode().getRelationships(new TaxonFrequencyResultsRel(null), Direction.INCOMING).iterator();

            while (relIterator.hasNext()) {

                TaxonFrequencyResultsRel taxonFreqRel = new TaxonFrequencyResultsRel(relIterator.next());
                NCBITaxonNode currentNode = new NCBITaxonNode(taxonFreqRel.getRelationship().getStartNode());

                if (mode.equals("direct")) {
                    if (taxonFreqRel.getAbsoluteValue() > 0) {
                        
                        //-----------creating ncbi taxon xml node------------------
                        NCBITaxonomyNodeXML nodeXML = new NCBITaxonomyNodeXML();
                        nodeXML.setTaxId(Integer.parseInt(currentNode.getTaxId()));
                        nodeXML.setScientificName(currentNode.getScientificName());
                        nodeXML.setAbsoluteFrequency(taxonFreqRel.getAbsoluteValue());
                        nodeXML.setAccumulatedAbsoluteFrequency(taxonFreqRel.getAccumulatedAbsoluteValue());

                        response.addChild(nodeXML);
                    }
                } else if (mode.equals("lca")) {
                    if (taxonFreqRel.getLCAAbsoluteValue() > 0) {
                        
                        //-----------creating ncbi taxon xml node------------------
                        NCBITaxonomyNodeXML nodeXML = new NCBITaxonomyNodeXML();
                        nodeXML.setTaxId(Integer.parseInt(currentNode.getTaxId()));
                        nodeXML.setScientificName(currentNode.getScientificName());
                        nodeXML.setAbsoluteFrequency(taxonFreqRel.getLCAAbsoluteValue());
                        nodeXML.setAccumulatedAbsoluteFrequency(taxonFreqRel.getLCAAccumulatedAbsoluteValue());
                        
                        response.addChild(nodeXML);
                    }
                }
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
