#!/usr/bin/env python
# coding=utf-8

from transformers import AutoTokenizer, AutoModelForCausalLM
from transformers import pipeline
import time
import sys
import os
import argparse

# os.environ["CUDA_VISIBLE_DEVICES"] = "0"


def loadModel(hparams):
    start = time.time()
    print(f'Loading the model, it will take about 10 seconds, please wait a moment.')
    print("Model path:"+hparams.model_dir)
    tokenizer = AutoTokenizer.from_pretrained(hparams.model_dir)
    model = AutoModelForCausalLM.from_pretrained(hparams.model_dir)
    generator = pipeline(task="text-generation", model=model, tokenizer=tokenizer, device=0)
    print("Model loading completed:", time.time() - start)
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


# if __name__ == '__main__':
def generate(hparams):
    # Init arguments.
    first_head = 6
    second_head = 4
    each_iter_num = int(hparams.file_num / (first_head+second_head))

    # Generate.
    # time_start = time.time()
    generator, tokenizer = loadModel(hparams)
    prefixList = ['public class MyJVMTest']
    generationTextPipe( hparams, generator, tokenizer, each_iter_num, prefixList=prefixList, 
                        num_return_sequences = first_head, count = 1)
    prefixList = ['import java']
    generationTextPipe( hparams, generator, tokenizer, each_iter_num, prefixList=prefixList, 
                        num_return_sequences = second_head, count = first_head*each_iter_num+1)
    # time_end = time.time()
    # time_sum = time_end - time_start
    # print("Total time spent(s):",time_sum)

    # Check grammar.
    print("Start to check grammar.")
    task = os.popen("cd ./generate_tools/GrammaCheck && javac checkGenerate.java && java checkGenerate "+hparams.save_dir)
    task.close()

    # Insert into database.
    args_content = " ".join([hparams.save_dir, "function", "Table_Function"])
    cmd = "cd ./generate_tools/SaveIntoDatabase && javac SaveFunction.java && java SaveFunction " + args_content
    print("cmd:"+cmd)
    print("Start to insert.")
    task = os.popen(cmd)
    task.close()
