#-*-coding:utf-8-*-
import serial
import time
import linecache
import sys

num_lines=0
dev_name='/dev/ttyUSB'
i=0
while 1:
    try:
        loraSender = serial.Serial(dev_name+str(i), 115200, timeout=3)
        print("LoRa Connect Success")
        break
    except serial.SerialException as e:
       if (i != 4):
            i+=1
            print(e)
            time.sleep(1)
       else:
            i=0
while 1:
    print('start flush')
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