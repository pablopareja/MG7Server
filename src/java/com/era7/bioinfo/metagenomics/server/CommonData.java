/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.era7.bioinfo.metagenomics.server;

import com.era7.lib.bioinfoxml.MetagenomicsDataXML;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class CommonData {

    public static final String COMMON_DATA_FILE = "metagenomicaData.xml";    
    
    
    private static MetagenomicsDataXML metagenomicaDataXML = null;

    public static MetagenomicsDataXML getMetagenomicaDataXML() throws FileNotFoundException, IOException, Exception {
        if (metagenomicaDataXML == null) {
            
            File file = new File(COMMON_DATA_FILE);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stBuilder.append((line + "\n"));
            }
            reader.close();

            metagenomicaDataXML = new MetagenomicsDataXML(stBuilder.toString());
        }

        return metagenomicaDataXML;
    }
}
