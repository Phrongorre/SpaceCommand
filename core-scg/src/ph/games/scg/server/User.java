package ph.games.scg.server;

import ph.games.scg.util.ILoggable;

public class User implements ILoggable {
	
	private String username;
	private String password;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	public User(String username) { this(username, null); }
	
	public String getUsername() {
		return this.username;
	}
	
	public boolean hasPassword(String password) {
		if (this.password == null) return true;
		return (this.password.equals(password));
	}
	
	@Override
	public String toString() {
		return "USER{username=" + this.username + "}";
	}

}