package org.chboston.cnlp.ctakes.kernel;

public class LinearKernel extends PolyKernel{
	public LinearKernel(){
		this(false);
	}
	public LinearKernel(boolean norm){
		super(1,0.0, norm);
	}
}

