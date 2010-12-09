# set up lexica directories
mkdir lexiconGeniaSTMP lexiconGeniaSTM lexiconGeniaSTP lexiconGeniaST
# run each trainer
java -Xmx1g -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTrainST.xml
rm -f resources/genia1-400-ST.mod.ftr
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTrainSTM.xml
rm -f resources/genia1-400-STM.mod.ftr
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTrainSTP.xml
rm -f resources/genia1-400-STP.mod.ftr
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTrainSTMP.xml
rm -f resources/genia1-400-STMP.mod.ftr

# run the tests!
mkdir data/outputGeniaSTMPSTMP
java -cp bin:lib/hppc-0.3.1.jar:resources clear.engine.DepPredict -t data/genia401-500.dep -o data/outputGeniaSTMPSTMP/doc0.dep -c resources/config_en_genia1-400-STMP.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTMTrainSTMP.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTPTrainSTMP.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTTrainSTMP.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTMTrainSTM.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTPTrainSTP.xml
java -Xmx1g  -cp "resources:lib/hppc-0.3.1.jar:lib/liblinear-1.51.jar:../POS tagger/resources:lib/args4j-2.0.12.jar:bin:$UIMA_HOME/lib/uima-core.jar:$UIMA_HOME/lib/uima-cpe.jar:$UIMA_HOME/lib/uima-tools.jar:$UIMA_HOME/lib/uima-document-annotations.jar:../chunker/bin:../clinical documents pipeline/bin:../context dependent tokenizer/bin:../core/bin:../dictionary lookup/bin:../document preprocessor/bin:../LVG/bin:../NE contexts/bin:../POS tagger/bin:../core/lib/log4j-1.2.8.jar:../core/lib/jdom.jar:../core/lib/lucene-core-3.0.2.jar:../core/lib/opennlp-tools-1.4.0.jar:../core/lib/maxent-2.5.0.jar:../core/lib/OpenAI_FSM.jar:../core/lib/trove.jar:../LVG/lib/lvg2008dist.jar:../document preprocessor/lib/xercesImpl.jar:../document preprocessor/lib/xml-apis.jar:../document preprocessor/lib/xmlParserAPIs.jar:../chunker/resources:../clinical documents pipeline/resources:../context dependent tokenizer/resources:../core/resources:../dictionary lookup/resources:../document preprocessor/resources:../LVG/resources:../NE contexts/resources:../POS tagger/resources" CpeTests desc/test/GeniaTestSTTrainST.xml

# grab results
data/eval.py data/genia401-500.dep data/outputGeniaSTMPSTMP/doc0.dep > genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTMPSTM/doc0.dep >> genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTMPSTP/doc0.dep >> genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTMPST/doc0.dep >> genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTMSTM/doc0.dep >> genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTPSTP/doc0.dep >> genia401-500-results.txt
data/eval.py data/genia401-500.dep data/outputGeniaSTST/doc0.dep >> genia401-500-results.txt
