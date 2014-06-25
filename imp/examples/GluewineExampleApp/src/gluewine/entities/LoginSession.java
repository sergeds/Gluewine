package gluewine.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Session")
public class LoginSession {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0;
	
	@Column(name = "isAdmin")
	private Boolean isAdmin;
	
	@Column(name = "userName")
	private String username;
	
	@Column(name="isActive")
	private Boolean isActive; //true when the user is logged in, false when the session is over	
	
	@Column(name="loginTime")
	private String loginTime;
	
	@Column(name="logoutTime")
	private String logoutTime;
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id) 
	{
		this.id = id;
	}
	
	public Boolean getIsAdmin()
	{
		return isAdmin;
	}
	
	public void setIsAdmin(Boolean isAdmin) 
	{
		this.isAdmin = isAdmin;
	}
	
	public String getUsername() 
	{
		return username;
	}
	
	public void setUsername(String username)
	{
		this.username = username;
	}
	
	public Boolean getIsActive()
	{
		return isActive;
	}
	
	public void setIsActive(Boolean isActive) 
	{
		this.isActive = isActive;
	}
	
	public String getLoginTime()
	{
		return loginTime;
	}
	
	public void setLoginTime(String loginTime) 
	{
		this.loginTime = loginTime;
	}
	
	public String getLogoutTime() 
	{
		return logoutTime;
	}
	
	public void setLogoutTime(String logoutTime) 
	{
		this.logoutTime = logoutTime;
	}
}