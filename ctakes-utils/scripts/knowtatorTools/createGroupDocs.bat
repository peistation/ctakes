@ECHO ON
set MAT_PKG_HOME=C:\Users\m054274\Downloads\MIST_1_3_RC5_for_SHARP\MIST_1_3_RC5_for_SHARP\src\MAT
%MAT_PKG_HOME%\bin\MATEngine.cmd --task "SHARP Deidentification" --workflow Demo --steps nominate --input_dir "r:\Dept\projects\Text\temporalRelations\%3\%1" --input_file_type mat-json --output_dir "R:\Dept\projects\Text\temporalRelations\%3\%2" --output_file_type mat-json --replacer "clear -> clear" --cache_scope "pt_name,batch;org_name,batch;doc_name,batch;address,batch;other_id,batch" 


