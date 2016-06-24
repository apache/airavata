import exception.AuroraException;

public interface AuroraJobSchedulerI {
	
	public void jobUpdateInfo(String info) throws AuroraException;
	public void jobUpdate(String update) throws AuroraException;
	public void jobRestart(String restart) throws AuroraException;
	public void jobKill(String kill) throws AuroraException;
	public void jobLaunch(String name) throws AuroraException;
	public void configCreate(String name, String ram, String cpu, String disk, String image) throws AuroraException;
}
