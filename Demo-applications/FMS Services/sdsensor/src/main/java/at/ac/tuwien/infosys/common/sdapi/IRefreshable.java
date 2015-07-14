package at.ac.tuwien.infosys.common.sdapi;

import java.util.List;

public interface IRefreshable {

	public Object getProperty();
	public void refresh(List<IRefreshable> newDependencies);
}
