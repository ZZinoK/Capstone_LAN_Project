#ifndef LPLC_H
#define LPLC_H
	enum {
    	AM_RADIO = 6,
    };
    
    typedef nx_struct LPLMsg {
    	nx_uint8_t nodeid;
    	nx_uint8_t route;
    	nx_int16_t pitch;
    	nx_int16_t roll;
    	nx_int16_t yaw;
    	nx_uint8_t variation;
    	nx_uint8_t myPeriod;
  	} LPLMsg;
#endif /* LPPC_H */
