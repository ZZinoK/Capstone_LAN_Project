
interface shareFlag {
	command bool dose_it_send();
	command void I_send_it(bool state);
	
	command bool does_it_receive();
	command void I_recevie_it(bool state);
	
	command uint16_t What_is_myPeriod();
	command void This_is_myPeriod(uint16_t intput_period);
}