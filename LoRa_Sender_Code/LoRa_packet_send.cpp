#include <cox.h>
#include <string.h>

static void Read(void* args);
static void SendTask(void* args);
static void eventOnRxStarted(void *arg);
static void eventOnRxDone(void *arg);
static void eventOnTxDone(void *arg, bool success);

Timer SendTimer;
struct wpan_frame *frame = NULL;
uint8_t sendData[40];
uint8_t a;
void setup(void)
{
  Serial.begin(115200);
  Serial.listen();
  SX1276.begin();
  SX1276.enableCrc(true);
  SX1276.setDataRate(10);
  SX1276.setCodingRate(3);
  SX1276.setTxPower(14);
  SX1276.setBandwidth(0);
  SX1276.setPreambleLength(12);
  SX1276.setChannel(917100000);
  SX1276.onRxStarted(eventOnRxStarted,NULL);
  SX1276.onRxDone(eventOnRxDone,NULL);
  SX1276.onTxDone(eventOnTxDone,NULL);
  SX1276.wakeup();

  //SendTimer.onFired(SendTask,NULL);
  SendTimer.onFired(Read,NULL);
  SendTimer.startPeriodic(300);
}
static void Read(void* args){
  a = Serial.available();
  uint8_t readLen = 0;
  char readData[40];
  Serial.input(readData,40,'\0');
  for(int i=0; i<strlen(readData); i++)
    sendData[i] = readData[i];
  SendTask(NULL);
}

static void SendTask(void* args){
  if(frame != NULL)
    return;

  frame = (struct wpan_frame *)dynamicMalloc(sizeof(struct wpan_frame)+40);

  if(!frame){
    return;
  }
  //har data[] = "4,421,62.062,-3.143,51.160,F";
  //printf("Send : %s\n", readData);

  frame->buf = (uint8_t *)(frame+1);
  frame->len = 40;
  sendData[0] = a;

  for(int i=0; i< strlen(sendData); i++)
      frame->buf[i] = sendData[i];

  SX1276.transmit(frame);
}

static void eventOnTxDone(void *arg, bool success){
  dynamicFree(frame);
  frame = NULL;
}

static void eventOnRxDone(void *arg){}
static void eventOnRxStarted(void *arg){}
