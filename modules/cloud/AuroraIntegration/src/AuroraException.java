import java.io.*;
import java.util.*;

public class AuroraException extends Exception {
	private String errorMsg;
	public AuroraException(String e){
		this.errorMsg=e;
	}
	public String toString(){
		return 	this.errorMsg;
	}


}