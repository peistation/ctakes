@ECHO OFF
set MAT_PKG_HOME=C:\Users\m054274\Downloads\MIST_1_3_RC5_for_SHARP\MIST_1_3_RC5_for_SHARP\src\MAT

%MAT_PKG_HOME%\bin\MATEngine.cmd --task "SHARP Deidentification" --workflow Demo --steps transform --input_dir "R:\Dept\projects\Text\temporalRelations\%4\%2" --input_file_type mat-json --output_dir "R:\Dept\projects\Text\temporalRelations\transform\%2" --output_file_type raw --offset_map R:\Dept\projects\Text\temporalRelations\maps\%1\mapfile%3 --dont_transform date,age

