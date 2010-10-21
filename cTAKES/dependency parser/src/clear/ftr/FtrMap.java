/**
* Copyright (c) 2009, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.ftr;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import clear.dep.DepNode;
import clear.util.DSUtil;
import clear.util.IOUtil;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

/**
 * This class contains mappings between indices and features.
 * @author Jinho D. Choi
 * <b>Last update:</b> 6/30/2010
 */
public class FtrMap
{
        /** Contains an ordered list of class labels */
        protected ArrayList<String>            a_label;
        /** Takes "class label" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_label;
        /** Takes "form" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_form;
        /** Takes "lemma" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_lemma;
        /** Takes "pos-tag" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_pos;
        /** Takes "dependency label" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_deprel;
        /** Takes "pos_lemma" from a token as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_pos_lemma_1gram;
        /** Takes "pos1_pos2" from token1 and token2 as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_pos_pos_2gram;
        /** Takes "pos1_lemma2" from token1 and token2 as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_pos_lemma_2gram;
        /** Takes "lemma1_pos2" from token1 and token2 as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_lemma_pos_2gram;
        /** Takes "lemma1_lemma2" from token1 and token2 as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_lemma_lemma_2gram;
        /** Takes "pos1_pos2_pos3" from token[1..3] as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_pos_pos_pos_3gram;
        /** Takes "punctuation" as a key and its index as a value */
        protected ObjectIntOpenHashMap<String> m_punctuation;
        /** Takes "pos_pos" as a key and the rule a a value */
        private ObjectIntOpenHashMap<String> m_pos_pos_dep_rule;
        
        private ObjectIntOpenHashMap<String> m_chunk_pos;
        
        /** Size of {@link FtrMap#m_label} */
        public int n_label;
        /** Size of {@link FtrMap#m_form} + 1  */
        public int n_form;
        /** Size of {@link FtrMap#m_lemma} + 1 */
        public int n_lemma;
        /** Size of {@link FtrMap#m_pos} + 1 */
        public int n_pos;
        /** Size of {@link FtrMap#m_deprel} + 1 */
        public int n_deprel;
        /** Size of {@link FtrMap#m_pos_lemma_1gram} + 1 */
        public int n_pos_lemma_1gram;
        /** Size of {@link FtrMap#m_pos_pos_2gram} + 1 */
        public int n_pos_pos_2gram;
        /** Size of {@link FtrMap#m_pos_lemma_2gram} + 1 */
        public int n_pos_lemma_2gram;
        /** Size of {@link FtrMap#m_lemma_pos_2gram} + 1 */
        public int n_lemma_pos_2gram;
        /** Size of {@link FtrMap#m_lemma_lemma_2gram} + 1 */
        public int n_lemma_lemma_2gram;
        /** Size of {@link FtrMap#m_pos_pos_pos_3gram} + 1 */
        public int n_pos_pos_pos_3gram;
        /** Size of {@link FtrMap#m_punctuation} + 1 */
        public int n_punctuation;
        
        public int n_chunk_pos;
        
        /** Initializes the empty map (values to be added later). */
        public FtrMap()
        {
                m_label             = new ObjectIntOpenHashMap<String>();
                m_form              = new ObjectIntOpenHashMap<String>();
                m_lemma             = new ObjectIntOpenHashMap<String>();
                m_pos               = new ObjectIntOpenHashMap<String>();
                m_deprel            = new ObjectIntOpenHashMap<String>();
                m_pos_lemma_1gram   = new ObjectIntOpenHashMap<String>();
                m_pos_pos_2gram     = new ObjectIntOpenHashMap<String>();
                m_pos_lemma_2gram   = new ObjectIntOpenHashMap<String>();
                m_lemma_pos_2gram   = new ObjectIntOpenHashMap<String>();
                m_lemma_lemma_2gram = new ObjectIntOpenHashMap<String>();
                m_pos_pos_pos_3gram = new ObjectIntOpenHashMap<String>();
                m_punctuation       = new ObjectIntOpenHashMap<String>();
                m_pos_pos_dep_rule  = new ObjectIntOpenHashMap<String>();
                m_chunk_pos         = new ObjectIntOpenHashMap<String>();
        }
        
