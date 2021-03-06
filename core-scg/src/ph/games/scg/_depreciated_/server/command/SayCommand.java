package ph.games.scg._depreciated_.server.command;

import java.net.Socket;

import ph.games.scg.util.Debug;

public class SayCommand extends Command {
	
	private String message;
	
	public SayCommand(Socket sock, String message) {
		super(CMD_TYP.SAY, sock);
		this.message = message;
		
		Debug.logv("New Command: " + this);
	}
	
	public String getMessage() {
		return this.message;
	}
	
	@Override
	public String toCommandString() {
		return "\\say " + this.message;
	}
	
	@Override
	public String toString() {
		return super.toString() + "{sock=" + this.getSock() + " message=" + this.message + "}";
	}
	
}
