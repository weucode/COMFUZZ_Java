from multiprocessing import Process, Queue
from time import sleep

from step3_1_diff import harness
import step3_2_coverage


def run_diff(choice: int):
    print("Task 1 started")
    harness(choice)
    print("Task 1 finished")

def run_coverage():
    print("Task 2 started")
    sleep(10)
    step3_2_coverage.main()
    print("Task 2 finished")

def diff_and_coverage(choice: int):
    process1 = Process(target=run_diff, args=(choice, ))
    process2 = Process(target=run_coverage)

    # Start processes. 
    process1.start()
    process2.start()

    # Wait for process1 to complete.
    process1.join()
    # If process1 finish, terminate process2.
    process2.terminate()
    process2.join()

    print("All tasks finished")

if __name__ == '__main__':
    diff_and_coverage(0)
