from step1_generator import generate
from step2_init import generate_testcase
from step3_harness import harness
from step4_mutation import mutation

import os
import sys
sys.path.append(os.getcwd().replace('\\','/'))
from src.studyMysql.db_operation_base import DataBaseHandle
from src.utils.config import Hparams

if __name__ == '__main__':
    print("---Start---")
    
    # Init arguments.
    hparams = Hparams().parser.parse_args()
    
    # Clean up the database.
    if hparams.clean_database:
        print("Clean up the database.")
        handler = DataBaseHandle()
        table_list = [  "Table_Function", "Table_Testcase", "Table_Result", "Table_Suspicious_Result", 
                        "Table_javac_Result", "Table_javac_Suspicious_Result"]
        for table in table_list:
            handler.deleteAll("truncate table "+table)

    # Generate testcases by finetuned model.
    generate(hparams)

    # Assemble and save into database.
    generate_testcase()
    
    # Apply differential test on origin testcases.
    harness(0)
    for i in range(hparams.max_iterator):
        # Mutate the testcases.
        mutation()
        # Apply differential test on mutated testcase(interesting).
        harness(1)

    # Apply differential test on mutated testcase(not interesting).
    harness(2)
    
    print("---End---")
