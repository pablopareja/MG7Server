/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.metagenomics.server.servlet;

import com.era7.bioinfo.bio4jmodel.util.Bio4jManager;
import com.era7.bioinfo.metagenomics.server.CommonData;
import com.era7.bioinfo.metagenomics.server.RequestList;
import com.era7.bioinfo.metagenomics.MetagenomicsManager;
import com.era7.bioinfo.metagenomics.nodes.ReadResultNode;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.metagenomics.ReadResultXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetReadResultServlet extends BasicServletNeo4j {

    public static final int MAX_READ_RESULTS = 10000;

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (rqst.getMethod().equals(RequestList.GET_READ_RESULT_REQUEST)) {
            
            ReadResultXML readResultXML = new ReadResultXML(rqst.getParameters().getChild(ReadResultXML.TAG_NAME));

            MetagenomicsManager manager = new MetagenomicsManager(CommonData.getMetagenomicaDataXML().getResultsDBFolder(),null);
            
            ReadResultNode readResultsNode = new ReadResultNode(manager.getReadResultReadIdIndex().get(ReadResultNode.READ_RESULT_READ_ID_INDEX, readResultXML.getReadId()).getSingle());

            readResultXML.detach();
            
            readResultXML.setReadId(readResultsNode.getReadId());
            readResultXML.setIdentity(readResultsNode.getIdentity());
            readResultXML.setQueryLength(readResultsNode.getQueryLength());
            readResultXML.setEvalue(readResultsNode.getEvalue());
            readResultXML.setGiId(readResultsNode.getGiId());
            readResultXML.setHitLength(readResultsNode.getHitLength());
            readResultXML.setAlignmentLength(readResultsNode.getAlignmentLength());
            readResultXML.setMidline(readResultsNode.getMidline());
            readResultXML.setQuerySequence(readResultsNode.getQuerySequence());
            readResultXML.setHitSequence(readResultsNode.getHitSequence());

            response.addChild(readResultXML);

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
            dbFolder = CommonData.getMetagenomicaDataXML().getResultsDBFolder();
        } catch (Exception ex) {
            Logger.getLogger(GetReadResultServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dbFolder;
    }

    @Override
    protected void initServlet() {
    }
}
