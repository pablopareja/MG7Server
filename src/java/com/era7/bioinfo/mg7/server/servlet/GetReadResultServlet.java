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
import com.era7.bioinfo.mg7.MG7Manager;
import com.era7.bioinfo.mg7.nodes.HitNode;
import com.era7.bioinfo.mg7.nodes.HspNode;
import com.era7.bioinfo.mg7.nodes.ReadResultNode;
import com.era7.bioinfo.mg7.relationships.HitHspRel;
import com.era7.bioinfo.mg7.relationships.HitReadResultRel;
import com.era7.bioinfo.servletlibraryneo4j.servlet.BasicServletNeo4j;
import com.era7.lib.bioinfoxml.Hit;
import com.era7.lib.bioinfoxml.Hsp;
import com.era7.lib.bioinfoxml.mg7.ReadResultXML;
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
public class GetReadResultServlet extends BasicServletNeo4j {

    public static final int MAX_READ_RESULTS = 10000;

    @Override
    protected Response processRequest(Request rqst, BasicSession bs, Bio4jManager mn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (rqst.getMethod().equals(RequestList.GET_READ_RESULT_REQUEST)) {

            ReadResultXML readResultXML = new ReadResultXML(rqst.getParameters().getChild(ReadResultXML.TAG_NAME));

            MG7Manager manager = new MG7Manager(CommonData.getMG7DataXML().getResultsDBFolder(), false, true);

            ReadResultNode readResultsNode = new ReadResultNode(manager.getReadResultReadIdIndex().get(ReadResultNode.READ_RESULT_READ_ID_INDEX, readResultXML.getReadId()).getSingle());
            
            readResultXML.detach();

            Iterator<Relationship> hitIterator = readResultsNode.getNode().getRelationships(new HitReadResultRel(null), Direction.INCOMING).iterator();
            while (hitIterator.hasNext()) {

                HitNode hitNode = new HitNode(hitIterator.next().getStartNode());
                Hit hitXML = new Hit();
                hitXML.setHitDef(hitNode.getHitDef());
                hitXML.setHitLen(hitNode.getHitLength());
                hitXML.setHitNum(hitNode.getHitNum());
                hitXML.setGiId(hitNode.getGiId());
                hitXML.setHitAccession(hitNode.getHitAccession());

                Iterator<Relationship> hspIterator = hitNode.getNode().getRelationships(new HitHspRel(null), Direction.OUTGOING).iterator();
                while (hspIterator.hasNext()) {
                    
                    HspNode hspNode = new HspNode(hspIterator.next().getEndNode());
                    Hsp hspXML = new Hsp();
                    hspXML.setIdentity(String.valueOf(hspNode.getIdentity()));
                    hspXML.setEvalue(hspNode.getEvalue());
                    hspXML.setAlignLen(String.valueOf(hspNode.getAlignmentLength()));
                    hspXML.setMidline(hspNode.getMidline());
                    hspXML.setQSeq(hspNode.getQuerySequence());
                    hspXML.setHSeq(hspNode.getHitSequence());
                    hspXML.setNum(""+hspNode.getHspNum());
                    
                    //hspXML.setBitScore();
                    
                    hitXML.addHsp(hspXML);
                }

                readResultXML.addHit(hitXML);
            }
            
            readResultXML.setReadId(readResultsNode.getReadId());
            readResultXML.setQueryLength(readResultsNode.getQueryLength());


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
