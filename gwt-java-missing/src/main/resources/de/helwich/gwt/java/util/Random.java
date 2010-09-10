package java.util;

/**
 * Class java.util.Random is not part of the GWT API. All used functionality of
 * this class is implemented here.
 * 
 * @author Hendrik Helwich
 *
 */
public class Random {

    public native int nextInt(int upperBound) /*-{
		// "~~" forces the value to a 32 bit integer.
		return ~~(Math.floor(Math.random() * upperBound));
	}-*/; 
    
}
