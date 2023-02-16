import os
import argparse

currentPath = os.getcwd().replace('\\','/')

class Hparams:
    parser = argparse.ArgumentParser()

    parser.add_argument("--clean_database", type=bool, default=False, help="Clean the data table in the database.")
    
    parser.add_argument("--max_iterator", type=int, default=3, help="The max count of mutation.")
    
    parser.add_argument("--use_testsuits_head", type=bool, default=False, help="Whether use head extracted from testsuits.")
    parser.add_argument("--testsuits_head_num", type=int, default=5, help="The number of head.")

    parser.add_argument('--file_num', default=10, type=int,
                        help='Specify the total number of generated files.Please enter a multiple of 10!')
    parser.add_argument('--batch_size', default=8, type=int,
                        help='Batch size for generation(increase for GPUs, 16 is recommended for RTX 2080)')
    parser.add_argument('--top_k', default=0, type=int, help='Sample only from top k tokens')
    parser.add_argument('--top_p', default=0.0, type=float, help='Sample from top p prob (overrides top_k if nonzero)')
    parser.add_argument('--temperature', default=0.25, type=float, help='Temperature of the generated texts')
    parser.add_argument('--generate_prefix_list', default="['public class MyJVMTest']", type=str,
                        help='Prefix used for generation, needs to be consist with data_prefix')
    parser.add_argument('--max_length', default=1024, type=int, help='The max value of generation length.')
    parser.add_argument('--model_dir', default=currentPath.replace('workline','data')+'/model/checkpoint-400000', type=str,
                        help='Pretrained model path')
    parser.add_argument('--save_dir', default=currentPath.replace('workline','data')+'/generate', type=str,
                        help='Generate file storage path')

