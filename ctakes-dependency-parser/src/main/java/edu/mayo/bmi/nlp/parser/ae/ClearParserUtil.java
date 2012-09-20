/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.mayo.bmi.nlp.parser.ae;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import clear.decode.AbstractMultiDecoder;
import clear.decode.OneVsAllDecoder;
import clear.ftr.map.DepFtrMap;
import clear.ftr.map.SRLFtrMap;
import clear.ftr.xml.DepFtrXml;
import clear.ftr.xml.SRLFtrXml;
import clear.parse.AbstractDepParser;
import clear.parse.AbstractParser;
import clear.parse.AbstractSRLParser;
import clear.parse.SRLParser;
import clear.parse.ShiftEagerParser;
import clear.parse.ShiftPopParser;
import clear.reader.AbstractReader;

/**
 * <br>
 * Copyright (c) 2012, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * This class was mostly written by Jinho Choi and will hopefully be folded back into the Clear
 * Parser code. See the following issue on the Clear Parser project page:
 * 
 * http://code.google.com/p/clearparser/issues/detail?id=2
 * 
 */

public class ClearParserUtil {

  static protected final String ENTRY_PARSER = "parser";

  static protected final String ENTRY_LEXICA = "lexica";

  static protected final String ENTRY_MODEL = "model";

  static protected final String ENTRY_FEATURE = "feature";

  public static AbstractDepParser createParser(InputStream inputStream, String algorithmName)
      throws IOException {
    ZipInputStream zin = new ZipInputStream(inputStream);
    ZipEntry zEntry;

    DepFtrXml xml = null;
    DepFtrMap map = null;
    AbstractMultiDecoder decoder = null;

    while ((zEntry = zin.getNextEntry()) != null) {
      if (zEntry.getName().equals(ENTRY_FEATURE)) {
        System.out.println("- loading feature template");

        BufferedReader reader = new BufferedReader(new InputStreamReader(zin));
        StringBuilder build = new StringBuilder();
        String string;

        while ((string = reader.readLine()) != null) {
          build.append(string);
          build.append("\n");
        }

        xml = new DepFtrXml(new ByteArrayInputStream(build.toString().getBytes()));
      }

      if (zEntry.getName().equals(ENTRY_LEXICA)) {
        System.out.println("- loading lexica");
        map = new DepFtrMap(xml);
        map.load(new BufferedReader(new InputStreamReader(zin)));
      } else if (zEntry.getName().equals(ENTRY_MODEL)) {
        System.out.println("- loading model");
        decoder = new OneVsAllDecoder(new BufferedReader(new InputStreamReader(zin)));
      }
    }

    if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_EAGER))
      return new ShiftEagerParser(AbstractParser.FLAG_PREDICT, xml, map, decoder);
    else if (algorithmName.equals(AbstractDepParser.ALG_SHIFT_POP))
      return new ShiftPopParser(AbstractParser.FLAG_PREDICT, xml, map, decoder);
    else
      return null;
  }

  public static AbstractSRLParser createSRLParser(InputStream inputStream) throws IOException {
    // Most of the code taken from ClearParser class AbstractCommon.java method getLabeler()
    final String s_language = AbstractReader.LANG_EN;

    ZipInputStream zin = new ZipInputStream(inputStream);
    ZipEntry zEntry;
    String entry;
    SRLFtrXml xml = null;
    SRLFtrMap[] map = new SRLFtrMap[2];
    OneVsAllDecoder[] decoder = new OneVsAllDecoder[2];

    while ((zEntry = zin.getNextEntry()) != null) {
      if (zEntry.getName().equals(ENTRY_FEATURE)) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zin));
        StringBuilder build = new StringBuilder();
        String string;

        while ((string = reader.readLine()) != null) {
          build.append(string);
          build.append("\n");
        }

        xml = new SRLFtrXml(new ByteArrayInputStream(build.toString().getBytes()));

      } else if ((entry = zEntry.getName()).startsWith(ENTRY_LEXICA)) {
        int i = Integer.parseInt(entry.substring(entry.lastIndexOf(".") + 1));
        map[i] = new SRLFtrMap(new BufferedReader(new InputStreamReader(zin)));

      } else if (zEntry.getName().startsWith(ENTRY_MODEL)) {
        int i = Integer.parseInt(entry.substring(entry.lastIndexOf(".") + 1));
        decoder[i] = new OneVsAllDecoder(new BufferedReader(new InputStreamReader(zin)));
      }

    }

    AbstractSRLParser labeler = new SRLParser(AbstractParser.FLAG_PREDICT, xml, map, decoder);
    labeler.setLanguage(s_language);

    return labeler;
  }
}
