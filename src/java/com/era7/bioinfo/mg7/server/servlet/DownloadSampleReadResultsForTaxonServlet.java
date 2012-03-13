/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.mg7.server.servlet;

import com.era7.bioinfo.bio4jmodel.nodes.ncbi.NCBITaxonNode;
import com.era7.bioinfo.bio4jmodel.util.NodeRetriever;
import com.era7.bioinfo.mg7.MG7Manager;
import com.era7.bioinfo.mg7.nodes.ReadResultNode;
import com.era7.bioinfo.mg7.nodes.SampleNode;
import com.era7.bioinfo.mg7.relationships.ReadResultNcbiTaxonRel;
import com.era7.bioinfo.mg7.relationships.ReadResultSampleRel;
import com.era7.bioinfo.mg7.server.CommonData;
import com.era7.bioinfo.mg7.server.RequestList;
import com.era7.lib.bioinfo.bioinfoutil.fasta.FastaUtil;
import com.era7.lib.bioinfoxml.metagenomics.ReadResultXML;
import com.era7.lib.bioinfoxml.metagenomics.SampleXML;
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


            if (method.equals(RequestList.DOWNLOAD_SAMPLE_READ_RESULTS_FOR_TAXON_REQUEST)) {

                String fileName = myReq.getParameters().getChildText("file_name");
                String format = myReq.getParameters().getChildText("format");
                Boolean includeDescendants = Boolean.parseBoolean(myReq.getParameters().getChildText("include_descendants"));

                System.out.println(myReq.getMethod());

                SampleXML sampleXML = new SampleXML(myReq.getParameters().getChild(SampleXML.TAG_NAME));
                NCBITaxonomyNodeXML taxonXML = new NCBITaxonomyNodeXML(myReq.getParameters().getChild(NCBITaxonomyNodeXML.TAG_NAME));

                //Map<String,String> configuration = EmbeddedGraphDatabase.loadConfigurations(CommonData.getMetagenomicaDataXML().getMLMConfigProps());
                MG7Manager manager = new MG7Manager(CommonData.getMetagenomicaDataXML().getResultsDBFolder());
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

                    getReadsAndWriteThemToFile(taxonNode, format, stBuilder, includeDescendants, sampleNode, readResultSampleRel);

                    if (format.equals("xml")) {
                        stBuilder.append("</read_results>");
                    }

                    System.out.println("writing response");

                    response.setContentType("application/x-download");
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xml");

                    //System.out.println("goSlim = " + goSlim);

                    byte[] byteArray = stBuilder.toString().getBytes();

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

        out.close();

    }

    protected void getReadsAndWriteThemToFile(NCBITaxonNode taxonNode,
            String format,
            StringBuilder stBuilder,
            Boolean includeDescendants,
            SampleNode sampleNode,
            ReadResultSampleRel readResultSampleRel) {

        Iterator<Relationship> relIterator = taxonNode.getNode().getRelationships(new ReadResultNcbiTaxonRel(null), Direction.INCOMING).iterator();

        while (relIterator.hasNext()) {

            ReadResultNode readResultsNode = new ReadResultNode(relIterator.next().getStartNode());
            SampleNode tempSampleNode = new SampleNode(readResultsNode.getNode().getSingleRelationship(readResultSampleRel, Direction.OUTGOING).getEndNode());

            if (sampleNode.getName().equals(tempSampleNode.getName())) {

                if (format.equals("xml")) {
                    ReadResultXML readResultXML = new ReadResultXML();
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

                    stBuilder.append((readResultXML.toString() + "\n"));

                } else if (format.equals("multifasta")) {

                    stBuilder.append((">" + readResultsNode.getReadId() + "\n" + FastaUtil.formatSequenceWithFastaFormat(readResultsNode.getHitSequence(), 60) + "\n"));

                }
            }

        }
        
        if(includeDescendants){
            for (NCBITaxonNode child : taxonNode.getChildren()) {
                getReadsAndWriteThemToFile(child, format, stBuilder, includeDescendants, sampleNode, readResultSampleRel);
            }            
        }
    }
}
