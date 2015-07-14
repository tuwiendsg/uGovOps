package at.ac.tuwien.infosys.common.sdapi;

public interface Producer {


	public void setUp();
	
	public void push(Event e);
	
	public void pollEvent();
	
	public void close();
}
