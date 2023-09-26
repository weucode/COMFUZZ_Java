#!/usr/bin/env python
# coding=utf-8

from transformers import AutoTokenizer, AutoModelForCausalLM
from transformers import pipeline
import time
import sys
import os
import argparse
import random

import sys
sys.path.append(os.getcwd().replace('\\','/').replace('workline',''))
from src.utils.config import Hparams

# os.environ["CUDA_VISIBLE_DEVICES"] = "0"


def loadModel(hparams):
    start = time.time()
    print(f'Loading the model, it will take about 10 seconds, please wait a moment.')
    # print("Model path:"+hparams.model_dir)
    tokenizer = AutoTokenizer.from_pretrained(hparams.model_dir)
    model = AutoModelForCausalLM.from_pretrained(hparams.model_dir)
    if hparams.use_gpus:
        generator = pipeline(task="text-generation", model=model, tokenizer=tokenizer, device = 0)
    else:
        generator = pipeline(task="text-generation", model=model, tokenizer=tokenizer)
    spend_time = round(time.time() - start, 2)
    print(f"Model loading completed:{spend_time}s.")
    return generator, tokenizer

def generationTextPipe(hparams,generator,tokenizer,each_iter_num,prefixList,num_return_sequences=50,count=1):
    # Start the generation and write out the generated content.
    allFunctions = []
    save_dir = hparams.save_dir
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)
    for i in range(each_iter_num):
        allGeneration = generator(  prefixList, num_return_sequences=num_return_sequences, 
                                    max_length=hparams.max_length,
                                    pad_token_id=tokenizer.eos_token_id, temperature=hparams.temperature,
                                    k=hparams.top_k, p=hparams.top_p)
        for generationItem in allGeneration:
            for index, value in enumerate(generationItem, start = count):
                content = value['generated_text'].replace("MyJVMTest", "MyJVMTest_"+str(index))
                if "\n}\n" not in content:
                    continue
                with open(save_dir+"/MyJVMTest_"+str(index)+".java", "w") as f:
                    f.write(content)
            count += num_return_sequences

def get_model_head(testsuits_head_num):
    with open("generate_tools/model_head.txt", "r") as f:
        content = f.read()
    head_list = content.split("\n<separator>\n")
    selected_head = random.sample(head_list, testsuits_head_num)
    return selected_head

def get_proper_num(tmp_num):
    for i in [50, 40, 30, 20, 10, 5, 4, 3, 2, 1]:
        if tmp_num%i == 0:
            return (int(tmp_num/i), i)

def generate(hparams):
    # Load model.
    generator, tokenizer = loadModel(hparams)
    start = time.time()
    if hparams.use_testsuits_head:
        # Init arguments.
        selected_head = get_model_head(hparams.testsuits_head_num)
        tmp_num = int(hparams.file_num / hparams.testsuits_head_num)
        if tmp_num > 100:
            num_return_sequences, each_iter_num = get_proper_num(tmp_num)
        else:
            num_return_sequences = tmp_num
            each_iter_num = 1
        
        # Generate.
        for index, head in enumerate(selected_head):
            generationTextPipe( hparams, generator, tokenizer, each_iter_num, prefixList=[head], 
                                num_return_sequences = num_return_sequences, count = 1+index*num_return_sequences)
    else:
        # Init arguments.
        first_head = 6
        second_head = 4
        each_iter_num = int(hparams.file_num / (first_head+second_head))
        
        # Generate.
        prefixList = ['public class MyJVMTest']
        generationTextPipe( hparams, generator, tokenizer, each_iter_num, prefixList=prefixList, 
                            num_return_sequences = first_head, count = 1)
        prefixList = ['import java']
        generationTextPipe( hparams, generator, tokenizer, each_iter_num, prefixList=prefixList, 
                            num_return_sequences = second_head, count = first_head*each_iter_num+1)
    spend_time = round(time.time() - start, 2)
    print(f"Generation completed:{spend_time}s.\n")

    # Check grammar.
    print("Start to check grammar.")
    task = os.popen("cd ./generate_tools/GrammaCheck && javac checkGenerate.java && java checkGenerate "+hparams.save_dir)
    task.close()
    print("Finish checking.\n")

    # Insert into database.
    args_content = " ".join([hparams.save_dir, "function", "Table_Function"])
    cmd = "cd ./generate_tools/SaveIntoDatabase && javac SaveFunction.java && java SaveFunction " + args_content
    # print("cmd:"+cmd)
    print("Start to insert.")
    task = os.popen(cmd)
    task.close()
    print("Finish inserting.\n")


if __name__ == '__main__':
    hparams = Hparams().parser.parse_args()
    generate(hparams)
    