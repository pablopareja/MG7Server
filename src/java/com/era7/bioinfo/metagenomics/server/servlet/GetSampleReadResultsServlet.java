/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.metagenomics.server.servlet;

import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.metagenomics.server.CommonData;
import com.era7.bioinfo.metagenomics.server.RequestList;
import com.era7.bioinfo.metagenomics.MetagenomicsManager;
import com.era7.bioinfo.metagenomics.nodes.ReadResultsNode;
import com.era7.bioinfo.metagenomics.nodes.SampleNode;
import com.era7.bioinfo.metagenomics.relationships.ReadResultSampleRel;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.metagenomics.SampleXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetSampleReadResultsServlet  extends BasicServletNeo4j{
    
    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {
        
        Response response = new Response();
        
        if(rqst.getMethod().equals(RequestList.GET_SAMPLE_READ_RESULTS_REQUEST)){
            
            MetagenomicsManager manager = new MetagenomicsManager(CommonData.DB_FOLDER);
            String sampleName = response.asJDomElement().getChildText(SampleXML.TAG_NAME);            
            
            Iterator<Node> iterator = manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleName).iterator();
            if(iterator.hasNext()){
                
                SampleNode sampleNode = new SampleNode(iterator.next());                
                Iterator<Relationship> relsIterator = sampleNode.getNode().getRelationships(new ReadResultSampleRel(null), Direction.INCOMING).iterator();
                while(relsIterator.hasNext()){
                    ReadResultsNode readResultsNode = new ReadResultsNode(relsIterator.next().getStartNode());
                    
                }
                        
                response.setStatus(Response.SUCCESSFUL_RESPONSE);
                
            }else{
                response.setError("There is no such sample");
            }
            
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
    protected String defineNeo4jDatabaseFolder() {  return CommonData.DB_FOLDER;   }
    @Override
    protected void initServlet() {}
    
}
