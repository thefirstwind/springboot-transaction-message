package com.tdw.transaction.constant;

/**
 * 阀值定义
 * @author DELL
 *
 */
public enum Threshods {
	
	MAX_SEND(10), MAX_PRESENDBACK(6), MAX_RESULTBACK(3),OVER(0);

    private int nCode ;

    private Threshods( int _nCode) {
        this.nCode = _nCode;
    }

    public int code() {
        return this.nCode;
    }
    
    @Override
    public String toString() {
        return String.valueOf ( this.nCode );
    }
    
}
