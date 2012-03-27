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

import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.mg7.server.CommonData;
import com.era7.bioinfo.mg7.server.RequestList;
import com.era7.bioinfo.mg7.nodes.SampleNode;
import com.era7.bioinfo.mg7.relationships.SamplesRel;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.mg7.SampleXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import com.era7.lib.era7xmlapi.model.XMLElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Element;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetSamplesServlet extends BasicServletNeo4j{

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager manager, HttpServletRequest hsr) throws Throwable {
        
        
        Response response = new Response();
        
        System.out.println(rqst.getMethod());
        
        if(rqst.getMethod().equals(RequestList.GET_SAMPLES_REQUEST)){
            
            XMLElement samplesElement = new XMLElement(new Element("samples"));
            
            Iterator<Relationship> iteratorSamples = manager.getReferenceNode().getRelationships(new SamplesRel(null), Direction.OUTGOING).iterator();
            while(iteratorSamples.hasNext()){
                SampleNode sampleNode = new SampleNode(iteratorSamples.next().getEndNode());
                SampleXML sampleXML = new SampleXML();
                sampleXML.setSampleName(sampleNode.getName());
                samplesElement.addChild(sampleXML);
            }
            
            response.addChild(samplesElement);
            
            response.setStatus(Response.SUCCESSFUL_RESPONSE);
            
            
        }else{
            response.setError("There is no such method");
        }
        
        return response;
        
    }

    @Override
    protected void logSuccessfulOperation(Request rqst, Response rspns, Bio4jManager bm, BasicSession bs) {    }
    @Override
    protected void logErrorResponseOperation(Request rqst, Response rspns, Bio4jManager bm, BasicSession bs) { }
    @Override
    protected void logErrorExceptionOperation(Request rqst, Response rspns, Throwable thrwbl, Bio4jManager bm) {  }
    @Override
    protected void noSession(Request rqst) {  }
    @Override
    protected boolean checkPermissions(ArrayList<?> al, Request rqst) {return true;  }
    @Override
    protected boolean defineCheckSessionFlag() {  return false; }
    @Override
    protected boolean defineCheckPermissionsFlag() {    return false; }
    @Override
    protected boolean defineLoggableFlag() {    return false;   }
    @Override
    protected boolean defineLoggableErrorsFlag() {  return false;  }
    @Override
    protected boolean defineUtf8CharacterEncodingRequest() {    return false;   }
    @Override
    protected String defineNeo4jDatabaseFolder() {  
        String dbFolder = "";
        try {
            dbFolder = CommonData.getMG7DataXML().getResultsDBFolder();
        } catch (Exception ex) {
            Logger.getLogger(GetSamplesServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbFolder;   
    }
    @Override
    protected void initServlet() {}
    
}
