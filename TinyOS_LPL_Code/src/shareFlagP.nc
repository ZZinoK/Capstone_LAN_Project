module shareFlagP {
provides {
		 interface shareFlag;
	}

}
implementation {
	static bool sendState = FALSE;
	static bool receiveState = FALSE;	
	static uint16_t period=1000;

	command bool shareFlag.dose_it_send(){
		return sendState;
	}

	command void shareFlag.I_send_it(bool state){
		sendState = state;
	}

	command bool shareFlag.does_it_receive(){
		return receiveState;
	}

	command void shareFlag.I_recevie_it(bool state){
		receiveState = state;
	}

	command uint16_t shareFlag.What_is_myPeriod(){
		return period;
	}

	command void shareFlag.This_is_myPeriod(uint16_t input_period){
		period = input_period;
	}
}
