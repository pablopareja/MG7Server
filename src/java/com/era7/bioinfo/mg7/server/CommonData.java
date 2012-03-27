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
package com.era7.bioinfo.mg7.server;

import com.era7.lib.bioinfoxml.mg7.MG7DataXML;
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

    public static final String COMMON_DATA_FILE = "mg7Data.xml";    
    
    
    private static MG7DataXML mg7DataXML = null;

    public static MG7DataXML getMG7DataXML() throws FileNotFoundException, IOException, Exception {
        if (mg7DataXML == null) {
            
            File file = new File(COMMON_DATA_FILE);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stBuilder.append((line + "\n"));
            }
            reader.close();

            mg7DataXML = new MG7DataXML(stBuilder.toString());
        }

        return mg7DataXML;
    }
}
