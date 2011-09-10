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
import com.era7.bioinfo.metagenomics.MetagenomicsManager;
import com.era7.bioinfo.metagenomics.nodes.ReadResultsNode;
import com.era7.bioinfo.metagenomics.nodes.SampleNode;
import com.era7.bioinfo.metagenomics.relationships.ReadResultNcbiTaxonRel;
import com.era7.bioinfo.metagenomics.relationships.ReadResultSampleRel;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.metagenomics.ReadResultXML;
import com.era7.lib.bioinfoxml.metagenomics.SampleXML;
import com.era7.lib.bioinfoxml.ncbi.NCBITaxonomyNodeXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Element;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetSampleReadResultsForTaxonServlet  extends BasicServletNeo4j{
    
    public static final int MAX_READ_RESULTS = 10000;
    
    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {
        
        Response response = new Response();
        
        System.out.println(rqst.getMethod());
        
        if(rqst.getMethod().equals(RequestList.GET_SAMPLE_READ_RESULTS_FOR_TAXON_REQUEST)){             
            
            MetagenomicsManager manager = new MetagenomicsManager(CommonData.DB_FOLDER);
            NodeRetriever nodeRetriever = new NodeRetriever(mn);
            
            ReadResultSampleRel readResultSampleRel = new ReadResultSampleRel(null);
            
            NCBITaxonomyNodeXML taxonXML = new NCBITaxonomyNodeXML(rqst.getParameters().getChild(NCBITaxonomyNodeXML.TAG_NAME)); 
            SampleXML sampleXML = new SampleXML(rqst.getParameters().getChild(SampleXML.TAG_NAME));  
            
            NCBITaxonNode taxonNode = nodeRetriever.getNCBITaxonByTaxId(""+ taxonXML.getTaxId());
            
            int readResultsCounter = 0;
            
            Element readResultsElement = new Element("read_results");
            
            Iterator<Node> iterator = manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleXML.getSampleName()).iterator();
            if(iterator.hasNext()){
                
                SampleNode sampleNode = new SampleNode(iterator.next());                
                Iterator<Relationship> relIterator = taxonNode.getNode().getRelationships(new ReadResultNcbiTaxonRel(null), Direction.INCOMING).iterator();
                
                while(relIterator.hasNext() && readResultsCounter < MAX_READ_RESULTS){
                    
                    ReadResultsNode readResultsNode = new ReadResultsNode(relIterator.next().getStartNode());
                    SampleNode tempSampleNode = new SampleNode(readResultsNode.getNode().getSingleRelationship(readResultSampleRel, Direction.OUTGOING).getEndNode());
                    
                    if(sampleNode.getName().equals(tempSampleNode.getName())){
                    
                        ReadResultXML readResultXML = new ReadResultXML();
                        readResultXML.setReadId(readResultsNode.getReadId());
                        readResultXML.setIdentity(readResultsNode.getIdentity());
                        readResultXML.setQueryLength(readResultsNode.getQueryLength());
                        
                        readResultsElement.addContent(readResultXML.asJDomElement());                        
                        readResultsCounter++;
                        
                    }
                    
                }
                
                if(readResultsCounter >= MAX_READ_RESULTS){
                    readResultsElement.setAttribute("max_results_limit_reached", "true");
                }else{
                    readResultsElement.setAttribute("max_results_limit_reached", "false");
                }
                
                response.getRoot().addContent(readResultsElement);
                        
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
