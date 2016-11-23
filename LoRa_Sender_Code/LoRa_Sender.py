#-*-coding:utf-8-*-
import serial
import time
import linecache
import sys
loraSender = serial.Serial('/dev/ttyUSB0', 115200, timeout=3)
num_lines = 0 #sum(1 for line in open('raw_data.txt'))
while 1:
    line = sys.stdin.readline().rstrip('\n')
    loraSender.flushInput()
    loraSender.flushOutput()

    if not line:
        print('line이 아님')
        break
    else:
        loraSender.write(line)
        print(line)
        time.sleep(0.2)