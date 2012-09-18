
ECHO This needs to be generalized before checked in

cd /d c:

ECHO Replace "your_pipeline_installation_home" in the following lines
cd  \your_pipeline_installation_home\chunker
cd  /your_pipeline_installation_home/chunker


pause
java  -Xms1024M -Xmx1300M    -cp "../core/lib/opennlp-tools-1.4.0.jar;../core/lib/trove.jar;../core/lib/maxent-2.5.0.jar;%CLASSPATH%"  opennlp.tools.chunker.ChunkerME  "/EraseME/chunk/corpus.opennlp.chunks"  "/EraseME/chunk/corpus.chunk.model.bin.gz"
pause
 



