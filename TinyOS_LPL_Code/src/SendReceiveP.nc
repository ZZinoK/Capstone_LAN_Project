#define CC2420_HW_ACKNOWLEDGEMENTS
#include "Timer.h"
#include "Leds.h"
#include "LPLC.h"
#define Target_Node 4

module SendReceiveP
{
	provides interface AMSend as AMSendP;
	provides interface SplitControl as AMControlP;
	provides interface Packet as PacketP;
	provides interface AMPacket as AMPacketP;
	provides interface startPeriodicalRadio as SPradioP;
	provides interface PacketAcknowledgements as AckP;
	
	uses interface CC2420Config;
	uses interface Timer<TMilli> as OffTimer;
	uses interface Timer<TMilli> as waitForData;
	uses interface PacketAcknowledgements as Ack;
	uses interface Leds;
	uses interface Packet;
	uses interface AMSend as AMSendC;
	uses interface AMPacket;
	uses interface SplitControl as AMControl;
	
	uses interface startPeriodicalRadio as SPradio;
	uses interface shareFlag as shareflag;
	
	provides interface Receive as PReceive;
	uses interface Receive as CReceive;
}
implementation
{
	am_addr_t tmp_addr=Target_Node; message_t *tmp_msg; uint8_t tmp_len;
	bool SendDone_flag = FALSE;
	bool Send_flag = FALSE;
	message_t pkt;
	
	task void Send();
	
	command void SPradioP.startPeriodicRadio(uint16_t start, uint16_t stop){
		call SPradio.startPeriodicRadio(call shareflag.What_is_myPeriod(), stop);
	}
	
	command error_t AMSendP.send(am_addr_t addr, message_t *msg, uint8_t len){	
		tmp_addr = addr; tmp_msg = msg; tmp_len = len;
		Send_flag = FALSE;
		if(SendDone_flag == FALSE){
			call SPradio.stopTimer();
			call SPradio.startCycleRadio(2000);
			call OffTimer.startOneShot(2000);
			SendDone_flag = TRUE;
		}
		return post Send();
	}
	
	task void Send() {
		LPLMsg* LPLmsg = (LPLMsg*)call Packet.getPayload(tmp_msg, sizeof(LPLMsg));
		if(LPLmsg == NULL) { return ; }
		else {
			call Ack.requestAck(tmp_msg);
			LPLmsg->myPeriod = call shareflag.What_is_myPeriod();
			if(call AMSendC.send(tmp_addr,tmp_msg, tmp_len) == SUCCESS){
				//printf("Sending a packet is successful!\n node ID : %d, pkt Sequence : %d,	 period : %d\n", LPLmsg->nodeid, LPLmsg->pktSequence, LPLmsg->myPeriod);
				//printfflush();
				Send_flag = TRUE;
			}
		}
		if(Send_flag == FALSE) { post Send(); }
		return;
	}
	
	event message_t * CReceive.receive(message_t *msg, void *payload, uint8_t len){
		LPLMsg *data_pkt = (LPLMsg*)payload;
		//printf("Receivng a packet is successful!\n node ID : %d, pkt Sequence : %d, period : %d\n", data_pkt->nodeid, data_pkt->pktSequence, data_pkt->myPeriod);
		//printfflush();
		
		if((data_pkt->myPeriod)< call shareflag.What_is_myPeriod()){
			//printf("My Period is %d\n",call shareflag.What_is_myPeriod());
			//printfflush();
			//printf("Set my Period is %d\n", data_pkt->myPeriod);
			call shareflag.This_is_myPeriod(data_pkt->myPeriod);
		}	
		else if(call shareflag.What_is_myPeriod() > 900)
			call shareflag.This_is_myPeriod(call shareflag.What_is_myPeriod()-500);
		else
			call shareflag.This_is_myPeriod(500);
			
		return signal PReceive.receive(msg, payload, len);
	}
	
	event void AMSendC.sendDone(message_t *msg, error_t error){
		if(call Ack.wasAcked(msg)) {
			call waitForData.startOneShot(20);
			//printf("Receiving Ack is successful!\n");
			//printfflush();
			
			Send_flag = FALSE;
			SendDone_flag = FALSE;
		}
		else{			
			post Send();
		}
		signal AMSendP.sendDone(msg, error);
	}
	
	event void OffTimer.fired(){
		SendDone_flag = FALSE;
		//call SPradio.startPeriodicRadio(1000, 10);
	}

	event void waitForData.fired(){
		call SPradio.stopTimer();
		call SPradio.startPeriodicRadio(call shareflag.What_is_myPeriod(),20);
		call OffTimer.stop();
	}

	command void * AMSendP.getPayload(message_t *msg, uint8_t len){
		return call AMSendC.getPayload(msg,len);
	}

	command error_t AMSendP.cancel(message_t *msg){
		return call AMSendC.cancel(msg);
	}

	command uint8_t AMSendP.maxPayloadLength(){
		return call AMSendC.maxPayloadLength();
	}

	event void AMControl.startDone(error_t error){
		signal AMControlP.startDone(error);
	}

	event void AMControl.stopDone(error_t error){
		signal AMControlP.stopDone(error);
	}

	command error_t AMControlP.start(){
		return call AMControl.start();
	}
	
	command error_t AMControlP.stop(){
		return call AMControl.stop();
	}

	command void PacketP.clear(message_t *msg){
		call Packet.clear(msg);
	}

	command uint8_t PacketP.maxPayloadLength(){
		return call Packet.maxPayloadLength();
	}

	command uint8_t PacketP.payloadLength(message_t *msg){
		return call Packet.payloadLength(msg);
	}

	command void * PacketP.getPayload(message_t *msg, uint8_t len){
		return call Packet.getPayload(msg, len);
	}

	command void PacketP.setPayloadLength(message_t *msg, uint8_t len){
		call Packet.setPayloadLength(msg, len);
	}

	command am_addr_t AMPacketP.address(){
		return call AMPacket.address();
	}

	command am_addr_t AMPacketP.source(message_t *amsg){
		return call AMPacket.source(amsg);
	}

	command am_group_t AMPacketP.localGroup(){
		return call AMPacket.localGroup();
	}

	command void AMPacketP.setDestination(message_t *amsg, am_addr_t addr){
		call AMPacket.setDestination(amsg,addr);
	}

	command void AMPacketP.setSource(message_t *amsg, am_addr_t addr){
		call AMPacket.setSource(amsg,addr);
	}

	command am_id_t AMPacketP.type(message_t *amsg){
		return call AMPacket.type(amsg);
	}

	command void AMPacketP.setGroup(message_t *amsg, am_group_t grp){
		call AMPacket.setGroup(amsg,grp);
	}

	command void AMPacketP.setType(message_t *amsg, am_id_t t){
		call AMPacket.setType(amsg,t);
	}

	command am_group_t AMPacketP.group(message_t *amsg){
		return call AMPacket.group(amsg);
	}

	command bool AMPacketP.isForMe(message_t *amsg){
		return call AMPacket.isForMe(amsg);
	}

	command am_addr_t AMPacketP.destination(message_t *amsg){
		return call AMPacket.destination(amsg);
	}

	command void SPradioP.stopTimer(){
		call SPradio.stopTimer();
	}

	command void SPradioP.startCycleRadio(uint16_t time){
		call SPradio.startCycleRadio(time);
	}

	async command bool AckP.wasAcked(message_t *msg){
		return call Ack.wasAcked(msg);
	}

	async command error_t AckP.noAck(message_t *msg){
		return call Ack.noAck(msg);
	}

	async command error_t AckP.requestAck(message_t *msg){
		return call Ack.requestAck(msg);
	}

	event void CC2420Config.syncDone(error_t error){
		// TODO Auto-generated method stub
	}
}