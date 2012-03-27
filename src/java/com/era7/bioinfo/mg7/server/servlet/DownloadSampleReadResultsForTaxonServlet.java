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
import com.era7.bioinfo.bio4jmodel.util.NodeRetriever;
import com.era7.bioinfo.mg7.MG7Manager;
import com.era7.bioinfo.mg7.nodes.ReadResultNode;
import com.era7.bioinfo.mg7.nodes.SampleNode;
import com.era7.bioinfo.mg7.relationships.ReadResultLCANcbiTaxonRel;
import com.era7.bioinfo.mg7.relationships.ReadResultNcbiTaxonRel;
import com.era7.bioinfo.mg7.relationships.ReadResultSampleRel;
import com.era7.bioinfo.mg7.server.CommonData;
import com.era7.bioinfo.mg7.server.RequestList;
import com.era7.lib.bioinfo.bioinfoutil.fasta.FastaUtil;
import com.era7.lib.bioinfoxml.mg7.ReadResultXML;
import com.era7.lib.bioinfoxml.mg7.SampleXML;
import com.era7.lib.bioinfoxml.ncbi.NCBITaxonomyNodeXML;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import java.io.OutputStream;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class DownloadSampleReadResultsForTaxonServlet extends HttpServlet {

    @Override
    public void init() {
    }

    @Override
    public void doPost(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {
        //System.out.println("doPost !");
        servletLogic(request, response);

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {
        //System.out.println("doGet !");
        servletLogic(request, response);


    }

    protected void servletLogic(HttpServletRequest request, HttpServletResponse response)
            throws javax.servlet.ServletException, java.io.IOException {


        OutputStream out = response.getOutputStream();

        String temp = request.getParameter(Request.TAG_NAME);

        try {

            Request myReq = new Request(temp);

            String method = myReq.getMethod();
            System.out.println("myReq = " + myReq);


            if (method.equals(RequestList.DOWNLOAD_SAMPLE_READ_RESULTS_FOR_TAXON_REQUEST)) {

                String fileName = myReq.getParameters().getChildText("file_name");
                String format = myReq.getParameters().getChildText("format");
                String mode = myReq.getParameters().getChildText("mode");
                Boolean includeDescendants = Boolean.parseBoolean(myReq.getParameters().getChildText("include_descendants"));

                System.out.println(myReq.getMethod());

                SampleXML sampleXML = new SampleXML(myReq.getParameters().getChild(SampleXML.TAG_NAME));
                NCBITaxonomyNodeXML taxonXML = new NCBITaxonomyNodeXML(myReq.getParameters().getChild(NCBITaxonomyNodeXML.TAG_NAME));

                //Map<String,String> configuration = EmbeddedGraphDatabase.loadConfigurations(CommonData.getMetagenomicaDataXML().getMLMConfigProps());
                MG7Manager manager = new MG7Manager(CommonData.getMG7DataXML().getResultsDBFolder(),false,true);
                NodeRetriever nodeRetriever = new NodeRetriever(manager);

                StringBuilder stBuilder = new StringBuilder();

                if (format.equals("xml")) {
                    stBuilder.append(("<read_results sample=\"" + sampleXML.getSampleName()
                            + "\" taxon_id=\"" + taxonXML.getTaxId() + "\" >\n"));
                }

                ReadResultSampleRel readResultSampleRel = new ReadResultSampleRel(null);

                Iterator<Node> sampleIterator = manager.getSampleNameIndex().get(SampleNode.SAMPLE_NAME_INDEX, sampleXML.getSampleName()).iterator();

                if (sampleIterator.hasNext()) {

                    SampleNode sampleNode = new SampleNode(sampleIterator.next());

                    NCBITaxonNode taxonNode = nodeRetriever.getNCBITaxonByTaxId("" + taxonXML.getTaxId());

                    getReadsAndWriteThemToFile(taxonNode, format, stBuilder, includeDescendants, sampleNode, readResultSampleRel, mode);

                    System.out.println("writing response");
                    response.setContentType("application/x-download");
                    
                    String sufix = "NoDesc";
                    if(includeDescendants){
                        sufix = "WithDesc";
                    }
                    
                    if (format.equals("xml")) {
                        stBuilder.append("</read_results>");
                        response.setHeader("Content-Disposition", "attachment; filename=" + sampleXML.getSampleName() + "_" + fileName + sufix + ".xml");
                    }else{
                        response.setHeader("Content-Disposition", "attachment; filename=" + sampleXML.getSampleName() + "_" + fileName + sufix +  ".fasta");
                    }                    
                    
                    byte[] byteArray = stBuilder.toString().getBytes();
                    
                    System.out.println("byteArray length: " + byteArray.length);
                    
                    out.write(byteArray);
                    
                    response.setContentLength(byteArray.length);                    

                    System.out.println("doneee!!");

                    System.out.println("end reached!");

                    //out.flush();



                } else {
                    Response resp = new Response();
                    resp.setError("There is no such method");
                    out.write(resp.toString().getBytes());

                }

            }
        } catch (Exception e) {
            Logger.getLogger(DownloadSampleReadResultsForTaxonServlet.class.getName()).log(Level.SEVERE, null, e);
        }
        
        out.flush();
        out.close();

    }

    protected void getReadsAndWriteThemToFile(NCBITaxonNode taxonNode,
            String format,
            StringBuilder stBuilder,
            Boolean includeDescendants,
            SampleNode sampleNode,
            ReadResultSampleRel readResultSampleRel,
            String mode) {
        
        //System.out.println("stBuilder length antes: " + stBuilder.toString().length());

        Iterator<Relationship> relIterator = null;
        
        if(mode.equals("lca")){
            relIterator = taxonNode.getNode().getRelationships(new ReadResultLCANcbiTaxonRel(null), Direction.INCOMING).iterator();
        }else if(mode.equals("direct")){
            relIterator = taxonNode.getNode().getRelationships(new ReadResultNcbiTaxonRel(null), Direction.INCOMING).iterator();
        }         

        while (relIterator.hasNext()) {

            ReadResultNode readResultsNode = new ReadResultNode(relIterator.next().getStartNode());
            SampleNode tempSampleNode = new SampleNode(readResultsNode.getNode().getSingleRelationship(readResultSampleRel, Direction.OUTGOING).getEndNode());
            

            if (sampleNode.getName().equals(tempSampleNode.getName())) {
                
                if (format.equals("xml")) {
                    
                    ReadResultXML readResultXML = new ReadResultXML();
                    readResultXML.setReadId(readResultsNode.getReadId());
                    readResultXML.setQueryLength(readResultsNode.getQueryLength());
                    readResultXML.setSequence(readResultsNode.getReadSequence());                   

                    stBuilder.append((readResultXML.toString() + "\n"));

                } else if (format.equals("multifasta")) {

                    stBuilder.append((">" + sampleNode.getName() + "|" +
                            readResultsNode.getReadId() + "|" +
                            taxonNode.getTaxId() + "|" +
                            readResultsNode.getReadSequence().length() + "|" +
                            
                            "\n" + 
                            FastaUtil.formatSequenceWithFastaFormat(readResultsNode.getReadSequence(), 70) 
                            ));

                }
            }

        }
        
        if(includeDescendants){
            for (NCBITaxonNode child : taxonNode.getChildren()) {
                getReadsAndWriteThemToFile(child, format, stBuilder, includeDescendants, sampleNode, readResultSampleRel, mode);
            }            
        }
        
    }
}
