#include "Timer.h"
#include "Leds.h"


module LEDP {
	provides interface Leds as PLeds; 
	uses interface Leds as CLeds;
}

implementation {

	async command void PLeds.led0On() { call CLeds.led0On(); }
	async command void PLeds.led0Off() { call CLeds.led0Off(); }
	async command void PLeds.led0Toggle() { call CLeds.led0Toggle(); }

	async command void PLeds.led1On() { call CLeds.led1On(); }
	async command void PLeds.led1Off() { call CLeds.led1Off(); }
	async command void PLeds.led1Toggle() { call CLeds.led1Toggle(); }

	async command void PLeds.led2On() { call CLeds.led2On(); }
	async command void PLeds.led2Off() { call CLeds.led2Off(); }
	async command void PLeds.led2Toggle() { call CLeds.led2Toggle(); }
    
	async command uint8_t PLeds.get() {  
		return call CLeds.get(); }
	async command void PLeds.set(uint8_t val) { call CLeds.set(val); }
	
}