package domain;

public class User {
	
	private int uId;
	private String username;
	private String email;
	private String genre;
	private String autonomousCommunity;
	private int age;

	public int getUId() {
		return uId;
	}

	public void setUId(int uId) {
		this.uId = uId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getAutonomousCommunity() {
		return autonomousCommunity;
	}

	public void setAutonomousCommunity(String autonomousCommunity) {
		this.autonomousCommunity = autonomousCommunity;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
