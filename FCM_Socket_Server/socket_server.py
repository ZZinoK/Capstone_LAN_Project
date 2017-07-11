#-*-coding:utf-8-*-
'''
    Simple socket server using threads
'''
import socket
import sys
import requests
import json
from operator import eq 
def sendAlertToFCM(msgType):
    if eq(msgType, 'alert'):
      body_content='산사태가 감지되었습니다.\n 토폴로지를 확인하세요'
      topicType="/topics/admin"
    else:
      body_content='팔달산 근처에서 산사태가 감지되었습니다. 대피하세요!'
      topicType="/topics/lan"
    body = {
    "data":{
      "MT_ID":"4"
       # "latitude":"37.279264",
       # "longitude":"127.010297",
       # "state":"danger"
    },
    "notification":{
      "title":"Capston Design LAN",
      "body":body_content,
      #"body":"캡스톤디자인 LAN",
      "content_available": "true"
    },
   #"to":"czO3GUc5QBo:APA91bH-vsrtz3to6c8m1sJ6bsWWMy-Xgujq2MffmCsh9jBsxz6CPqdiMYK2o-sOzJUq_w0KfH9WXzyZOiACr-vCnZ8KjOYNvPDA_nz9CT0Q3W6uDPH_vdsM9qGRotLVtw6jsNFv8kNT"
    "to":topicType    
    }
    result=requests.post(url, data=json.dumps(body), headers=headers)
    
HOST = '172.20.10.2' #'172.30.1.29' #'192.168.0.96'   # Symbolic name, meaning all available interfaces
PORT = 8888 # Arbitrary non-privileged port
MT_ID = 0
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print 'Socket created'
 
#Bind socket to local host and port
try:
    s.bind((HOST, PORT))
except socket.error as msg:
    print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
    sys.exit()
     
print 'Socket bind complete'
 
#Start listening on socket
s.listen(10)
print 'Socket now listening'
url = 'https://fcm.googleapis.com/fcm/send'

headers = {"Content-Type":"application/json",
        "Authorization": "key=AIzaSyAOafcWpQp60zgxqmse2ZssOkWiozjbgyE"}
#now keep talking with the client
while 1:
    #wait to accept a connection - blocking call
  conn, addr = s.accept()
  data = conn.recv(1024);
  print 'Connected with ' + addr[0] + ':' + str(addr[1])
  print 'received data from android -> ' + data

  if eq(data, 'send'):
    sendAlertToFCM(data)
    print('send push FCM Server!')
    #s.send("Success")
  elif eq(data, 'alert'):
    sendAlertToFCM(data)
  else:
    print('Wrong meassage from '+ addr[0]) 
s.close()







