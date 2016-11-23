#-*-coding:utf-8-*-

import requests
import json
url = 'https://fcm.googleapis.com/fcm/send'
body = {
"data":{
   "MT_ID":"4"  
   # "latitude":"37.279264",
   # "longitude":"127.010297",
   # "state":"danger"
},
"notification":{
  "title":"Capston Design LAN",
  "body":"캡스톤디자인!",
  "content_available": "true"
},
 #"to":"czO3GUc5QBo:APA91bH-vsrtz3to6c8m1sJ6bsWWMy-Xgujq2MffmCsh9jBsxz6CPqdiMYK2o-sOzJUq_w0KfH9WXzyZOiACr-vCnZ8KjOYNvPDA_nz9CT0Q3W6uDPH_vdsM9qGRotLVtw6jsNFv8kNT"
  "to":"/topics/lan"	
}

headers = {"Content-Type":"application/json",
        "Authorization": "key=AIzaSyAOafcWpQp60zgxqmse2ZssOkWiozjbgyE"}
result=requests.post(url, data=json.dumps(body), headers=headers)
print(result)
