package at.ac.tuwien.infosys.common.sdapi;

public interface Bootstrapable{

	public void setRootDependency(Object o);
	public void start();
	public void stop();
}