        /** Returns HashMap of pre-determined punctuation. */
        protected ObjectIntOpenHashMap<String> getPunctuation()
        {
                ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
                
                map.put("." , 1);       map.put("..", 1);       map.put("..." , 1);     map.put("....", 1);     map.put(".-", 1);
                map.put("!" , 1);       map.put("!!", 1);       map.put("!!!" , 1);     map.put("!!!!", 1);     
                map.put("?" , 1);       map.put(".?", 1);       map.put("!?"  , 1);     map.put("?!"  , 1);     map.put("??", 1);       map.put("???", 1);
                map.put("," , 1);       map.put(":" , 1);       map.put(";"   , 1);     map.put("/"   , 1);
                map.put("-" , 1);       map.put("--", 1);       map.put("---" , 1);
                map.put("`" , 1);       map.put("'" , 1);       map.put("\""  , 1);
                map.put("``", 1);       map.put("''", 1);       map.put("\"\"", 1);
                map.put("(" , 1);       map.put(")" , 1);
                map.put("{" , 1);       map.put("}" , 1);
                map.put("[" , 1);       map.put("]" , 1);
                
                return map;
        }
        
        /**
         * Calls {@link FtrMap#load(String)}.
         * @param lexiconDir path to the directory containing lexicon files
         */
        public FtrMap(String lexiconDir)
        {
                load(lexiconDir);
        }

