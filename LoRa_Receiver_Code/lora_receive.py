#-*- coding: utf-8-*-
import spidev
import time
import subprocess
import json
from datetime import datetime
import random
import csv
import numpy as np
import socket
import sys
import requests
from operator import eq
import serial

ser = serial.Serial('/dev/ttyUSB0',115200,timeout=None)

try:     

    longitude = [127.010757,127.011422,127.009609,127.011562,127.010113,127.009019]
    latitude = [37.277318,37.278018,37.277993,37.279094,37.279103,37.279077]
    #여기에 로라로 들어오는 값을 파싱하여 저장한 csv파일을 계속 읽고 db에올림.
    while 1 :
	ser.flushInput()
	ser.flushOutput()
	rdata=ser.readline().rstrip("\n")
        mdata=rdata.split(',')
        
	try:
	  print(mdata)
	
	except IndexError:
	    continue
	try:	
            json_data = {"MT_ID":4, "Longitude":longitude[int(mdata[1])-1],"Latitude":latitude[int(mdata[1])-1], "Node_ID":int(mdata[1]), "Node_X": int(mdata[3]), "Node_Y": int(mdata[4]), "Node_Z":int(mdata[5]), "Route":int(mdata[2]), "Variation":int(mdata[6]),"Time":datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
	
	except IndexError:
	    continue
	except ValueError:
	    continue
	#json_data를 표준 출력 로그 파일에 추가
	encode_json_data=json.dumps(json_data)
	print encode_json_data
	f=open("/home/pi/dbTest/logTest.log","a")
	f.write(encode_json_data)
        f.write("\n")
        f.close()
	time.sleep(1)
	print("\n")
	
except KeyboardInterrupt :
    pass
