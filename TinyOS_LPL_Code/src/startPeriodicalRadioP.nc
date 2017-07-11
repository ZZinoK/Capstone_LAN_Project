//#include"printf.h"

module startPeriodicalRadioP {
provides {
		 interface startPeriodicalRadio;
	}
	uses {
	interface Timer<TMilli> as PeriodicTimer;
	interface Timer<TMilli> as PeriodicTimer2;
	interface Timer<TMilli> as CycleTimer;
	interface SplitControl as AMControlSPR;
	interface shareFlag as shareflag;
	}
}
implementation {
	uint16_t val_stop=0, val_start=0;
	
	command void startPeriodicalRadio.startPeriodicRadio(uint16_t start, uint16_t stop){
		call AMControlSPR.stop();
		call PeriodicTimer.startOneShot(start-stop);
		val_stop = stop;
		val_start = start;
	}

	event void PeriodicTimer.fired() {
		call AMControlSPR.start();
		//printf("----------------------- Radio On and my Period %d\n", call shareflag.What_is_myPeriod());
		//printfflush();
	    call PeriodicTimer2.startOneShot(val_stop);
	    //printf("---------------- Radio Trun on ---------------------\n");
	    return;
	}
	event void PeriodicTimer2.fired() {
		if(call shareflag.What_is_myPeriod() < 2000){
			call shareflag.This_is_myPeriod(call shareflag.What_is_myPeriod()+100);
		}
		
		call AMControlSPR.stop();	
		call PeriodicTimer.startOneShot(call shareflag.What_is_myPeriod()-val_stop);
		//printf("---------------- Radio Trun off ---------------------\n");
		return;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	command void startPeriodicalRadio.startCycleRadio(uint16_t time){
		call AMControlSPR.start();
		call CycleTimer.startOneShot(time);
		//printf("---------------- Radio Trun on ---------------------\n");
	}
	event void CycleTimer.fired(){
		call AMControlSPR.stop();
		call PeriodicTimer.startOneShot(call shareflag.What_is_myPeriod()-val_stop);
		//printf("---------------- Radio Trun off ---------------------\n");
		return;
	}
	
	command void startPeriodicalRadio.stopTimer(){
		//call AMControlSPR.start();
		call PeriodicTimer.stop();
		call PeriodicTimer2.stop();
		call CycleTimer.stop();
		//printf("---------------- Change Period ---------------------\n");
	}
	event void AMControlSPR.startDone(error_t error){
	}
	
	event void AMControlSPR.stopDone(error_t error){
	}
}