        /**
         * Loads configuration files from the directory.
         * @param lexiconDir path to the directory containing lexicon files
         */
        public void load(String lexiconDir)
        {
                HashSet<String> fileset = DSUtil.toHashSet(new File(lexiconDir).list());
                
                if (fileset.contains(FtrLib.FILE_LABEL))
                {
                        a_label = IOUtil.getArrayList(lexiconDir + File.separator + FtrLib.FILE_LABEL);
                        m_label = DSUtil.toHashMap(a_label, 1);
                        n_label = m_label.size();                                                               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_FORM))
                {
                        m_form = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_FORM, 1);
                        n_form = m_form.size() + 1;                                                             System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_LEMMA))
                {
                        m_lemma = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_LEMMA, 1);
                        n_lemma = m_lemma.size() + 1;                                                   System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS))
                {
                        m_pos = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_POS, 1);
                        n_pos = m_pos.size() + 1;                                                               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_DEPREL))
                {
                        m_deprel = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_DEPREL, 1);
                        n_deprel = m_deprel.size() + 1;                                                 System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS_LEMMA_1GRAM))
                {
                        m_pos_lemma_1gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_POS_LEMMA_1GRAM, 1);
                        n_pos_lemma_1gram = m_pos_lemma_1gram.size() + 1;               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS_POS_2GRAM))
                {
                        m_pos_pos_2gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_POS_POS_2GRAM, 1);
                        n_pos_pos_2gram = m_pos_pos_2gram.size() + 1;                   System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS_LEMMA_2GRAM))
                {
                        m_pos_lemma_2gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_POS_LEMMA_2GRAM, 1);
                        n_pos_lemma_2gram = m_pos_lemma_2gram.size() + 1;               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_LEMMA_POS_2GRAM))
                {       
                        m_lemma_pos_2gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_LEMMA_POS_2GRAM, 1);
                        n_lemma_pos_2gram = m_lemma_pos_2gram.size() + 1;               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_LEMMA_LEMMA_2GRAM))
                {
                        m_lemma_lemma_2gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_LEMMA_LEMMA_2GRAM, 1);
                        n_lemma_lemma_2gram = m_lemma_lemma_2gram.size() + 1;   System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS_POS_POS_3GRAM))
                {
                        m_pos_pos_pos_3gram = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_POS_POS_POS_3GRAM, 1);
                        n_pos_pos_pos_3gram = m_pos_pos_pos_3gram.size() + 1;   System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_PUNCTUATION))
                {
                        m_punctuation = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_PUNCTUATION, 1);
                        n_punctuation = m_punctuation.size() + 1;                               System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_POS_POS_DEP_RULE))
                {
                        m_pos_pos_dep_rule = IOUtil.getTStringIntHashMap(lexiconDir + File.separator + FtrLib.FILE_POS_POS_DEP_RULE, FtrLib.RULE_DELIM);
                        System.out.print(".");
                }
                
                if (fileset.contains(FtrLib.FILE_CHUNK_POS))
                {
                    m_chunk_pos = IOUtil.getHashMap(lexiconDir + File.separator + FtrLib.FILE_CHUNK_POS, 1);
                    n_chunk_pos = m_chunk_pos.size() + 1;                               System.out.print(".");
                }
        }
        
        /** Saves all tags to <code>lexiconDir</code>. */
        public void save(String lexiconDir)
        {
                if (!m_label.isEmpty())
                {       IOUtil.printFile(m_label            , lexiconDir + File.separator + FtrLib.FILE_LABEL);                         System.out.print(".");}
                if (!m_form.isEmpty())
                {       IOUtil.printFile(m_form             , lexiconDir + File.separator + FtrLib.FILE_FORM);                          System.out.print(".");}
                if (!m_lemma.isEmpty())
                {       IOUtil.printFile(m_lemma            , lexiconDir + File.separator + FtrLib.FILE_LEMMA);                         System.out.print(".");}
                if (!m_pos.isEmpty())
                {       IOUtil.printFile(m_pos              , lexiconDir + File.separator + FtrLib.FILE_POS);                           System.out.print(".");}
                if (!m_deprel.isEmpty())
                {       IOUtil.printFile(m_deprel           , lexiconDir + File.separator + FtrLib.FILE_DEPREL);                        System.out.print(".");}
                if (!m_pos_lemma_1gram.isEmpty())
                {       IOUtil.printFile(m_pos_lemma_1gram  , lexiconDir + File.separator + FtrLib.FILE_POS_LEMMA_1GRAM);       System.out.print(".");}
                if (!m_pos_pos_2gram.isEmpty())
                {       IOUtil.printFile(m_pos_pos_2gram    , lexiconDir + File.separator + FtrLib.FILE_POS_POS_2GRAM);         System.out.print(".");}
                if (!m_pos_lemma_2gram.isEmpty())
                {       IOUtil.printFile(m_pos_lemma_2gram  , lexiconDir + File.separator + FtrLib.FILE_POS_LEMMA_2GRAM);       System.out.print(".");}
                if (!m_lemma_pos_2gram.isEmpty())
                {       IOUtil.printFile(m_lemma_pos_2gram  , lexiconDir + File.separator + FtrLib.FILE_LEMMA_POS_2GRAM);       System.out.print(".");}
                if (!m_lemma_lemma_2gram.isEmpty())                     
                {       IOUtil.printFile(m_lemma_lemma_2gram, lexiconDir + File.separator + FtrLib.FILE_LEMMA_LEMMA_2GRAM);     System.out.print(".");}
                if (!m_pos_pos_pos_3gram.isEmpty())
                {       IOUtil.printFile(m_pos_pos_pos_3gram, lexiconDir + File.separator + FtrLib.FILE_POS_POS_POS_3GRAM);     System.out.print(".");}
                if (!m_punctuation.isEmpty())
                {       IOUtil.printFile(m_punctuation      , lexiconDir + File.separator + FtrLib.FILE_PUNCTUATION);           System.out.print(".");}
                if (!m_pos_pos_dep_rule.isEmpty())
                {       saveRules(m_pos_pos_dep_rule        , lexiconDir + File.separator + FtrLib.FILE_POS_POS_DEP_RULE,1);System.out.print(".");}
                if (!m_chunk_pos.isEmpty())
                {       IOUtil.printFile(m_chunk_pos        , lexiconDir + File.separator + FtrLib.FILE_CHUNK_POS);           System.out.print(".");}
                
        }
        
        /** Saves rules in <code>map</code> to <code>filename</code> using <code>cutoff</code>. */
        private void saveRules(ObjectIntOpenHashMap<String> map, String filename, int cutoff)
        {
                PrintStream fout = IOUtil.createPrintFileStream(filename);
                
                for (ObjectCursor<String> key : map.keySet())
                {
                        int value = map.get(key.value);
                        
                        if (Math.abs(value) > cutoff)
                        {
                                if      (value < 0)     fout.println(key.value + FtrLib.RULE_DELIM +"-1");
                                else if (value > 0)     fout.println(key.value + FtrLib.RULE_DELIM + "1");
                        }
                }
                
                fout.close();
        }
        
        /** Adds the class label. */
        public void addLabel(String label)
        {
                m_label.put(label, 1);
        }
                
        /** Adds the form. */
        public void addForm(String form)
        {
                m_form.put(form, 1);
        }
        
        /** Adds the lemma. */
        public void addLemma(String lemma)
        {
                m_lemma.put(lemma, 1);
        }
        
        /** Adds the part-of-speech tag. */
        public void addPos(String pos)
        {
                m_pos.put(pos, 1);
        }
        
        /** Adds the dependency label. */
        public void addDeprel(String deprel)
        {
                m_deprel.put(deprel, 1);
        }
        
        /** Adds <code>posLemma</code>. */
        public void addPosLemma1gram(String posLemma)
        {
                m_pos_lemma_1gram.put(posLemma, 1);
        }
        
        /** Adds <code>posPos</code>. */
        public void addPosPos2gram(String posPos)
        {
                m_pos_pos_2gram.put(posPos, 1);
        }
        
        /** Adds <code>posLemma</code>. */
        public void addPosLemma2gram(String posLemma)
        {
                m_pos_lemma_2gram.put(posLemma, 1);
        }
        
        /** Adds <code>lemmaPos</code>. */
        public void addLemmaPos2gram(String lemmaPos)
        {
                m_lemma_pos_2gram.put(lemmaPos, 1);
        }
        
        /** Adds <code>lemmaLemma</code>. */
        public void addLemmaLemma2gram(String lemmaLemma)
        {
                m_lemma_lemma_2gram.put(lemmaLemma, 1);
        }
        
        /** Adds <code>posPosPos</code>. */
        public void addPosPosPos3gram(String posPosPos)
        {
                m_pos_pos_pos_3gram.put(posPosPos, 1);
        }
        
        /** Adds the punctuation. */
        public void addPunctuation(String punctuation)
        {
                m_punctuation.put(punctuation, 1);
        }
        
        public void addChunkPos(String pos)
        {
            m_chunk_pos.put(pos, 1);
        }
        
        /**
         * Adds "pos_pos" dependency rule.
         * @param dir -1: <code>left</code> is the head of <code>right</code>, 1: <code>right</code> is the head of <code>left</code>
         */
        public void addPosPosDepRule(DepNode left, DepNode right, int dir)
        {
                String rule = left.pos + FtrLib.TAG_DELIM + right.pos;
                int   value = m_pos_pos_dep_rule.get(rule) + dir;
                m_pos_pos_dep_rule.put(rule, value);
        }
        
        /** @return the class label corresponding to the index. */
        public String indexToLabel(int index)
        {
                return a_label.get(index);
        }
        
        /**
         * Returns the index of the class label.
         * If the class label does not exist, returns -1.
         */
        public int labelToIndex(String label)
        {
                return m_label.get(label) - 1;
        }
        
        /**
         * Returns the index of the form.
         * If the form does not exist, returns 0.
         */
        public int formToIndex(String form)
        {
                return m_form.get(form);
        }
        
        /**
         * Returns the index of the lemma.
         * If the lemma does not exist, returns 0.
         */
        public int lemmaToIndex(String lemma)
        {
                return m_lemma.get(lemma);
        }
        
        /**
         * Returns the index of the pos-tag.
         * If the pos-tag does not exist, return 0.
         */
        public int posToIndex(String pos)
        {
                return m_pos.get(pos);
        }
        
        /**
         * Returns the index of the dependency label.
         * If the dependency label does not exist, returns 0.
         */
        public int deprelToIndex(String deprel)
        {
                return m_deprel.get(deprel);
        }
        
        /**
         * Returns the index of <code>posLemma</code>.
         * If <code>posLemma</code> does not exist, returns 0.
         */
        public int posLemma1gramToIndex(String posLemma)
        {
                return m_pos_lemma_1gram.get(posLemma);
        }
        
        /**
         * Returns the index of <code>posPos</code>.
         * If <code>posPos</code> does not exist, returns 0.
         */
        public int posPos2gramToIndex(String posPos)
        {
                return m_pos_pos_2gram.get(posPos);
        }

        /**
         * Returns the index of <code>posLemma</code>.
         * If <code>posLemma</code> does not exist, returns 0.
         */
        public int posLemma2gramToIndex(String posLemma)
        {
                return m_pos_lemma_2gram.get(posLemma);
        }
        
        /**
         * Returns the index of <code>lemmaPos</code>.
         * If <code>lemmaPos</code> does not exist, returns 0.
         */
        public int lemmaPos2gramToIndex(String lemmaPos)
        {
                return m_lemma_pos_2gram.get(lemmaPos);
        }
                
        /**
         * Returns the index of <code>lemmaLemma</code>.
         * If <code>lemmaLemma</code> does not exist, returns 0.
         */
        public int lemmaLemma2gramToIndex(String lemmaLemma)
        {
                return m_lemma_lemma_2gram.get(lemmaLemma);
        }
        
        /**
         * Returns the index of <code>posPosPos</code>.
         * If <code>posPosPos</code> does not exist, returns 0.
         */
        public int posPosPos3gramToIndex(String posPosPos)
        {
                return m_pos_pos_pos_3gram.get(posPosPos);
        }
        
        /**
         * Returns the index of the punctuation.
         * If the punctuation does not exist, returns 0.
         */
        public int punctuationToIndex(String punctuation)
        {
                return m_punctuation.get(punctuation);
        }
        
        /**
         * Returns the rule of <code>posPos</code>.
         * -1: left is the head of right, 0: no relation, 1: right is the head of left
         */
        public int getPosPosDepRule(String posPos)
        {
                return m_pos_pos_dep_rule.get(posPos);
        }
        
        public int chunkPosToIndex(String pos)
        {
            return m_chunk_pos.get(pos);
        }
        
        /** @return the frequency of <code>key</code> in <map>. */
        protected int getFreq(ObjectIntOpenHashMap<String> map, String key)
        {
                return (map.containsKey(key)) ? map.get(key) + 1 : 1;
        }
}
